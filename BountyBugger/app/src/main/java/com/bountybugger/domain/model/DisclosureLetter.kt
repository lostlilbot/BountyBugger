package com.bountybugger.domain.model

/**
 * Template types for vulnerability disclosure letters
 */
enum class DisclosureTemplate(val displayName: String, val description: String) {
    BASIC_BOUNTY(
        "Basic Bug Bounty",
        "Simple template for bug bounty submissions"
    ),
    DETAILED_TECHNICAL(
        "Detailed Technical Report",
        "Comprehensive technical report with full PoC"
    ),
    VDP_POLICY(
        "Vulnerability Disclosure Policy",
        "Template for VDP submissions"
    ),
    CREDENTIAL_LEAK(
        "Credential Leak Report",
        "Specialized template for credential exposures"
    ),
    API_SECURITY(
        "API Security Report",
        "Template for API vulnerability findings"
    ),
    XSS_REPORT(
        "XSS Vulnerability Report",
        "Template for cross-site scripting findings"
    ),
    SQL_INJECTION(
        "SQL Injection Report",
        "Template for SQL injection findings"
    ),
    MOBILE_SECURITY(
        "Mobile Security Report",
        "Template for mobile app vulnerabilities"
    ),
    NETWORK_FINDING(
        "Network Finding Report",
        "Template for network scanning results"
    )
}

/**
 * Export format options
 */
enum class ExportFormat(val displayName: String, val extension: String) {
    PDF("PDF Document", "pdf"),
    JSON("JSON Data", "json"),
    MARKDOWN("Markdown", "md"),
    EMAIL("Email Draft", "txt")
}

/**
 * Represents a vulnerability disclosure letter/report
 */
data class DisclosureLetter(
    val id: String,
    val title: String,
    val template: DisclosureTemplate,
    val summary: String,
    val description: String,
    val vulnerableUrl: String,
    val affectedEndpoint: String?,
    val stepsToReproduce: List<String>,
    val proofOfConcept: String,
    val impact: String,
    val severity: Severity,
    val cvssScore: Float,
    val cvssVector: String?,
    val suggestedFix: String,
    val environmentDetails: String,
    val attachments: List<String> = emptyList(),
    val timeline: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val programName: String? = null,
    val programUrl: String? = null,
    val researcherName: String = "Anonymous",
    val researcherContact: String? = null
) {
    companion object {
        fun generateId(): String = "disclosure_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}

/**
 * CVSS Calculator result
 */
data class CVSSResult(
    val score: Float,
    val vector: String,
    val severity: Severity,
    val metrics: CVSSMetrics
)

/**
 * CVSS metrics for calculation
 */
data class CVSSMetrics(
    val attackVector: String = "N", // N, A, L, P
    val attackComplexity: String = "L", // L, H
    val privilegesRequired: String = "N", // N, L, H
    val userInteraction: String = "N", // N, R
    val scope: String = "U", // U, C
    val confidentiality: String = "H", // N, L, H
    val integrity: String = "N", // N, L, H
    val availability: String = "N" // N, L, H
) {
    fun toVectorString(): String {
        return "CVSS:3.1/AV:$attackVector/AC:$attackComplexity/PR:$privilegesRequired/UI:$userInteraction/S:$scope/C:$confidentiality/I:$integrity/A:$availability"
    }
}

/**
 * OWASP Risk Rating
 */
data class OWASPRiskRating(
    val overallSeverity: Severity,
    val likelihood: String,
    val technicalImpact: String,
    val businessImpact: String
)
