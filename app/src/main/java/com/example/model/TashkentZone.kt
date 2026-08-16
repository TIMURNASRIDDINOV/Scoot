package com.example.model

enum class ZoneType {
    RIDE_ZONE,           // Standard permitted riding zone
    SLOW_SPEED_15KMH,    // Pedestrian friendly speed-limited zone (e.g. Broadway, Parks)
    NO_PARKING,          // Strict no-parking zone (government buildings, high traffic)
    RESTRICTED           // Non-service zone
}

data class TashkentZone(
    val id: String,
    val name: String,
    val nameUz: String,
    val type: ZoneType,
    val description: String,
    val centerLat: Double,
    val centerLng: Double,
    val radiusMeters: Double,
    val maxSpeedKmh: Int = when (type) {
        ZoneType.SLOW_SPEED_15KMH -> 15
        ZoneType.RESTRICTED -> 0
        else -> 25
    }
)

object TashkentZones {
    val zones = listOf(
        TashkentZone(
            id = "zone-amir-timur",
            name = "Amir Timur Square & Ring",
            nameUz = "Amir Temur Xiyoboni",
            type = ZoneType.RIDE_ZONE,
            description = "Central hub • High scooter availability & drop-off spots",
            centerLat = 41.3111,
            centerLng = 69.2797,
            radiusMeters = 400.0
        ),
        TashkentZone(
            id = "zone-broadway",
            name = "Sayilgoh (Broadway) Promenade",
            nameUz = "Sayilgoh ko'chasi (Broadway)",
            type = ZoneType.SLOW_SPEED_15KMH,
            description = "Pedestrian Boulevard • Auto-speed capped at 15 km/h for safety",
            centerLat = 41.3135,
            centerLng = 69.2742,
            radiusMeters = 350.0
        ),
        TashkentZone(
            id = "zone-tashkent-city",
            name = "Tashkent City Park",
            nameUz = "Toshkent Siti Bog'i",
            type = ZoneType.SLOW_SPEED_15KMH,
            description = "Park paths & musical fountain • Slow 15 km/h zone",
            centerLat = 41.3128,
            centerLng = 69.2540,
            radiusMeters = 500.0
        ),
        TashkentZone(
            id = "zone-mustaqillik",
            name = "Mustaqillik Government Square",
            nameUz = "Mustaqillik Maydoni",
            type = ZoneType.NO_PARKING,
            description = "Government perimeter • Riding allowed, Parking strictly prohibited",
            centerLat = 41.3168,
            centerLng = 69.2682,
            radiusMeters = 300.0
        ),
        TashkentZone(
            id = "zone-chorsu",
            name = "Chorsu Bazaar & Old City",
            nameUz = "Chorsu Bozori",
            type = ZoneType.RIDE_ZONE,
            description = "Historic marketplace & Kukeldash Madrasah parking hub",
            centerLat = 41.3275,
            centerLng = 69.2360,
            radiusMeters = 450.0
        ),
        TashkentZone(
            id = "zone-navoi",
            name = "Alisher Navoi Theatre Plaza",
            nameUz = "Alisher Navoiy Teatri",
            type = ZoneType.RIDE_ZONE,
            description = "Fountain plaza & cafe zone with designated parking bays",
            centerLat = 41.3090,
            centerLng = 69.2720,
            radiusMeters = 280.0
        ),
        TashkentZone(
            id = "zone-magic-city",
            name = "Magic City Park",
            nameUz = "Magic City Bog'i",
            type = ZoneType.SLOW_SPEED_15KMH,
            description = "Family amusement area • 15 km/h max speed zone",
            centerLat = 41.3032,
            centerLng = 69.2470,
            radiusMeters = 400.0
        )
    )
}
