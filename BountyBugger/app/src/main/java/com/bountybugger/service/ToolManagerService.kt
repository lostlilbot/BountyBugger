package com.bountybugger.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
//import com.bountybugger.data.api.GitHubApiService
import com.bountybugger.domain.model.Tool
import com.bountybugger.domain.model.ToolCategory
import com.bountybugger.domain.model.ToolPlatform
import com.bountybugger.domain.model.ToolStatus
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Tool Manager Service
 * Handles searching, downloading, and managing pentest tools
 */
class ToolManagerService(private val context: Context) {

    private val gson = Gson()
    private val toolsFile: File = File(context.filesDir, "tools.json")
    private val downloadsDir: File = File(context.filesDir, "downloads").apply { mkdirs() }

    // Built-in tool recommendations
    private val builtinTools = listOf(
        Tool(
            id = "nmap",
            name = "Nmap",
            description = "Network mapper - port scanning and network discovery",
            category = ToolCategory.NETWORK,
            platform = ToolPlatform.TERMUX,
            repositoryUrl = "https://github.com/nmap/nmap",
            downloadUrl = null,
            stars = 25000,
            requiresTermux = true,
            dependencies = listOf("nmap")
        ),
        Tool(
            id = "sqlmap",
            name = "SQLMap",
            description = "Automatic SQL injection and database takeover tool",
            category = ToolCategory.WEB,
            platform = ToolPlatform.PYTHON,
            repositoryUrl = "https://github.com/sqlmapproject/sqlmap",
            stars = 28000,
            requiresTermux = true,
            dependencies = listOf("python", "sqlmap")
        ),
        Tool(
            id = "nikto",
            name = "Nikto",
            description = "Web server scanner for vulnerabilities",
            category = ToolCategory.WEB,
            platform = ToolPlatform.PYTHON,
            repositoryUrl = "https://github.com/sullo/nikto",
            stars = 5000,
            requiresTermux = true,
            dependencies = listOf("perl", "nikto")
        ),
        Tool(
            id = "metasploit",
            name = "Metasploit Framework",
            description = "Penetration testing and exploitation framework",
            category = ToolCategory.EXPLOITS,
            platform = ToolPlatform.TERMUX,
            repositoryUrl = "https://github.com/rapid7/metasploit-framework",
            stars = 30000,
            requiresTermux = true,
            dependencies = listOf("metasploit")
        ),
        Tool(
            id = "hydra",
            name = "Hydra",
            description = "Network login cracker",
            category = ToolCategory.PASSWORD,
            platform = ToolPlatform.TERMUX,
            repositoryUrl = "https://github.com/vanhauser-thc/thc-hydra",
            stars = 3000,
            requiresTermux = true,
            dependencies = listOf("hydra")
        ),
        Tool(
            id = "john",
            name = "John the Ripper",
            description = "Password cracker",
            category = ToolCategory.PASSWORD,
            platform = ToolPlatform.TERMUX,
            repositoryUrl = "https://github.com/openwall/john",
            stars = 5000,
            requiresTermux = true,
            dependencies = listOf("john")
        ),
        Tool(
            id = "aircrack",
            name = "Aircrack-ng",
            description = "Wireless network security assessment",
            category = ToolCategory.WIRELESS,
            platform = ToolPlatform.TERMUX,
            repositoryUrl = "https://github.com/aircrack-ng/aircrack-ng",
            stars = 3000,
            requiresTermux = true,
            dependencies = listOf("aircrack-ng")
        ),
        Tool(
            id = "drozer",
            name = "Drozer",
            description = "Android security assessment framework",
            category = ToolCategory.MOBILE,
            platform = ToolPlatform.PYTHON,
            repositoryUrl = "https://github.com/WithSecureLabs/drozer",
            stars = 2000,
            requiresTermux = true,
            dependencies = listOf("python2", "drozer")
        ),
        Tool(
            id = "mobsf",
            name = "MobSF",
            description = "Mobile Security Framework - APK analysis",
            category = ToolCategory.MOBILE,
            platform = ToolPlatform.PYTHON,
            repositoryUrl = "https://github.com/MobSF/Mobile-Security-Framework-MobSF",
            stars = 15000,
            requiresTermux = true,
            dependencies = listOf("python", "mobsf")
        ),
        Tool(
            id = "sublist3r",
            name = "Sublist3r",
            description = "Subdomain enumeration tool",
            category = ToolCategory.RECON,
            platform = ToolPlatform.PYTHON,
            repositoryUrl = "https://github.com/aboul3la/Sublist3r",
            stars = 7000,
            requiresTermux = true,
            dependencies = listOf("python", "sublist3r")
        ),
        Tool(
            id = "theHarvester",
            name = "theHarvester",
            description = "E-mail, subdomain and person finder",
            category = ToolCategory.RECON,
            platform = ToolPlatform.PYTHON,
            repositoryUrl = "https://github.com/laramies/theHarvester",
            stars = 4000,
            requiresTermux = true,
            dependencies = listOf("python", "theharvester")
        ),
        Tool(
            id = "wireshark",
            name = "Wireshark",
            description = "Network protocol analyzer",
            category = ToolCategory.NETWORK,
            platform = ToolPlatform.TERMUX,
            repositoryUrl = "https://github.com/wireshark/wireshark",
            stars = 1000,
            requiresTermux = true,
            dependencies = listOf("wireshark")
        ),
        Tool(
            id = "ettercap",
            name = "Ettercap",
            description = "Man-in-the-middle attack tool",
            category = ToolCategory.NETWORK,
            platform = ToolPlatform.TERMUX,
            repositoryUrl = "https://github.com/Ettercap/ettercap",
            stars = 1000,
            requiresTermux = true,
            dependencies = listOf("ettercap")
        ),
        Tool(
            id = "responder",
            name = "Responder",
            description = "LLMNR, NBT-NS and MDNS poisoner",
            category = ToolCategory.EXPLOITS,
            platform = ToolPlatform.PYTHON,
            repositoryUrl = "https://github.com/lgandx/Responder",
            stars = 4000,
            requiresTermux = true,
            dependencies = listOf("python", "responder")
        ),
        Tool(
            id = "dirb",
            name = "DIRB",
            description = "Web content scanner",
            category = ToolCategory.WEB,
            platform = ToolPlatform.TERMUX,
            repositoryUrl = "https://github.com/rachelsun/dirb",
            stars = 500,
            requiresTermux = true,
            dependencies = listOf("dirb")
        )
    )

