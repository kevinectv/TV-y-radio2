package com.example.data.model

data class SportTeam(
    val id: String,
    val name: String,
    val shortName: String,
    val logoUrl: String? = null,
    val score: Int? = null
)

enum class MatchStatus {
    SCHEDULED,
    LIVE,
    FINISHED,
    POSTPONED
}

data class MatchEvent(
    val time: String,
    val teamName: String,
    val player: String,
    val type: String, // "GOAL", "YELLOW_CARD", "RED_CARD", "SUBSTITUTION"
    val detail: String = ""
)

data class SportMatch(
    val id: String,
    val homeTeam: SportTeam,
    val awayTeam: SportTeam,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val statusDisplay: String = "",
    val minute: String? = null,
    val startTime: String = "",
    val dateDisplay: String = "",
    val competition: String = "",
    val competitionLogo: String? = null,
    val venue: String? = null,
    val broadcasts: List<String> = emptyList(),
    val events: List<MatchEvent> = emptyList()
)

data class SportsLeagueSection(
    val leagueId: String,
    val leagueName: String,
    val leagueLogo: String? = null,
    val matches: List<SportMatch> = emptyList()
)
