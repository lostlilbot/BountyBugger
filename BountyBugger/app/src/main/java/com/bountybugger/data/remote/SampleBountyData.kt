package com.bountybugger.data.remote

import com.bountybugger.domain.model.*

/**
 * Sample bounty program data for popular bug bounty platforms.
 * This provides a working data source without requiring API authentication.
 */
object SampleBountyData {

    fun getSamplePrograms(): List<BountyProgram> = listOf(
        // Google VRP
        BountyProgram(
            id = "google_vrp_001",
            name = "Google VRP",
            platform = BountyPlatform.GOOGLE_VRP,
            url = "https://bughunter.google.com",
            description = "Google Vulnerability Reward Program covering all Google products, services, and websites.",
            rewards = Rewards(100, 31337, "USD", "Up to \$313,337 for critical vulnerabilities"),
            scopes = listOf(
                Scope("*.google.com", "Web Application", "All Google web properties", true),
                Scope("*.android.com", "Mobile", "Android applications", true),
                Scope("cloud.google.com", "Cloud", "Google Cloud Platform", true)
            ),
            outOfScopes = listOf("Denial of service attacks", "Social engineering", "Physical security"),
            bountyType = listOf(BountyType.WEB, BountyType.MOBILE, BountyType.API),
            industry = listOf(Industry.TECHNOLOGY),
            minBounty = 100,
            maxBounty = 31337,
            publishedAt = "2010-11-23",
            lastUpdated = "2024-01-15",
            isPrivate = false,
            safeHarbor = true,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow coordinated disclosure guidelines"
        ),

        // Meta BBP
        BountyProgram(
            id = "meta_bbp_001",
            name = "Meta BBP",
            platform = BountyPlatform.META_BBP,
            url = "https://www.facebook.com/bugbounty",
            description = "Meta Bug Bounty Program for Facebook, Instagram, WhatsApp, and Oculus.",
            rewards = Rewards(500, 50000, "USD", "Rewards based on severity and impact"),
            scopes = listOf(
                Scope("*.facebook.com", "Web Application", "Facebook main platform", true),
                Scope("*.instagram.com", "Web Application", "Instagram", true),
                Scope("*.whatsapp.com", "Web Application", "WhatsApp", true)
            ),
            outOfScopes = listOf("Denial of service", "Spam", "Social engineering"),
            bountyType = listOf(BountyType.WEB, BountyType.API, BountyType.MOBILE),
            industry = listOf(Industry.SOCIAL_MEDIA, Industry.TECHNOLOGY),
            minBounty = 500,
            maxBounty = 50000,
            publishedAt = "2011-02-01",
            lastUpdated = "2024-02-20",
            isPrivate = false,
            safeHarbor = true,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow Meta's disclosure guidelines"
        ),

        // Microsoft BRP
        BountyProgram(
            id = "microsoft_brp_001",
            name = "Microsoft BRP",
            platform = BountyPlatform.MICROSOFT_BRP,
            url = "https://www.microsoft.com/msrc/bug-bounty",
            description = "Microsoft Bug Bounty Program covering Windows, Azure, Office 365, and other Microsoft products.",
            rewards = Rewards(500, 100000, "USD", "Up to \$100,000 for critical vulnerabilities"),
            scopes = listOf(
                Scope("*.microsoft.com", "Web Application", "Microsoft web properties", true),
                Scope("*.windows.com", "Operating System", "Windows OS", true),
                Scope("azure.microsoft.com", "Cloud", "Azure Cloud Platform", true)
            ),
            outOfScopes = listOf("DOS attacks", "Physical violence", "Bribery"),
            bountyType = listOf(BountyType.WEB, BountyType.API, BountyType.CLOUD),
            industry = listOf(Industry.TECHNOLOGY, Industry.CLOUD),
            minBounty = 500,
            maxBounty = 100000,
            publishedAt = "2014-09-26",
            lastUpdated = "2024-03-01",
            isPrivate = false,
            safeHarbor = true,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow MSRC disclosure policy"
        ),

        // Apple BBP
        BountyProgram(
            id = "apple_brp_001",
            name = "Apple BBP",
            platform = BountyPlatform.APPLE_BRP,
            url = "https://developer.apple.com/security-bounty",
            description = "Apple Security Bounty Program for iOS, macOS, watchOS, and other Apple products.",
            rewards = Rewards(5000, 1000000, "USD", "Up to \$1,000,000 for critical iOS kernel vulnerabilities"),
            scopes = listOf(
                Scope("iOS", "Mobile", "iOS operating system", true),
                Scope("macOS", "Operating System", "macOS", true),
                Scope("Safari", "Browser", "Safari browser", true)
            ),
            outOfScopes = listOf("DoS", "Physical damage", "Social engineering"),
            bountyType = listOf(BountyType.MOBILE, BountyType.WEB),
            industry = listOf(Industry.TECHNOLOGY),
            minBounty = 5000,
            maxBounty = 1000000,
            publishedAt = "2016-08-15",
            lastUpdated = "2024-01-10",
            isPrivate = false,
            safeHarbor = true,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow Apple security guidelines"
        ),

        // GitHub BBP
        BountyProgram(
            id = "github_bbp_001",
            name = "GitHub BBP",
            platform = BountyPlatform.GITHUB_BBP,
            url = "https://bounty.github.com",
            description = "GitHub Security Bug Bounty Program covering GitHub.com and GitHub Enterprise.",
            rewards = Rewards(500, 20000, "USD", "Rewards based on severity"),
            scopes = listOf(
                Scope("github.com", "Web Application", "GitHub.com", true),
                Scope("githubenterprise.com", "Web Application", "GitHub Enterprise", true),
                Scope("github.io", "Web Application", "GitHub Pages", true)
            ),
            outOfScopes = listOf("Denial of service", "Brute force", "Social engineering"),
            bountyType = listOf(BountyType.WEB, BountyType.API),
            industry = listOf(Industry.TECHNOLOGY),
            minBounty = 500,
            maxBounty = 20000,
            publishedAt = "2014-01-15",
            lastUpdated = "2024-02-01",
            isPrivate = false,
            safeHarbor = true,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow GitHub disclosure policy"
        ),

        // HackerOne - Twitter
        BountyProgram(
            id = "hackerone_twitter_001",
            name = "Twitter",
            platform = BountyPlatform.HACKERONE,
            url = "https://hackerone.com/twitter",
            description = "Twitter Bug Bounty Program via HackerOne covering all Twitter properties.",
            rewards = Rewards(500, 15000, "USD", "Up to \$15,000 for critical issues"),
            scopes = listOf(
                Scope("twitter.com", "Web Application", "Twitter main platform", true),
                Scope("*.twitter.com", "Web Application", "Twitter subdomains", true),
                Scope("mobile.twitter.com", "Mobile", "Twitter mobile", true)
            ),
            outOfScopes = listOf("Spam", "Self-XSS", "Social engineering"),
            bountyType = listOf(BountyType.WEB, BountyType.API, BountyType.MOBILE),
            industry = listOf(Industry.SOCIAL_MEDIA),
            minBounty = 500,
            maxBounty = 15000,
            publishedAt = "2014-05-01",
            lastUpdated = "2024-01-20",
            isPrivate = false,
            safeHarbor = true,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow coordinated disclosure"
        ),

        // Bugcrowd - Tesla
        BountyProgram(
            id = "bugcrowd_tesla_001",
            name = "Tesla",
            platform = BountyPlatform.BUGCROWD,
            url = "https://bugcrowd.com/tesla",
            description = "Tesla Bug Bounty Program covering Tesla.com, vehicle software, and energy products.",
            rewards = Rewards(1000, 15000, "USD", "Rewards up to \$15,000 for critical vehicle vulnerabilities"),
            scopes = listOf(
                Scope("tesla.com", "Web Application", "Tesla website", true),
                Scope("*.teslamotors.com", "Web Application", "Tesla domains", true),
                Scope("Tesla Mobile App", "Mobile", "Tesla iOS/Android apps", true)
            ),
            outOfScopes = listOf("Physical damage to vehicles", "Denial of service"),
            bountyType = listOf(BountyType.WEB, BountyType.MOBILE, BountyType.IoT),
            industry = listOf(Industry.TECHNOLOGY),
            minBounty = 1000,
            maxBounty = 15000,
            publishedAt = "2020-01-01",
            lastUpdated = "2024-02-15",
            isPrivate = false,
            safeHarbor = true,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow coordinated disclosure"
        ),

        // Intigriti - Uber
        BountyProgram(
            id = "intigriti_uber_001",
            name = "Uber",
            platform = BountyPlatform.INTIGRITI,
            url = "https://intigriti.com/uber",
            description = "Uber Bug Bounty Program covering Uber apps and infrastructure.",
            rewards = Rewards(500, 20000, "USD", "Up to \$20,000 for critical issues"),
            scopes = listOf(
                Scope("uber.com", "Web Application", "Uber main site", true),
                Scope("*.uber.com", "Web Application", "Uber subdomains", true),
                Scope("Uber App", "Mobile", "iOS/Android apps", true)
            ),
            outOfScopes = listOf("Account takeover without impact", "Denial of service"),
            bountyType = listOf(BountyType.WEB, BountyType.MOBILE, BountyType.API),
            industry = listOf(Industry.TRANSPORTATION),
            minBounty = 500,
            maxBounty = 20000,
            publishedAt = "2018-03-15",
            lastUpdated = "2024-01-05",
            isPrivate = false,
            safeHarbor = true,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow Uber disclosure guidelines"
        ),

        // Immunefi - DeFi
        BountyProgram(
            id = "immunefi_chainlink_001",
            name = "Chainlink",
            platform = BountyPlatform.IMMUNEFI,
            url = "https://immunefi.com/chainlink/",
            description = "Chainlink Bug Bounty Program for smart contracts and blockchain infrastructure.",
            rewards = Rewards(2000, 350000, "USD", "Up to \$350,000 for critical smart contract bugs"),
            scopes = listOf(
                Scope("Smart Contracts", "Blockchain", "Chainlink smart contracts", true),
                Scope("Proxy Contracts", "Blockchain", "Proxy implementations", true)
            ),
            outOfScopes = listOf("Spam", "UI/UX issues", "Theoretical vulnerabilities"),
            bountyType = listOf(BountyType.BLOCKCHAIN),
            industry = listOf(Industry.BLOCKCHAIN, Industry.CRYPTO),
            minBounty = 2000,
            maxBounty = 350000,
            publishedAt = "2020-09-15",
            lastUpdated = "2024-02-28",
            isPrivate = false,
            safeHarbor = false,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow Immunefi disclosure guidelines"
        ),

        // Open Bug Bounty
        BountyProgram(
            id = "openbugbounty_001",
            name = "Open Bug Bounty",
            platform = BountyPlatform.OPEN_BUG_BOUNTY,
            url = "https://openbugbounty.org",
            description = "Platform for reporting vulnerabilities in non-bug bounty programs. Free and open source.",
            rewards = null,
            scopes = listOf(
                Scope("Various", "Web Application", "Any website accepting responsible disclosure", true)
            ),
            outOfScopes = listOf("DOS", "Physical attacks", "Social engineering"),
            bountyType = listOf(BountyType.WEB),
            industry = listOf(Industry.OTHER),
            minBounty = null,
            maxBounty = null,
            publishedAt = "2015-01-01",
            lastUpdated = "2024-03-01",
            isPrivate = false,
            safeHarbor = false,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow OBB disclosure guidelines"
        ),

        // HackerOne - Shopify
        BountyProgram(
            id = "hackerone_shopify_001",
            name = "Shopify",
            platform = BountyPlatform.HACKERONE,
            url = "https://hackerone.com/shopify",
            description = "Shopify Bug Bounty Program covering all Shopify e-commerce platforms.",
            rewards = Rewards(900, 30000, "USD", "Up to \$30,000 for critical issues"),
            scopes = listOf(
                Scope("*.shopify.com", "Web Application", "Shopify domains", true),
                Scope("*.myshopify.com", "Web Application", "Merchant stores", true),
                Scope("Shopify App", "Mobile", "iOS/Android", true)
            ),
            outOfScopes = listOf("DoS", "Social engineering", "Brute force"),
            bountyType = listOf(BountyType.WEB, BountyType.API, BountyType.MOBILE),
            industry = listOf(Industry.ECOMMERCE),
            minBounty = 900,
            maxBounty = 30000,
            publishedAt = "2017-03-01",
            lastUpdated = "2024-01-25",
            isPrivate = false,
            safeHarbor = true,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow Shopify disclosure policy"
        ),

        // HackerOne - Airbnb
        BountyProgram(
            id = "hackerone_airbnb_001",
            name = "Airbnb",
            platform = BountyPlatform.HACKERONE,
            url = "https://hackerone.com/airbnb",
            description = "Airbnb Bug Bounty Program covering Airbnb.com and mobile apps.",
            rewards = Rewards(500, 20000, "USD", "Up to \$20,000 for critical vulnerabilities"),
            scopes = listOf(
                Scope("airbnb.com", "Web Application", "Airbnb main site", true),
                Scope("*.airbnb.com", "Web Application", "Airbnb subdomains", true),
                Scope("Airbnb App", "Mobile", "Mobile applications", true)
            ),
            outOfScopes = listOf("Self-XSS", "Spam", "Social engineering"),
            bountyType = listOf(BountyType.WEB, BountyType.API, BountyType.MOBILE),
            industry = listOf(Industry.ECOMMERCE),
            minBounty = 500,
            maxBounty = 20000,
            publishedAt = "2016-07-01",
            lastUpdated = "2024-02-10",
            isPrivate = false,
            safeHarbor = true,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow coordinated disclosure"
        ),

        // Bugcrowd - Stripe
        BountyProgram(
            id = "bugcrowd_stripe_001",
            name = "Stripe",
            platform = BountyPlatform.BUGCROWD,
            url = "https://bugcrowd.com/stripe",
            description = "Stripe Bug Bounty Program for payment security vulnerabilities.",
            rewards = Rewards(1000, 50000, "USD", "Up to \$50,000 for critical payment vulnerabilities"),
            scopes = listOf(
                Scope("stripe.com", "Web Application", "Stripe main site", true),
                Scope("api.stripe.com", "API", "Stripe API", true),
                Scope("Stripe App", "Mobile", "Mobile SDK", true)
            ),
            outOfScopes = listOf("DoS", "Social engineering", "Physical security"),
            bountyType = listOf(BountyType.WEB, BountyType.API, BountyType.MOBILE),
            industry = listOf(Industry.FINANCE),
            minBounty = 1000,
            maxBounty = 50000,
            publishedAt = "2015-09-01",
            lastUpdated = "2024-01-30",
            isPrivate = false,
            safeHarbor = true,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow Stripe disclosure guidelines"
        ),

        // HackerOne - Slack
        BountyProgram(
            id = "hackerone_slack_001",
            name = "Slack",
            platform = BountyPlatform.HACKERONE,
            url = "https://hackerone.com/slack",
            description = "Slack Security Bug Bounty Program.",
            rewards = Rewards(500, 15000, "USD", "Up to \$15,000 for critical issues"),
            scopes = listOf(
                Scope("slack.com", "Web Application", "Slack web app", true),
                Scope("*.slack.com", "Web Application", "Slack subdomains", true),
                Scope("Slack App", "Mobile", "Desktop/mobile apps", true)
            ),
            outOfScopes = listOf("DoS", "Spam", "Social engineering"),
            bountyType = listOf(BountyType.WEB, BountyType.API, BountyType.MOBILE),
            industry = listOf(Industry.TECHNOLOGY, Industry.SOCIAL_MEDIA),
            minBounty = 500,
            maxBounty = 15000,
            publishedAt = "2015-11-01",
            lastUpdated = "2024-02-05",
            isPrivate = false,
            safeHarbor = true,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow Slack disclosure guidelines"
        ),

        // YesWeHack - WordPress
        BountyProgram(
            id = "yeswehack_wordpress_001",
            name = "WordPress",
            platform = BountyPlatform.YESWEHACK,
            url = "https://yeswehack.com/programs/wordpress",
            description = "WordPress Security Bug Bounty Program.",
            rewards = Rewards(100, 3000, "USD", "Up to \$3,000 for critical core vulnerabilities"),
            scopes = listOf(
                Scope("wordpress.org", "Web Application", "WordPress.org", true),
                Scope("*.wordpress.org", "Web Application", "WordPress subdomains", true)
            ),
            outOfScopes = listOf("DoS", "Social engineering", "WPScan"),
            bountyType = listOf(BountyType.WEB),
            industry = listOf(Industry.TECHNOLOGY),
            minBounty = 100,
            maxBounty = 3000,
            publishedAt = "2017-01-01",
            lastUpdated = "2024-01-15",
            isPrivate = false,
            safeHarbor = true,
            savedAt = System.currentTimeMillis(),
            eligibility = "Open to all security researchers",
            disclosurePolicy = "Follow WordPress security guidelines"
        )
    )

