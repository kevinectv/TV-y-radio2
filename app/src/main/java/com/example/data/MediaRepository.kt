package com.example.data

import com.example.data.database.FavoriteEntity
import com.example.data.database.MediaDao
import com.example.data.database.RecentEntity
import com.example.data.database.PlaylistEntity
import com.example.data.database.EpgSourceEntity
import com.example.data.model.Channel
import com.example.data.model.EPGProgram
import com.example.data.model.RadioStation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MediaRepository(private val mediaDao: MediaDao) {

    // Predefined IPTV Channels
    val channelsList = listOf(
        Channel(
            id = "nasa_science",
            name = "NASA Science Live",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            logoUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=200",
            category = "Documentary",
            description = "Explore the universe, view spectacular live views of Planet Earth, and watch deep space launches.",
            number = 101
        ),
        Channel(
            id = "sintel_cinema",
            name = "Sintel HD Cinema",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            logoUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=200",
            category = "Cinema",
            description = "Classic independent animated cinema and masterpieces of computer-generated graphic design.",
            number = 102
        ),
        Channel(
            id = "tears_scifi",
            name = "Sci-Fi Universe HQ",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            logoUrl = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?q=80&w=200",
            category = "Sci-Fi",
            description = "A showcase of near-future dystopian landscapes, cybernetic technology, and majestic visual effects.",
            number = 103
        ),
        Channel(
            id = "elephants_tech",
            name = "DreamTech Channel",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            logoUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=200",
            category = "Tech",
            description = "Surreal mechanical wonders, innovative technology presentations, and robotic evolutionary streams.",
            number = 104
        ),
        Channel(
            id = "nature_adventure",
            name = "Wild Explorer Live",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            logoUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?q=80&w=200",
            category = "Adventure",
            description = "Breathtaking landscapes, deep ocean surveys, and intense wildlife expeditions across extreme habitats.",
            number = 105
        )
    )

    // Predefined Radio Stations
    val radioStationsList = listOf(
        RadioStation(
            id = "lofi_beats",
            name = "Lofi Cozy Beats",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            logoUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=300",
            genre = "Lofi Hip Hop",
            frequency = "88.5 FM",
            themeColorHex = "#2E1A47" // Deep Purple
        ),
        RadioStation(
            id = "classic_jazz",
            name = "Golden Era Jazz",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            logoUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?q=80&w=300",
            genre = "Classic Jazz",
            frequency = "95.1 FM",
            themeColorHex = "#3E2723" // Dark Brown Warm
        ),
        RadioStation(
            id = "synth_future",
            name = "Synthwave Retro 1984",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            logoUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=300",
            genre = "Outrun Synthwave",
            frequency = "101.9 FM",
            themeColorHex = "#1A0033" // Electric Neon Purple
        ),
        RadioStation(
            id = "ambient_space",
            name = "Atmospheric Void",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            logoUrl = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=300",
            genre = "Space Ambient",
            frequency = "104.3 FM",
            themeColorHex = "#0D1B2A" // Void Blue
        ),
        RadioStation(
            id = "rock_classic",
            name = "Infinite Rock Legends",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            logoUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?q=80&w=300",
            genre = "Hard Rock",
            frequency = "98.7 FM",
            themeColorHex = "#1C0D02" // Lava Red Black
        )
    )

    // Predefined programs covering a standard timeline. Let's make 24h timeline
    val programsList = listOf(
        // channel: nasa_science (101)
        EPGProgram("ns1", "nasa_science", "Morning Star Launch Panel", "Live broadcast covering the Orion flight configuration, space rocket boosters design, and scientific exploration modules.", "08:00 AM", "09:30 AM", 8.0f, 1.5f, "https://images.unsplash.com/photo-1541185933-ef5d8ed016c2?q=80&w=200", "Science"),
        EPGProgram("ns2", "nasa_science", "Hubble Odyssey: Deep Fields", "A magnificent documentary detailing the deepest focal point images captured by Hubble in distant cosmic clusters.", "09:30 AM", "11:30 AM", 9.5f, 2.0f, "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=200", "Documentary"),
        EPGProgram("ns3", "nasa_science", "Live ISS Station Report", "Daily orbital feeds directly broadcasted from the International Space Station crew detailing microgravity botanical experiments.", "11:30 AM", "01:00 PM", 11.5f, 1.5f, "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=200", "Live"),
        EPGProgram("ns4", "nasa_science", "Exoplanet Frontiers", "Exploring habitable zones of red dwarf stars, ocean planets, and visual simulations of atmospheric conditions.", "01:00 PM", "03:00 PM", 13.0f, 2.0f, "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?q=80&w=200", "Science"),
        EPGProgram("ns5", "nasa_science", "Apollo Historic Vaults", "Restored archival footage from the lunar surface, astronaut communications, and design reviews of Saturn V rocketry.", "03:00 PM", "05:00 PM", 15.0f, 2.0f, "https://images.unsplash.com/photo-1517976487492-5750f3195933?q=80&w=200", "History"),
        EPGProgram("ns6", "nasa_science", "Mars Rover Curiosity: Year Ten", "Stunning panoramic vistas from Mount Sharp and scientific summary of soil sampling missions.", "05:00 PM", "07:30 PM", 17.0f, 2.5f, "https://images.unsplash.com/photo-1612892483236-41136b017fc9?q=80&w=200", "Science"),
        EPGProgram("ns7", "nasa_science", "James Webb: Deep Universe Part 1", "An in-depth analysis of newly analyzed infrared observations highlighting the emergence of infant galaxies.", "07:30 PM", "09:30 PM", 19.5f, 2.0f, "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?q=80&w=200", "Science"),
        EPGProgram("ns8", "nasa_science", "Infinite Cosmic Horizons", "Late-night meditative ambient visual journey into modeled planetary systems and majestic nebula structures.", "09:30 PM", "12:00 AM", 21.5f, 2.5f, "https://images.unsplash.com/photo-1444703686981-a3abbc4d4fe3?q=80&w=200", "Relax"),

        // channel: sintel_cinema (102)
        EPGProgram("sc1", "sintel_cinema", "Animation Masterclass: Blender", "A step-by-step masterclass explaining modeling, dynamic bone rigging, and complex hair particle rendering techniques.", "08:00 AM", "10:00 AM", 8.0f, 2.0f, "https://images.unsplash.com/photo-1601987077677-5346c0c57d3f?q=80&w=200", "Educational"),
        EPGProgram("sc2", "sintel_cinema", "Sintel Untold: Behind the Scenes", "Visual compilation and animator interviews discussing the production of the open-source cinematic masterpiece Sintel.", "10:00 AM", "11:30 AM", 10.0f, 1.5f, "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=200", "Special"),
        EPGProgram("sc3", "sintel_cinema", "Sintel: Movie Premiere (Extended Edition)", "Pre-rendered masterpiece showing Sintel's search for her baby dragon, showcasing stunning visuals and orchestral track.", "11:30 AM", "01:30 PM", 11.5f, 2.0f, "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=200", "Cinema"),
        EPGProgram("sc4", "sintel_cinema", "CGI Character Sculpting Live", "An specialized digital sculpting artist creates fantasy characters in real-time speed, utilizing high-dynamic shaders.", "01:30 PM", "03:30 PM", 13.5f, 2.0f, "https://images.unsplash.com/photo-1513364776144-60967b0f800f?q=80&w=200", "Art"),
        EPGProgram("sc5", "sintel_cinema", "The Art of Raytracing Light", "Educational visual guides on raytracing algorithms, global illumination, path-tracing, and photon mapping physics.", "03:30 PM", "05:30 PM", 15.5f, 2.0f, "https://images.unsplash.com/photo-1547891654-e66ed7edd96c?q=80&w=200", "Tech"),
        EPGProgram("sc6", "sintel_cinema", "Tears of Steel: Sound Integration", "A sound effects compilation demonstrating acoustic foley works and Dolby Atmos surround integrations.", "05:30 PM", "07:30 PM", 17.5f, 2.0f, "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=200", "Audio"),
        EPGProgram("sc7", "sintel_cinema", "Classic Indie Animated Showcase", "A curated anthology of short award-winning animated indie productions from modern European cinema creators.", "07:30 PM", "10:00 PM", 19.5f, 2.5f, "https://images.unsplash.com/photo-1518173946687-a4c8a383392e?q=80&w=200", "Cinema"),
        EPGProgram("sc8", "sintel_cinema", "Late Night Cyberpunk Shorts", "Dark, atmospheric neo-noir animation shorts exploring glowing neon streets, robotics, and cybernetic identity.", "10:00 PM", "12:00 AM", 22.0f, 2.0f, "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=200", "Cinema"),

        // channel: tears_scifi (103)
        EPGProgram("ts1", "tears_scifi", "The Cybernetic Future Panel", "Intellectual talk-show analyzing bionic limb control, high-efficiency neural interfaces, and future bio-robotics.", "08:00 AM", "09:30 AM", 8.0f, 1.5f, "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?q=80&w=200", "Talk"),
        EPGProgram("ts2", "tears_scifi", "Sci-Fi Visual Effects breakdown", "In-depth studio review highlighting chromakey rotoscoping, camera matching tracking, and compositing elements in Tears of Steel.", "09:30 AM", "12:00 PM", 9.5f, 2.5f, "https://images.unsplash.com/photo-1535016120720-40c646be5580?q=80&w=200", "Behind the Scenes"),
        EPGProgram("ts3", "tears_scifi", "Tears of Steel: Special Cut", "Full sci-fi cinematic presentation featuring near-future Amsterdam, advanced weapon systems, and massive cyber-crabs.", "12:00 PM", "02:00 PM", 12.0f, 2.0f, "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?q=80&w=200", "Cinema"),
        EPGProgram("ts4", "tears_scifi", "AI Revolution in VFX", "An engineering documentary highlighting the deployment of machine learning neural models inside post-production environments.", "02:00 PM", "04:30 PM", 14.0f, 2.5f, "https://images.unsplash.com/photo-1507146426996-ef05306b995a?q=80&w=200", "Documentary"),
        EPGProgram("ts5", "tears_scifi", "Epic Robotic Concept Design", "Digital design session mapping high-detail mechanical joints, hydraulic armatures, and armored panels.", "04:30 PM", "06:30 PM", 16.5f, 2.0f, "https://images.unsplash.com/photo-1531746790731-6c087fecd05a?q=80&w=200", "Art"),
        EPGProgram("ts6", "tears_scifi", "Cyber City VR: Amsterdam 2099", "VR exploration of cyberpunk elements, floating traffic lanes, dynamic advertising screens, and rain-washed alleyways.", "06:30 PM", "08:30 PM", 18.5f, 2.0f, "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?q=80&w=200", "Sci-Fi"),
        EPGProgram("ts7", "tears_scifi", "Dystopian Sci-Fi Cinema: Chrono", "An exciting sci-fi short film focusing on timeline containment, temporal paradox management, and quantum warp field generators.", "08:30 PM", "10:30 PM", 20.5f, 2.0f, "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=200", "Cinema"),
        EPGProgram("ts8", "tears_scifi", "Synthetic Humans Debate", "Late-night philosophy conference summarizing ethical standards around conscious artificial intellects and robot rights.", "10:30 PM", "12:00 AM", 22.5f, 1.5f, "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?q=80&w=200", "Talk"),

        // channel: elephants_tech (104)
        EPGProgram("et1", "elephants_tech", "Robotic Assembly Systems Live", "Live camera from automated factory lines showcasing high-speed motherboard fabrication, SMD mounting, and testing.", "08:00 AM", "10:00 AM", 8.0f, 2.0f, "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?q=80&w=200", "Tech"),
        EPGProgram("et2", "elephants_tech", "Elephants Dream: CGI Origins", "A historical retrospective of early Blender open movie projects, exploring the design of the giant mechanical telephone exchange.", "10:00 AM", "12:00 PM", 10.0f, 2.0f, "https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=200", "History"),
        EPGProgram("et3", "elephants_tech", "Elephants Dream: Animated Movie", "The surreal and visually eccentric movie exploring the mechanical dreamscape of Proog and Emo under massive structures.", "12:00 PM", "01:30 PM", 12.0f, 1.5f, "https://images.unsplash.com/photo-1563089145-599997674d42?q=80&w=200", "Cinema"),
        EPGProgram("et4", "elephants_tech", "Programming the Smart TV Evolution", "A deep technical presentation by lead engineering groups detailing performance improvements in Kotlin and Jetpack Compose on TV.", "01:30 PM", "03:30 PM", 13.5f, 2.0f, "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?q=80&w=200", "Tech"),
        EPGProgram("et5", "elephants_tech", "Modular Synthesizers In Studio", "Electronic artists demonstrate custom modular synth rigs, patch cables, envelope filters, and voltage control oscillation loops.", "03:30 PM", "05:30 PM", 15.5f, 2.0f, "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=200", "Music"),
        EPGProgram("et6", "elephants_tech", "Future Tech Gadgets 2026", "A futuristic tech expo reviewing flexible AMOLED displays, smart optical glasses, holographic projectors, and smart home hubs.", "05:30 PM", "08:00 PM", 17.5f, 2.5f, "https://images.unsplash.com/photo-1504274066654-52ff5a553541?q=80&w=200", "Tech"),
        EPGProgram("et7", "elephants_tech", "Quantum Computing Breakthroughs", "Academic experts explain qubit coherence times, silicon spin qubits architecture, and cryogenic cooling systems.", "08:00 PM", "10:00 PM", 20.0f, 2.0f, "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?q=80&w=200", "Science"),
        EPGProgram("et8", "elephants_tech", "Lofi Tech Code: Late-Night Stream", "Chill synthetic track overlayed on visual patterns of real-time computer compiler code optimization logs.", "10:00 PM", "12:00 AM", 22.0f, 2.0f, "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?q=80&w=200", "Music"),

        // channel: nature_adventure (105)
        EPGProgram("na1", "nature_adventure", "Patagonia: Glacier Kingdoms", "Spectacular drone and high-definition recordings of massive ice structures calving in southern South America valleys.", "08:00 AM", "09:30 AM", 8.0f, 1.5f, "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?q=80&w=200", "Nature"),
        EPGProgram("na2", "nature_adventure", "Extreme Fire Climbers of Oregon", "Elite tactical firefighters battle wild blazing forest fires across dense mountains, using custom airplane drop tracking.", "09:30 AM", "11:30 AM", 9.5f, 2.0f, "https://images.unsplash.com/photo-1469474968028-56623f02e42e?q=80&w=200", "Adventure"),
        EPGProgram("na3", "nature_adventure", "Amazon Canopy Live Exploration", "A real-time scientific botanical team mounts 50-meter tree climbing operations in deep unexplored Peruvian forest blocks.", "11:30 AM", "02:00 PM", 11.5f, 2.5f, "https://images.unsplash.com/photo-1448375240586-882707db888b?q=80&w=200", "Live"),
        EPGProgram("na4", "nature_adventure", "Volcanic Abyss: Diving Mariana", "High-pressure submarine drone feeds highlighting thermal vents, active underwater sulfur volcanoes, and alien crustacean life.", "02:00 PM", "04:00 PM", 14.0f, 2.0f, "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=200", "Documentary"),
        EPGProgram("na5", "nature_adventure", "Climbing K2 Without Oxygen", "Staggering mountain climbing expedition records following high-altitude alpine specialists scaling ice chimneys on K2.", "04:30 PM", "06:30 PM", 16.5f, 2.0f, "https://images.unsplash.com/photo-1522014631610-ae4901f65f3f?q=80&w=200", "Adventure"),
        EPGProgram("na6", "nature_adventure", "African Savanna: Night Predators", "Utilizing specialized night-vision thermo-imaging cameras to reveal tactical hunts by lion prides during intense rain.", "06:30 PM", "08:30 PM", 18.5f, 2.0f, "https://images.unsplash.com/photo-1547471080-7cc2caa01a7e?q=80&w=200", "Nature"),
        EPGProgram("na7", "nature_adventure", "Deep Ocean Blue: The Twilight Zone", "A classic expedition searching deep water zones between 200 and 1000 meters, discovering glowing comb jellies.", "08:30 PM", "10:30 PM", 20.5f, 2.0f, "https://images.unsplash.com/photo-1546026423-cc4642628d2b?q=80&w=200", "Nature"),
        EPGProgram("na8", "nature_adventure", "Campfire Meditations Live Feed", "A high-fidelity tranquil feed of crackling pine wood logs glowing under beautiful nocturnal mountain star arrays.", "10:30 PM", "12:00 AM", 22.5f, 1.5f, "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?q=80&w=200", "Relax")
    )

    // Reactive streams from the DB
    fun getFavorites(): Flow<List<FavoriteEntity>> = mediaDao.getFavorites()
    fun getRecents(): Flow<List<RecentEntity>> = mediaDao.getRecents()

    suspend fun addFavorite(itemId: String, type: String) {
        mediaDao.insertFavorite(FavoriteEntity(id = "${type}_$itemId", type = type, itemId = itemId))
    }

    suspend fun removeFavorite(itemId: String, type: String) {
        mediaDao.deleteFavorite("${type}_$itemId")
    }

    suspend fun isFavorite(itemId: String, type: String): Boolean {
        return mediaDao.isFavorite("${type}_$itemId")
    }

    suspend fun markAsRecent(itemId: String, type: String) {
        mediaDao.insertRecent(RecentEntity(id = "${type}_$itemId", type = type, itemId = itemId, lastPlayedAt = System.currentTimeMillis()))
    }

    suspend fun removeRecent(itemId: String, type: String) {
        mediaDao.deleteRecent("${type}_$itemId")
    }

    suspend fun clearRecentsHistory() {
        mediaDao.clearRecents()
    }

    // Playlist & EPG Manager actions
    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = mediaDao.getAllPlaylists()
    suspend fun insertPlaylist(playlist: PlaylistEntity) = mediaDao.insertPlaylist(playlist)
    suspend fun deletePlaylist(id: String) = mediaDao.deletePlaylist(id)
    suspend fun getPlaylistById(id: String): PlaylistEntity? = mediaDao.getPlaylistById(id)

    fun getAllEpgSources(): Flow<List<EpgSourceEntity>> = mediaDao.getAllEpgSources()
    suspend fun insertEpgSource(source: EpgSourceEntity) = mediaDao.insertEpgSource(source)
    suspend fun deleteEpgSource(id: String) = mediaDao.deleteEpgSource(id)
}
