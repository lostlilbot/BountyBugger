package com.bountybugger.domain.model

/**
 * Categories for pentest tools
 */
enum class ToolCategory(val displayName: String) {
    NETWORK("Network"),
    WEB("Web"),
    MOBILE("Mobile"),
    EXPLOITS("Exploits"),
    RECON("Reconnaissance"),
    PASSWORD("Password"),
    WIRELESS("Wireless"),
    FORENSICS("Forensics"),
    UTILITY("Utility"),
    OTHER("Other")
}

/**
 * Tool platform requirements
 */
enum class ToolPlatform(val displayName: String) {
    ANDROID("Android"),
    TERMUX("Termux"),
    LINUX("Linux"),
    WINDOWS("Windows"),
    CROSS_PLATFORM("Cross-Platform"),
    PYTHON("Python"),
    RUBY("Ruby"),
    BINARY("Binary")
}

/**
 * Status of a tool
 */
enum class ToolStatus(val displayName: String) {
    AVAILABLE("Available"),
    DOWNLOADED("Downloaded"),
    INSTALLED("Installed"),
    RUNNING("Running"),
    ERROR("Error")
}

/**
 * Represents a security/penetration testing tool
 */
data class Tool(
    val id: String,
    val name: String,
    val description: String,
    val category: ToolCategory,
    val platform: ToolPlatform,
    val repositoryUrl: String,
    val downloadUrl: String? = null,
    val stars: Int = 0,
    val forks: Int = 0,
    val author: String? = null,
    val lastUpdated: String? = null,
    val license: String? = null,
    val tags: List<String> = emptyList(),
    val status: ToolStatus = ToolStatus.AVAILABLE,
    val installedPath: String? = null,
    val version: String? = null,
    val requiresTermux: Boolean = false,
    val dependencies: List<String> = emptyList(),
    val readmeContent: String? = null
) {
    companion object {
        fun generateId(): String = "tool_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}

/**
 * GitHub search result item
 */
data class GitHubSearchResult(
    val totalCount: Int,
    val items: List<GitHubToolItem>
)

/**
 * GitHub repository item
 */
data class GitHubToolItem(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val htmlUrl: String,
    val cloneUrl: String,
    val stargazersCount: Int,
    val forksCount: Int,
    val language: String?,
    val topics: List<String>,
    val updatedAt: String,
    val owner: GitHubOwner,
    val license: GitHubLicense?
)

/**
 * GitHub owner
 */
data class GitHubOwner(
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String
)

/**
 * GitHub license
 */
data class GitHubLicense(
    val key: String,
    val name: String,
    val spdxId: String
)

/**
 * Installed tool information
 */
data class InstalledTool(
    val toolId: String,
    val name: String,
    val installPath: String,
    val installDate: Long,
    val version: String?,
    val lastUsed: Long? = null,
    val isExecutable: Boolean = false,
    val output: String? = null
)

/**
 * Tool execution result
 */
data class ToolExecutionResult(
    val toolId: String,
    val command: String,
    val exitCode: Int,
    val output: String,
    val errorOutput: String,
    val startTime: Long,
    val endTime: Long
) {
    val isSuccess: Boolean get() = exitCode == 0
    val duration: Long get() = endTime - startTime
}