    /**
     * Filter programs based on search filters
     */
    fun filterPrograms(programs: List<BountyProgram>, filters: BountySearchFilters): List<BountyProgram> {
        var filtered = programs

        // Text search
        if (filters.query.isNotBlank()) {
            val query = filters.query.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(query) ||
                it.description?.lowercase()?.contains(query) == true ||
                it.platform.displayName.lowercase().contains(query)
            }
        }

        // Filter by types
        if (filters.types.isNotEmpty()) {
            filtered = filtered.filter { program ->
                program.bountyType.any { it in filters.types }
            }
        }

        // Filter by industries
        if (filters.industries.isNotEmpty()) {
            filtered = filtered.filter { program ->
                program.industry.any { it in filters.industries }
            }
        }

        // Filter by platforms
        if (filters.platforms.isNotEmpty()) {
            filtered = filtered.filter { it.platform in filters.platforms }
        }

        // Filter by reward range
        if (filters.rewardRange != RewardRange.ANY) {
            filtered = filtered.filter { program ->
                val min = program.minBounty ?: 0
                val max = program.maxBounty ?: 0
                min in filters.rewardRange.min..filters.rewardRange.max ||
                max in filters.rewardRange.min..filters.rewardRange.max ||
                (min <= filters.rewardRange.max && max >= filters.rewardRange.min)
            }
        }

        // Filter by min/max reward
        filters.minReward?.let { min ->
            filtered = filtered.filter { (it.minBounty ?: 0) >= min }
        }
        filters.maxReward?.let { max ->
            filtered = filtered.filter { (it.maxBounty ?: 0) <= max }
        }

        // Filter private only
        if (filters.onlyPrivate) {
            filtered = filtered.filter { it.isPrivate }
        }

        // Filter with bounties only
        if (filters.onlyWithBounties) {
            filtered = filtered.filter { it.minBounty != null && it.minBounty > 0 }
        }

        // Sort
        filtered = when (filters.sortBy) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.publishedAt ?: "" }
            SortOption.OLDEST -> filtered.sortedBy { it.publishedAt ?: "" }
            SortOption.HIGHEST_REWARD -> filtered.sortedByDescending { it.maxBounty ?: 0 }
            SortOption.LOWEST_REWARD -> filtered.sortedBy { it.minBounty ?: 0 }
            SortOption.MOST_SCOPES -> filtered.sortedByDescending { it.scopes.size }
            SortOption.ENDING_SOON -> filtered.sortedBy { it.lastUpdated ?: "" }
        }

        return filtered
    }
}
