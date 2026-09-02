package com.example.data

import com.example.data.api.SportsApiService
import com.example.data.model.MatchEvent
import com.example.data.model.MatchStatus
import com.example.data.model.SportMatch
import com.example.data.model.SportTeam
import com.example.data.model.SportsLeagueSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SportsRepository(
    private val apiService: SportsApiService = SportsApiService(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _allMatches = MutableStateFlow<List<SportMatch>>(emptyList())
    val allMatches: StateFlow<List<SportMatch>> = _allMatches.asStateFlow()

    private val _liveMatches = MutableStateFlow<List<SportMatch>>(emptyList())
    val liveMatches: StateFlow<List<SportMatch>> = _liveMatches.asStateFlow()

    private val _featuredMatches = MutableStateFlow<List<SportMatch>>(emptyList())
    val featuredMatches: StateFlow<List<SportMatch>> = _featuredMatches.asStateFlow()

    private val _leagueSections = MutableStateFlow<List<SportsLeagueSection>>(emptyList())
    val leagueSections: StateFlow<List<SportsLeagueSection>> = _leagueSections.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var pollingJob: Job? = null

    init {
        // Pre-fill with clean curated fallback in case of initial load or offline
        val initialCurated = getCuratedFallbackMatches()
        _allMatches.value = initialCurated
        updateDerivedStates(initialCurated)
        
        // Initial fetch
        scope.launch {
            refreshMatches()
        }
    }

    suspend fun refreshMatches() {
        _isLoading.value = true
        try {
            val remoteMatches = apiService.fetchAllScoreboards()
            if (remoteMatches.isNotEmpty()) {
                _allMatches.value = remoteMatches
                updateDerivedStates(remoteMatches)
            } else if (_allMatches.value.isEmpty()) {
                val fallback = getCuratedFallbackMatches()
                _allMatches.value = fallback
                updateDerivedStates(fallback)
            }
        } catch (e: Exception) {
            if (_allMatches.value.isEmpty()) {
                val fallback = getCuratedFallbackMatches()
                _allMatches.value = fallback
                updateDerivedStates(fallback)
            }
        } finally {
            _isLoading.value = false
        }
    }

    private fun updateDerivedStates(matches: List<SportMatch>) {
        val live = matches.filter { it.status == MatchStatus.LIVE }
        _liveMatches.value = live

        // Featured: live matches first, followed by upcoming, then finished
        val featured = matches.sortedWith(
            compareBy<SportMatch> {
                when (it.status) {
                    MatchStatus.LIVE -> 0
                    MatchStatus.SCHEDULED -> 1
                    MatchStatus.FINISHED -> 2
                    MatchStatus.POSTPONED -> 3
                }
            }
        ).take(15)
        _featuredMatches.value = featured

        // Group by competition
        val grouped = matches.groupBy { it.competition }
        val sections = grouped.map { (compName, list) ->
            SportsLeagueSection(
                leagueId = compName.lowercase().replace(" ", "_"),
                leagueName = compName,
                leagueLogo = list.firstOrNull()?.competitionLogo,
                matches = list
            )
        }
        _leagueSections.value = sections
    }

    fun startLivePolling(intervalSeconds: Long = 45) {
        if (pollingJob?.isActive == true) return
        pollingJob = scope.launch {
            while (isActive) {
                delay(intervalSeconds * 1000)
                try {
                    val remoteMatches = apiService.fetchAllScoreboards()
                    if (remoteMatches.isNotEmpty()) {
                        // Compare to avoid unnecessary recomposition
                        if (remoteMatches != _allMatches.value) {
                            _allMatches.value = remoteMatches
                            updateDerivedStates(remoteMatches)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun stopLivePolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun getCuratedFallbackMatches(): List<SportMatch> {
        return listOf(
            SportMatch(
                id = "uefa_1",
                homeTeam = SportTeam(
                    id = "rm",
                    name = "Real Madrid",
                    shortName = "RMA",
                    logoUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/86.png",
                    score = 2
                ),
                awayTeam = SportTeam(
                    id = "mci",
                    name = "Manchester City",
                    shortName = "MCI",
                    logoUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/382.png",
                    score = 1
                ),
                homeScore = 2,
                awayScore = 1,
                status = MatchStatus.LIVE,
                statusDisplay = "68'",
                minute = "68'",
                startTime = "21:00",
                dateDisplay = "Hoy",
                competition = "UEFA Champions League",
                venue = "Santiago Bernabéu",
                broadcasts = listOf("Movistar Liga de Campeones", "DAZN", "ESPN"),
                events = listOf(
                    MatchEvent("12'", "Manchester City", "Erling Haaland", "GOAL", "Asistencia de De Bruyne"),
                    MatchEvent("34'", "Real Madrid", "Vinícius Jr.", "GOAL", "Gran disparo desde la frontal"),
                    MatchEvent("58'", "Real Madrid", "Jude Bellingham", "GOAL", "Cabezazo tras centro")
                )
            ),
            SportMatch(
                id = "uefa_2",
                homeTeam = SportTeam(
                    id = "fcb",
                    name = "FC Barcelona",
                    shortName = "BAR",
                    logoUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/83.png",
                    score = 1
                ),
                awayTeam = SportTeam(
                    id = "psg",
                    name = "Paris Saint-Germain",
                    shortName = "PSG",
                    logoUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/160.png",
                    score = 1
                ),
                homeScore = 1,
                awayScore = 1,
                status = MatchStatus.LIVE,
                statusDisplay = "42'",
                minute = "42'",
                startTime = "21:00",
                dateDisplay = "Hoy",
                competition = "UEFA Champions League",
                venue = "Estadi Olímpic Lluís Companys",
                broadcasts = listOf("Movistar Plus+", "TNT Sports", "ESPN"),
                events = listOf(
                    MatchEvent("19'", "FC Barcelona", "Lamine Yamal", "GOAL", "Jugada individual"),
                    MatchEvent("31'", "Paris Saint-Germain", "Ousmane Dembélé", "GOAL", "Remate cruzado")
                )
            ),
            SportMatch(
                id = "laliga_1",
                homeTeam = SportTeam(
                    id = "atm",
                    name = "Atlético de Madrid",
                    shortName = "ATM",
                    logoUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/1068.png",
                    score = null
                ),
                awayTeam = SportTeam(
                    id = "sev",
                    name = "Sevilla FC",
                    shortName = "SEV",
                    logoUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/243.png",
                    score = null
                ),
                homeScore = null,
                awayScore = null,
                status = MatchStatus.SCHEDULED,
                statusDisplay = "21:00",
                minute = null,
                startTime = "21:00",
                dateDisplay = "Hoy",
                competition = "LaLiga EA Sports",
                venue = "Cívitas Metropolitano",
                broadcasts = listOf("DAZN LaLiga", "Movistar Plus+")
            ),
            SportMatch(
                id = "prem_1",
                homeTeam = SportTeam(
                    id = "ars",
                    name = "Arsenal",
                    shortName = "ARS",
                    logoUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/359.png",
                    score = null
                ),
                awayTeam = SportTeam(
                    id = "liv",
                    name = "Liverpool",
                    shortName = "LIV",
                    logoUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/364.png",
                    score = null
                ),
                homeScore = null,
                awayScore = null,
                status = MatchStatus.SCHEDULED,
                statusDisplay = "17:30",
                minute = null,
                startTime = "17:30",
                dateDisplay = "Mañana",
                competition = "Premier League",
                venue = "Emirates Stadium",
                broadcasts = listOf("DAZN", "Sky Sports", "Peacock")
            ),
            SportMatch(
                id = "seriea_1",
                homeTeam = SportTeam(
                    id = "int",
                    name = "Inter de Milán",
                    shortName = "INT",
                    logoUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/110.png",
                    score = 3
                ),
                awayTeam = SportTeam(
                    id = "juv",
                    name = "Juventus",
                    shortName = "JUV",
                    logoUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/111.png",
                    score = 2
                ),
                homeScore = 3,
                awayScore = 2,
                status = MatchStatus.FINISHED,
                statusDisplay = "FINAL",
                minute = null,
                startTime = "20:45",
                dateDisplay = "Ayer",
                competition = "Serie A",
                venue = "San Siro",
                broadcasts = listOf("Movistar Liga de Campeones", "ESPN", "Paramount+"),
                events = listOf(
                    MatchEvent("15'", "Inter de Milán", "Lautaro Martínez", "GOAL", "Disparo potente"),
                    MatchEvent("28'", "Juventus", "Dušan Vlahović", "GOAL", "Penalti"),
                    MatchEvent("54'", "Inter de Milán", "Marcus Thuram", "GOAL", "Remate de cabeza"),
                    MatchEvent("72'", "Juventus", "Federico Chiesa", "GOAL", "Tiro raso"),
                    MatchEvent("89'", "Inter de Milán", "Nicolò Barella", "GOAL", "Volea decisiva")
                )
            ),
            SportMatch(
                id = "bundes_1",
                homeTeam = SportTeam(
                    id = "bay",
                    name = "Bayern Múnich",
                    shortName = "BAY",
                    logoUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/132.png",
                    score = null
                ),
                awayTeam = SportTeam(
                    id = "dor",
                    name = "Borussia Dortmund",
                    shortName = "BVB",
                    logoUrl = "https://a.espncdn.com/i/teamlogos/soccer/500/124.png",
                    score = null
                ),
                homeScore = null,
                awayScore = null,
                status = MatchStatus.SCHEDULED,
                statusDisplay = "18:30",
                minute = null,
                startTime = "18:30",
                dateDisplay = "Sábado",
                competition = "Bundesliga",
                venue = "Allianz Arena",
                broadcasts = listOf("DAZN", "Sky Sport Bundesliga")
            )
        )
    }
}
