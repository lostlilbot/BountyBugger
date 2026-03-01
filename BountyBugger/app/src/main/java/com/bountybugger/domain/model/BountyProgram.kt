package com.bountybugger.domain.model

/**
 * Bug bounty program type
 */
enum class BountyType(val displayName: String) {
    WEB("Web"),
    API("API"),
    MOBILE("Mobile"),
    NETWORK("Network"),
    IoT("IoT"),
    CLOUD("Cloud"),
    BLOCKCHAIN("Blockchain"),
    GENERAL("General")
}

/**
 * Industry sector for bug bounty programs
 */
enum class Industry(val displayName: String) {
    TECHNOLOGY("Technology"),
    FINANCE("Finance"),
    CRYPTO("Cryptocurrency"),
    CLOUD("Cloud Computing"),
    BLOCKCHAIN("Blockchain"),
    ECOMMERCE("E-commerce"),
    SOCIAL_MEDIA("Social Media"),
    GAMING("Gaming"),
    HEALTHCARE("Healthcare"),
    EDUCATION("Education"),
    GOVERNMENT("Government"),
    TELECOMMUNICATIONS("Telecommunications"),
    TRANSPORTATION("Transportation"),
    ENERGY("Energy"),
    RETAIL("Retail"),
    MEDIA("Media & Entertainment"),
    OTHER("Other")
}

/**
 * Bug bounty platform
 */
enum class BountyPlatform(val displayName: String, val baseUrl: String) {
    HACKERONE("HackerOne", "https://api.hackerone.com"),
    BUGCROWD("Bugcrowd", "https://bugcrowd.com"),
    INTIGRITI("Intigriti", "https://intigriti.com"),
    YESWEHACK("YesWeHack", "https://yeswehack.com"),
    IMMUNEFI("Immunefi", "https://immunefi.com"),
    HACKENPROOF("HackenProof", "https://hackenproof.com"),
    OPEN_BUG_BOUNTY("Open Bug Bounty", "https://openbugbounty.org"),
    GOOGLE_VRP("Google VRP", "https://goo.gle/vrp"),
    META_BBP("Meta BBP", "https://www.facebook.com/bugbounty"),
    MICROSOFT_BRP("Microsoft BRP", "https://www.microsoft.com/msrc/bug-bounty"),
    APPLE_BRP("Apple BRP", "https://developer.apple.com/security-bounty"),
    GITHUB_BBP("GitHub BBP", "https://bounty.github.com"),
    TWITTER_BBP("Twitter BBP", "https://twitter.com/about/threat-intel"),
    LOCAL("Local", ""); // For self-hosted/listed programs
}

/**
 * Reward range for filtering
 */
enum class RewardRange(val displayName: String, val min: Int, val max: Int) {
    ANY("Any", 0, Integer.MAX_VALUE),
    LOW("< $500", 0, 499),
    MEDIUM("$500 - $1,000", 500, 1000),
    HIGH("$1,000 - $5,000", 1001, 5000),
    VERY_HIGH("$5,000 - $10,000", 5001, 10000),
    CRITICAL("> $10,000", 10001, Integer.MAX_VALUE)
}

/**
 * Sorting options for bounty search
 */
enum class SortOption(val displayName: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    HIGHEST_REWARD("Highest Reward"),
    LOWEST_REWARD("Lowest Reward"),
    MOST_SCOPES("Most Scopes"),
    ENDING_SOON("Ending Soon")
}

/**
 * Represents a bug bounty program
 */
data class BountyProgram(
    val id: String,
    val name: String,
    val platform: BountyPlatform,
    val url: String,
    val description: String?,
    val rewards: Rewards?,
    val scopes: List<Scope>,
    val outOfScopes: List<String>,
    val bountyType: List<BountyType>,
    val industry: List<Industry>,
    val minBounty: Int?,
    val maxBounty: Int?,
    val publishedAt: String?,
    val lastUpdated: String?,
    val isPrivate: Boolean = false,
    val isInScope: Boolean = true,
    val eligibility: String?,
    val disclosurePolicy: String?,
    val safeHarbor: Boolean = false,
    val savedAt: Long? = null,
    val isFavorite: Boolean = false
) {
    companion object {
        fun generateId(): String = "bounty_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}

/**
 * Reward information for a program
 */
data class Rewards(
    val min: Int?,
    val max: Int?,
    val currency: String = "USD",
    val details: String?
)

/**
 * Scope item (in-scope asset)
 */
data class Scope(
    val asset: String,
    val type: String,
    val description: String?,
    val isInScope: Boolean = true
)

/**
 * Search filters for bounty programs
 */
data class BountySearchFilters(
    val query: String = "",
    val types: List<BountyType> = emptyList(),
    val industries: List<Industry> = emptyList(),
    val platforms: List<BountyPlatform> = emptyList(),
    val rewardRange: RewardRange = RewardRange.ANY,
    val minReward: Int? = null,
    val maxReward: Int? = null,
    val sortBy: SortOption = SortOption.NEWEST,
    val onlyPrivate: Boolean = false,
    val onlyWithBounties: Boolean = false
)

/**
 * Result of a bounty search
 */
data class BountySearchResult(
    val programs: List<BountyProgram>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean
)
