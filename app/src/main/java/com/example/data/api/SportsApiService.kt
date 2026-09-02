package com.example.data.api

import com.example.data.model.MatchEvent
import com.example.data.model.MatchStatus
import com.example.data.model.SportMatch
import com.example.data.model.SportTeam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class SportsApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    // ESPN Public Scoreboards
    private val leagueEndpoints = listOf(
        Pair("uefa.champions", "UEFA Champions League"),
        Pair("esp.1", "LaLiga EA Sports"),
        Pair("eng.1", "Premier League"),
        Pair("ita.1", "Serie A"),
        Pair("ger.1", "Bundesliga"),
        Pair("fra.1", "Ligue 1"),
        Pair("conmebol.libertadores", "Copa Libertadores")
    )

    suspend fun fetchAllScoreboards(): List<SportMatch> = withContext(Dispatchers.IO) {
        val allMatches = mutableListOf<SportMatch>()
        for ((leagueCode, leagueName) in leagueEndpoints) {
            try {
                val matches = fetchLeagueScoreboard(leagueCode, leagueName)
                allMatches.addAll(matches)
            } catch (e: Exception) {
                // Continue with other leagues if one fails
            }
        }
        allMatches
    }

    suspend fun fetchLeagueScoreboard(leagueCode: String, fallbackLeagueName: String): List<SportMatch> = withContext(Dispatchers.IO) {
        val url = "https://site.api.espn.com/apis/site/v2/sports/soccer/$leagueCode/scoreboard"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Android TV; Lumina Media)")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return@withContext emptyList()

        val body = response.body?.string() ?: return@withContext emptyList()
        val root = JSONObject(body)
        val leaguesArr = root.optJSONArray("leagues")
        val leagueObj = leaguesArr?.optJSONObject(0)
        val leagueName = leagueObj?.optString("name", fallbackLeagueName) ?: fallbackLeagueName
        val leagueLogo = leagueObj?.optJSONArray("logos")?.optJSONObject(0)?.optString("href")

        val eventsArr = root.optJSONArray("events") ?: return@withContext emptyList()
        val matches = mutableListOf<SportMatch>()

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())

        for (i in 0 until eventsArr.length()) {
            val eventObj = eventsArr.getJSONObject(i)
            val eventId = eventObj.optString("id", i.toString())
            val dateStr = eventObj.optString("date", "")
            var parsedDate: Date? = null
            try {
                if (dateStr.isNotEmpty()) {
                    parsedDate = isoFormat.parse(dateStr)
                }
            } catch (_: Exception) {}

            val startTimeFormatted = if (parsedDate != null) timeFormat.format(parsedDate) else ""
            val dateFormatted = if (parsedDate != null) dateFormat.format(parsedDate) else ""

            val statusObj = eventObj.optJSONObject("status")
            val statusTypeObj = statusObj?.optJSONObject("type")
            val state = statusTypeObj?.optString("state", "pre") // "pre", "in", "post"
            val displayClock = statusObj?.optString("displayClock", "")
            val period = statusObj?.optInt("period", 0) ?: 0
            val detail = statusTypeObj?.optString("detail", "") ?: ""

            val matchStatus = when (state) {
                "in" -> MatchStatus.LIVE
                "post" -> MatchStatus.FINISHED
                else -> MatchStatus.SCHEDULED
            }

            val statusDisplay = when (matchStatus) {
                MatchStatus.LIVE -> if (!displayClock.isNullOrEmpty()) "$displayClock'" else if (period == 2) "2T" else if (period == 1) "1T" else "EN VIVO"
                MatchStatus.FINISHED -> "FINAL"
                MatchStatus.SCHEDULED -> if (startTimeFormatted.isNotEmpty()) startTimeFormatted else "Programado"
                MatchStatus.POSTPONED -> "Aplazado"
            }

            val competitionsArr = eventObj.optJSONArray("competitions")
            val compObj = competitionsArr?.optJSONObject(0)
            val competitorsArr = compObj?.optJSONArray("competitors")

            var homeTeam: SportTeam? = null
            var awayTeam: SportTeam? = null
            var homeScore: Int? = null
            var awayScore: Int? = null

            if (competitorsArr != null) {
                for (c in 0 until competitorsArr.length()) {
                    val comp = competitorsArr.getJSONObject(c)
                    val homeAway = comp.optString("homeAway", "")
                    val teamObj = comp.optJSONObject("team")
                    val teamId = teamObj?.optString("id", "") ?: ""
                    val teamName = teamObj?.optString("displayName", teamObj.optString("name", "Equipo")) ?: "Equipo"
                    val shortName = teamObj?.optString("shortDisplayName", teamObj.optString("abbreviation", teamName)) ?: teamName
                    val logoUrl = teamObj?.optString("logo")
                    val scoreStr = comp.optString("score", "")
                    val score = scoreStr.toIntOrNull()

                    val team = SportTeam(
                        id = teamId,
                        name = teamName,
                        shortName = shortName,
                        logoUrl = logoUrl,
                        score = score
                    )

                    if (homeAway == "home") {
                        homeTeam = team
                        homeScore = score
                    } else {
                        awayTeam = team
                        awayScore = score
                    }
                }
            }

            if (homeTeam == null || awayTeam == null) continue

            // Broadcast info (TV networks)
            val broadcastsList = mutableListOf<String>()
            val broadcastsArr = compObj?.optJSONArray("broadcasts")
            if (broadcastsArr != null) {
                for (b in 0 until broadcastsArr.length()) {
                    val bObj = broadcastsArr.getJSONObject(b)
                    val namesArr = bObj.optJSONArray("names")
                    if (namesArr != null) {
                        for (n in 0 until namesArr.length()) {
                            val name = namesArr.optString(n)
                            if (name.isNotEmpty() && !broadcastsList.contains(name)) {
                                broadcastsList.add(name)
                            }
                        }
                    }
                }
            }

            // Venue
            val venueName = compObj?.optJSONObject("venue")?.optString("fullName")

            // Events (details like goals if present)
            val eventsList = mutableListOf<MatchEvent>()
            val detailsArr = compObj?.optJSONArray("details")
            if (detailsArr != null) {
                for (d in 0 until detailsArr.length()) {
                    val dObj = detailsArr.getJSONObject(d)
                    val clock = dObj.optJSONObject("clock")?.optString("displayValue", "") ?: ""
                    val typeName = dObj.optJSONObject("type")?.optString("text", "Evento") ?: "Evento"
                    val athleteName = dObj.optJSONArray("athletesInvolved")?.optJSONObject(0)?.optString("displayName", "") ?: ""
                    val teamId = dObj.optJSONObject("team")?.optString("id", "")
                    val tName = if (teamId == homeTeam.id) homeTeam.name else if (teamId == awayTeam.id) awayTeam.name else ""

                    eventsList.add(
                        MatchEvent(
                            time = clock,
                            teamName = tName,
                            player = athleteName,
                            type = typeName,
                            detail = dObj.optString("text", "")
                        )
                    )
                }
            }

            matches.add(
                SportMatch(
                    id = eventId,
                    homeTeam = homeTeam,
                    awayTeam = awayTeam,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    status = matchStatus,
                    statusDisplay = statusDisplay,
                    minute = if (matchStatus == MatchStatus.LIVE) displayClock else null,
                    startTime = startTimeFormatted,
                    dateDisplay = dateFormatted,
                    competition = leagueName,
                    competitionLogo = leagueLogo,
                    venue = venueName,
                    broadcasts = broadcastsList,
                    events = eventsList
                )
            )
        }

        matches
    }
}