    /**
     * Search for tools on GitHub - Disabled, returns built-in tools only
     */
    suspend fun searchGitHubTools(query: String): List<Tool> {
        // Return built-in tools if API fails
        return builtinTools.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
    }

    /**
     * Get all available tools (built-in + downloaded)
     */
    suspend fun getAllTools(): List<Tool> = withContext(Dispatchers.IO) {
        val downloadedTools = loadDownloadedTools()
        builtinTools + downloadedTools
    }

    /**
     * Get built-in tools
     */
    fun getBuiltinTools(): List<Tool> = builtinTools

    /**
     * Download a tool
     */
    suspend fun downloadTool(tool: Tool, onProgress: ((Float) -> Unit)? = null): Result<File> = withContext(Dispatchers.IO) {
        try {
            // For built-in tools, create an info file with installation instructions
            // since these are installed via Termux package manager
            val toolFile = File(downloadsDir, "${tool.id}_info.txt")
            toolFile.writeText("""
                Tool: ${tool.name}
                Repository: ${tool.repositoryUrl}
                Category: ${tool.category.displayName}
                Description: ${tool.description}
                Platform: ${tool.platform.name}
                
                ═════════════════════════════════════════════════════════════
                INSTALLATION INSTRUCTIONS
                ═════════════════════════════════════════════════════════════
                
                This tool can be installed via Termux. Follow these steps:
                
                1. Install Termux from Play Store or F-Droid
                2. Open Termux and run:
                
                   pkg update
                   pkg install ${tool.dependencies.joinToString(" ")}
                
                Or for GitHub installation:
                
                   pkg install git python
                   git clone ${tool.repositoryUrl}
                   cd ${tool.id}
                   pip install -r requirements.txt
                
                Note: Some tools may require additional dependencies.
                Check the tool's GitHub repository for details.
            """.trimIndent())

            onProgress?.invoke(1f)
            Result.success(toolFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Install tool via Termux
     */
    fun installViaTermux(tool: Tool): Intent? {
        if (!isTermuxInstalled()) {
            return null
        }

        val packageName = getToolPackageName(tool)
        return createTermuxIntent("pkg install -y $packageName")
    }

    /**
     * Check if Termux is installed
     */
    fun isTermuxInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.termux", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Get Termux play store URL
     */
    fun getTermuxInstallIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.termux"))
    }

    /**
     * Create Termux intent
     */
    private fun createTermuxIntent(command: String): Intent {
        return Intent("com.termux.RUN_COMMAND").apply {
            putExtra("com.termux.RUN_COMMAND_COMMAND", command)
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)
            putExtra("com.termux.RUN_COMMAND_SESSION", "new")
        }
    }

    /**
     * Categorize tool based on topics and description
     */
    private fun categorizeTool(topics: List<String>, description: String): ToolCategory {
        val text = (topics + description.lowercase()).joinToString(" ")

        return when {
            text.contains("network") || text.contains("port") || text.contains("scan") -> ToolCategory.NETWORK
            text.contains("web") || text.contains("http") || text.contains("scan") -> ToolCategory.WEB
            text.contains("mobile") || text.contains("android") || text.contains("apk") -> ToolCategory.MOBILE
            text.contains("exploit") || text.contains("payload") || text.contains(" metasploit") -> ToolCategory.EXPLOITS
            text.contains("password") || text.contains("crack") || text.contains("hash") -> ToolCategory.PASSWORD
            text.contains("wireless") || text.contains("wifi") || text.contains("802.11") -> ToolCategory.WIRELESS
            text.contains("recon") || text.contains("enum") || text.contains("subdomain") -> ToolCategory.RECON
            text.contains("forensic") || text.contains("malware") || text.contains("analy") -> ToolCategory.FORENSICS
            else -> ToolCategory.UTILITY
        }
    }

    /**
     * Determine platform based on language
     */
    private fun determinePlatform(language: String?): ToolPlatform {
        return when (language?.lowercase()) {
            "python" -> ToolPlatform.PYTHON
            "ruby" -> ToolPlatform.RUBY
            "c", "c++" -> ToolPlatform.BINARY
            "shell" -> ToolPlatform.TERMUX
            else -> ToolPlatform.CROSS_PLATFORM
        }
    }

    /**
     * Get Termux package name for tool
     */
    private fun getToolPackageName(tool: Tool): String {
        return when (tool.id) {
            "nmap" -> "nmap"
            "sqlmap" -> "sqlmap"
            "nikto" -> "nikto"
            "metasploit" -> "metasploit"
            "hydra" -> "hydra"
            "john" -> "john"
            "aircrack" -> "aircrack-ng"
            "wireshark" -> "wireshark"
            "ettercap" -> "ettercap"
            "responder" -> "responder"
            "dirb" -> "dirb"
            else -> tool.id.lowercase()
        }
    }

    /**
     * Load downloaded tools from storage
     */
    private fun loadDownloadedTools(): List<Tool> {
        return try {
            if (toolsFile.exists()) {
                val json = toolsFile.readText()
                gson.fromJson(json, Array<Tool>::class.java).toList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Save downloaded tool to storage
     */
    private fun saveDownloadedTool(tool: Tool) {
        try {
            val tools = loadDownloadedTools().toMutableList()
            tools.add(tool)
            toolsFile.writeText(gson.toJson(tools))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        // GitHub API disabled - requires GitHubApiService
        /* private val githubApi: GitHubApiService by lazy {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Accept", "application/vnd.github.v3+json")
                        .build()
                    chain.proceed(request)
                }
                .build()

            Retrofit.Builder()
                .baseUrl(GitHubApiService.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GitHubApiService::class.java)
        } */
    }
}
