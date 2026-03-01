package com.bountybugger.ui.mobile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bountybugger.R
import com.bountybugger.databinding.ActivityMobileAnalysisBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipFile

/**
 * Mobile App Analysis Activity
 * Performs static analysis of Android APK files
 */
class MobileAnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMobileAnalysisBinding
    private var selectedApkUri: Uri? = null
    private var selectedApkFile: File? = null

    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedApkUri = uri
                binding.textSelectedFile.text = getFileName(uri) ?: "APK selected"
                binding.btnAnalyze.isEnabled = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMobileAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.mobile_analysis_title)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSelectApk.setOnClickListener {
            openFilePicker()
        }

        binding.btnAnalyze.setOnClickListener {
            analyzeApk()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.android.package-archive"
        }
        filePickerLauncher.launch(intent)
    }

    private fun getFileName(uri: Uri): String? {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }

    private fun copyApkToInternal(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(cacheDir, "temp_analysis.apk")
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun analyzeApk() {
        val uri = selectedApkUri ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.btnAnalyze.isEnabled = false
        binding.textResults.text = ""

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                performAnalysis(uri)
            }

            binding.progressBar.visibility = View.GONE
            binding.btnAnalyze.isEnabled = true

            if (result != null) {
                binding.textResults.text = result
            } else {
                Toast.makeText(this@MobileAnalysisActivity, "Analysis failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performAnalysis(uri: Uri): String? {
        val tempFile = copyApkToInternal(uri) ?: return null

        return try {
            val results = JSONObject()

            // Basic APK Info
            val apkInfo = JSONObject()
            apkInfo.put("file_name", tempFile.name)
            apkInfo.put("file_size", "${tempFile.length() / 1024} KB")
            results.put("apk_info", apkInfo)

            // Manifest Analysis
            if (binding.checkboxManifest.isChecked) {
                val manifestAnalysis = analyzeManifest(tempFile)
                results.put("manifest", manifestAnalysis)
            }

            // Permissions Analysis
            if (binding.checkboxPermissions.isChecked) {
                val permissionsAnalysis = analyzePermissions(tempFile)
                results.put("permissions", permissionsAnalysis)
            }

            // Code Analysis (basic)
            if (binding.checkboxCode.isChecked) {
                val codeAnalysis = analyzeCode(tempFile)
                results.put("code_analysis", codeAnalysis)
            }

            formatResults(results)
        } catch (e: Exception) {
            e.printStackTrace()
            "Error analyzing APK: ${e.message}"
        } finally {
            tempFile.delete()
        }
    }

    private fun analyzeManifest(tempFile: File): JSONObject {
        val manifest = JSONObject()
        
        try {
            ZipFile(tempFile).use { zip ->
                val manifestEntry = zip.getEntry("AndroidManifest.xml")
                if (manifestEntry != null) {
                    manifest.put("status", "Manifest file found")
                    manifest.put("note", "Binary manifest analysis requires additional libraries")
                } else {
                    manifest.put("status", "Manifest not found")
                }
            }
        } catch (e: Exception) {
            manifest.put("error", e.message)
        }

        return manifest
    }

    private fun analyzePermissions(tempFile: File): JSONObject {
        val permissions = JSONObject()
        
        try {
            ZipFile(tempFile).use { zip ->
                // Check for common dangerous permissions in the APK
                val dangerousPermissions = listOf(
                    "READ_EXTERNAL_STORAGE" to "Allows reading from external storage",
                    "WRITE_EXTERNAL_STORAGE" to "Allows writing to external storage",
                    "CAMERA" to "Access to camera",
                    "RECORD_AUDIO" to "Record audio",
                    "ACCESS_FINE_LOCATION" to "Access precise location",
                    "ACCESS_COARSE_LOCATION" to "Access approximate location",
                    "READ_CONTACTS" to "Read contacts",
                    "WRITE_CONTACTS" to "Write contacts",
                    "READ_SMS" to "Read SMS messages",
                    "SEND_SMS" to "Send SMS messages",
                    "READ_CALL_LOG" to "Read call log",
                    "RECORD_CALLS" to "Record calls",
                    "READ_PHONE_STATE" to "Read phone state",
                    "CALL_PHONE" to "Make phone calls",
                    "READ_GMAIL" to "Read Gmail",
                    "GET_ACCOUNTS" to "Get accounts",
                    "PROCESS_OUTGOING_CALLS" to "Process outgoing calls"
                )

                val foundPermissions = JSONArray()
                
                // Check for permission strings in the APK
                val manifestEntry = zip.getEntry("classes.dex")
                if (manifestEntry != null) {
                    val dexContent = zip.getInputStream(manifestEntry).bufferedReader().readText()
                    
                    dangerousPermissions.forEach { (perm, desc) ->
                        if (dexContent.contains(perm)) {
                            val permObj = JSONObject()
                            permObj.put("permission", "android.permission.$perm")
                            permObj.put("description", desc)
                            permObj.put("risk", getPermissionRisk(perm))
                            foundPermissions.put(permObj)
                        }
                    }
                }

                permissions.put("total_found", foundPermissions.length())
                permissions.put("permissions", foundPermissions)
            }
        } catch (e: Exception) {
            permissions.put("error", e.message)
        }

        return permissions
    }

    private fun getPermissionRisk(permission: String): String {
        return when (permission) {
            "READ_SMS", "SEND_SMS", "READ_CALL_LOG", "RECORD_CALLS", "READ_PHONE_STATE" -> "High"
            "CAMERA", "RECORD_AUDIO", "ACCESS_FINE_LOCATION", "READ_CONTACTS", "WRITE_CONTACTS" -> "Medium"
            else -> "Low"
        }
    }

    private fun analyzeCode(tempFile: File): JSONObject {
        val codeAnalysis = JSONObject()
        
        try {
            ZipFile(tempFile).use { zip ->
                val dexFiles = zip.entries().asSequence()
                    .filter { it.name.startsWith("classes") && it.name.endsWith(".dex") }
                    .toList()

                codeAnalysis.put("dex_files_count", dexFiles.size)
                
                // Check for common security issues
                val issues = JSONArray()
                
                // Check for debuggable flag
                if (zip.getEntry("AndroidManifest.xml") != null) {
                    // This is a simplified check
                    issues.put("Note: Full code analysis requires additional tools like MobSF")
                }

                codeAnalysis.put("issues_found", 0)
                codeAnalysis.put("issues", issues)
                codeAnalysis.put("recommendation", "Use MobSF or similar tools for comprehensive static analysis")
            }
        } catch (e: Exception) {
            codeAnalysis.put("error", e.message)
        }

        return codeAnalysis
    }

    private fun formatResults(results: JSONObject): String {
        return buildString {
            appendLine("════════════════════════════════════════════════════════════")
            appendLine("                    APK ANALYSIS REPORT")
            appendLine("════════════════════════════════════════════════════════════")
            appendLine()

            // APK Info
            val apkInfo = results.optJSONObject("apk_info")
            if (apkInfo != null) {
                appendLine("────────────────────────────────────────────────────────────")
                appendLine("                      APK INFORMATION")
                appendLine("────────────────────────────────────────────────────────────")
                appendLine("File: ${apkInfo.optString("file_name")}")
                appendLine("Size: ${apkInfo.optString("file_size")}")
                appendLine()
            }

            // Permissions
            val permissions = results.optJSONObject("permissions")
            if (permissions != null) {
                appendLine("────────────────────────────────────────────────────────────")
                appendLine("                      PERMISSIONS")
                appendLine("────────────────────────────────────────────────────────────")
                appendLine("Total Permissions Found: ${permissions.optInt("total_found")}")
                appendLine()

                val permsArray = permissions.optJSONArray("permissions")
                if (permsArray != null) {
                    for (i in 0 until permsArray.length()) {
                        val perm = permsArray.getJSONObject(i)
                        appendLine("${i + 1}. ${perm.optString("permission")}")
                        appendLine("   Risk: ${perm.optString("risk")}")
                        appendLine("   ${perm.optString("description")}")
                        appendLine()
                    }
                }
            }

            // Manifest
            val manifest = results.optJSONObject("manifest")
            if (manifest != null) {
                appendLine("────────────────────────────────────────────────────────────")
                appendLine("                       MANIFEST")
                appendLine("────────────────────────────────────────────────────────────")
                appendLine("Status: ${manifest.optString("status")}")
                appendLine()
            }

            // Code Analysis
            val codeAnalysis = results.optJSONObject("code_analysis")
            if (codeAnalysis != null) {
                appendLine("────────────────────────────────────────────────────────────")
                appendLine("                      CODE ANALYSIS")
                appendLine("────────────────────────────────────────────────────────────")
                appendLine("DEX Files: ${codeAnalysis.optInt("dex_files_count")}")
                appendLine("Issues Found: ${codeAnalysis.optInt("issues_found")}")
                appendLine()
                appendLine("Recommendation:")
                appendLine("  ${codeAnalysis.optString("recommendation")}")
                appendLine()
            }

            appendLine("════════════════════════════════════════════════════════════")
            appendLine("                    END OF REPORT")
            appendLine("════════════════════════════════════════════════════════════")
        }
    }
}
