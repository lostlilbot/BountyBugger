package com.bountybugger.domain.model

/**
 * Represents a complete scan result
 */
data class ScanResult(
    val id: String,
    val scanType: ScanType,
    val target: String,
    val startTime: Long,
    val endTime: Long,
    val status: ScanStatus,
    val vulnerabilities: List<Vulnerability> = emptyList(),
    val networkResults: List<PortResult> = emptyList(),
    val mobileResults: MobileAnalysisResult? = null,
    val rawOutput: String? = null,
    val errorMessage: String? = null
) {
    val duration: Long get() = endTime - startTime

    val vulnerabilityCount: Int get() = vulnerabilities.size

    val averageCvssScore: Float
        get() = if (vulnerabilities.isEmpty()) 0f
                else vulnerabilities.map { it.cvssScore }.average().toFloat()

    val severityBreakdown: Map<Severity, Int>
        get() = vulnerabilities.groupBy { it.severity }.mapValues { it.value.size }

    companion object {
        fun generateId(): String = "scan_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}

/**
 * Types of scans supported by the app
 */
enum class ScanType(val displayName: String, val description: String) {
    NETWORK_SCAN("Network Scan", "Port scanning and service detection"),
    WEB_SCAN("Web Scan", "Web vulnerability assessment"),
    MOBILE_ANALYSIS("Mobile Analysis", "APK static and dynamic analysis"),
    FULL_SCAN("Full Scan", "Comprehensive security assessment"),
    API_SCAN("API Scan", "API endpoint testing"),
    RECON("Reconnaissance", "Information gathering")
}

/**
 * Status of a scan
 */
enum class ScanStatus(val displayName: String) {
    PENDING("Pending"),
    RUNNING("Running"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    PAUSED("Paused")
}

/**
 * Port scan result
 */
data class PortResult(
    val port: Int,
    val protocol: String = "tcp",
    val state: PortState,
    val service: String? = null,
    val serviceVersion: String? = null,
    val banner: String? = null
)

/**
 * Port states
 */
enum class PortState(val displayName: String) {
    OPEN("Open"),
    CLOSED("Closed"),
    FILTERED("Filtered"),
    UNKNOWN("Unknown")
}

/**
 * Mobile analysis result
 */
data class MobileAnalysisResult(
    val packageName: String,
    val appName: String,
    val versionName: String?,
    val versionCode: Int?,
    val permissions: List<PermissionInfo>,
    val dangerousPermissions: List<PermissionInfo>,
    val components: ComponentInfo,
    val securityFindings: List<Vulnerability>,
    val manifestAnalysis: String,
    val staticAnalysis: StaticAnalysisResult?,
    val dynamicAnalysis: DynamicAnalysisResult?
)

/**
 * Permission information
 */
data class PermissionInfo(
    val name: String,
    val isDangerous: Boolean,
    val description: String
)

/**
 * Component information (Activities, Services, BroadcastReceivers, etc.)
 */
data class ComponentInfo(
    val activities: List<String> = emptyList(),
    val services: List<String> = emptyList(),
    val broadcastReceivers: List<String> = emptyList(),
    val contentProviders: List<String> = emptyList()
)

/**
 * Static analysis results
 */
data class StaticAnalysisResult(
    val fileAnalysis: FileAnalysis,
    val codeAnalysis: List<CodeFinding>,
    val cryptoFindings: List<CryptoFinding>
)

/**
 * File analysis results
 */
data class FileAnalysis(
    val apkSize: Long,
    val certificateInfo: CertificateInfo?,
    val nativeLibraries: List<String> = emptyList(),
    val resources: List<String> = emptyList()
)

/**
 * Certificate information
 */
data class CertificateInfo(
    val issuer: String,
    val subject: String,
    val validFrom: Long,
    val validTo: Long,
    val isSigned: Boolean,
    val signatureAlgorithm: String
)

/**
 * Code finding from static analysis
 */
data class CodeFinding(
    val file: String,
    val line: Int,
    val finding: String,
    val severity: Severity,
    val description: String
)

/**
 * Cryptographic finding
 */
data class CryptoFinding(
    val type: String,
    val location: String,
    val description: String,
    val severity: Severity
)

/**
 * Dynamic analysis results
 */
data class DynamicAnalysisResult(
    val networkTraffic: List<NetworkTraffic> = emptyList(),
    val fileAccess: List<String> = emptyList(),
    val runtimeBehavior: List<String> = emptyList(),
    val suspiciousActivities: List<String> = emptyList()
)

/**
 * Network traffic captured during dynamic analysis
 */
data class NetworkTraffic(
    val url: String,
    val method: String,
    val requestHeaders: Map<String, String>,
    val responseCode: Int?,
    val timestamp: Long
)
