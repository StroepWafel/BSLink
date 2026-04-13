package com.beastsaber.app.data.filters

/**
 * Display label → BeatSaver `tags` slug or `environments` API value.
 * Tag slugs match BeatSaver search (comma = AND). Environment values match diff `environment` / Solr field.
 */
enum class FilterOptionKind {
    Tag,
    Environment
}

data class FilterOption(
    val label: String,
    val apiValue: String,
    val kind: FilterOptionKind
)

data class FilterSection(
    val id: String,
    val titleKey: String,
    val options: List<FilterOption>
)

object BeatSaverFilterCatalog {

    /** Keys match [com.beastsaber.app.R.string] entries for section titles. */
    const val TITLE_STYLE = "filter_section_style"
    const val TITLE_GENRE = "filter_section_genre"
    const val TITLE_ENV_LEGACY = "filter_section_env_legacy"
    const val TITLE_ENV_NEW = "filter_section_env_new"

    private fun tag(label: String, slug: String) =
        FilterOption(label, slug, FilterOptionKind.Tag)

    private fun env(label: String, api: String) =
        FilterOption(label, api, FilterOptionKind.Environment)

    /** Style / mapping tags (subset of BeatSaver tag vocabulary). */
    private val styleTags: List<FilterOption> = listOf(
        tag("Accuracy", "accuracy"),
        tag("Alternative", "alternative"),
        tag("Balanced", "balanced"),
        tag("Challenge", "challenge"),
        tag("Dance", "dance-style"),
        tag("Drill", "drill"),
        tag("Fitbeat", "fitbeat"),
        tag("Gimmick", "gimmick"),
        tag("Speed", "speed"),
        tag("Tech", "tech"),
        tag("Tech dance", "tech-dance"),
        tag("Vibe", "vibe")
    )

    /** Genre / mood tags. */
    private val genreTags: List<FilterOption> = listOf(
        tag("Ambient", "ambient"),
        tag("Anime", "anime"),
        tag("Blues", "blues"),
        tag("Classical", "classical"),
        tag("Country", "country"),
        tag("Dance", "dance"),
        tag("Drum and bass", "drum-and-bass"),
        tag("Dubstep", "dubstep"),
        tag("Electronic", "electronic"),
        tag("Folk", "folk"),
        tag("Funk", "funk"),
        tag("Future bass", "future-bass"),
        tag("Glitch hop", "glitch-hop"),
        tag("Hard dance", "hard-dance"),
        tag("Hardcore", "hardcore"),
        tag("Hip hop", "hip-hop"),
        tag("House", "house"),
        tag("Indie", "indie"),
        tag("J-pop", "j-pop"),
        tag("J-rock", "j-rock"),
        tag("Jazz", "jazz"),
        tag("K-pop", "k-pop"),
        tag("Metal", "metal"),
        tag("Nightcore", "nightcore"),
        tag("Orchestral", "orchestral"),
        tag("Pop", "pop"),
        tag("Punk", "punk"),
        tag("R&B", "r-and-b"),
        tag("Rap", "rap"),
        tag("Rock", "rock"),
        tag("Swing", "swing"),
        tag("Synthwave", "synthwave"),
        tag("Techno", "techno"),
        tag("Trance", "trance"),
        tag("Trap", "trap"),
        tag("Vocaloid", "vocaloid")
    )

    /** Classic / legacy environments (Beat Saber 1.x naming). */
    private val environmentsLegacy: List<FilterOption> = listOf(
        env("Default", "DefaultEnvironment"),
        env("Nice", "NiceEnvironment"),
        env("Big mirror", "BigMirrorEnvironment"),
        env("Triangle", "TriangleEnvironment"),
        env("Pyramid", "PyramidEnvironment"),
        env("Dragons", "DragonsEnvironment"),
        env("KDA", "KDAEnvironment"),
        env("Monstercat", "MonstercatEnvironment"),
        env("Crab rave", "CrabRaveEnvironment"),
        env("Panic", "PanicEnvironment"),
        env("Panic 2", "Panic2Environment"),
        env("Halloween", "HalloweenEnvironment"),
        env("Rocket", "RocketEnvironment"),
        env("Skrillex", "SkrillexEnvironment"),
        env("BTS", "BTSEnvironment"),
        env("Billie Eilish", "BillieEnvironment"),
        env("Fall out boy", "FallOutBoyEnvironment"),
        env("Green day", "GreenDayEnvironment"),
        env("Imagine dragons", "ImagineDragonsEnvironment"),
        env("Interscope", "InterscopeEnvironment"),
        env("Kaleidoscope", "KaleidoscopeEnvironment"),
        env("Linkin park", "LinkinParkEnvironment"),
        env("Lollapalooza", "LollapaloozaEnvironment"),
        env("Rock mixtape", "RockMixtapeEnvironment"),
        env("Spooky", "SpookyEnvironment"),
        env("Timbaland", "TimbalandEnvironment"),
        env("The weeknd", "TheWeekndEnvironment")
    )

    /** Newer / 360 / glass-era environments. */
    private val environmentsNew: List<FilterOption> = listOf(
        env("Glass", "GlassEnvironment"),
        env("Glass 2", "Glass2Environment"),
        env("Electronic cat", "ElectronicCatEnvironment"),
        env("Hyper ship", "HyperShipEnvironment"),
        env("Hip hop", "HipHopEnvironment"),
        env("The first", "TheFirstEnvironment"),
        env("The second", "TheSecondEnvironment"),
        env("The third", "TheThirdEnvironment"),
        env("The fourth", "TheFourthEnvironment"),
        env("The fifth", "TheFifthEnvironment"),
        env("The sixth", "TheSixthEnvironment"),
        env("The seventh", "TheSeventhEnvironment"),
        env("The eighth", "TheEighthEnvironment"),
        env("The ninth", "TheNinthEnvironment"),
        env("The tenth", "TheTenthEnvironment"),
        env("The final", "TheFinalEnvironment"),
        env("The environment 2", "TheEnvironment2")
    )

    val sections: List<FilterSection> = listOf(
        FilterSection("style", TITLE_STYLE, styleTags),
        FilterSection("genre", TITLE_GENRE, genreTags),
        FilterSection("env_legacy", TITLE_ENV_LEGACY, environmentsLegacy),
        FilterSection("env_new", TITLE_ENV_NEW, environmentsNew)
    )
}
