package com.bountybugger.service

import android.content.Context
import android.os.Environment
import com.bountybugger.domain.model.Severity
import com.bountybugger.domain.model.Vulnerability
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Report Generator Service - Generates vulnerability reports in PDF and JSON format
 */
class ReportGenerator(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private val reportsDir: File
        get() {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "reports")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    /**
     * Generate a vulnerability report from scan results
     */
    fun generateReport(
        targetUrl: String,
        scanType: String,
        vulnerabilities: List<Vulnerability>,
        portResults: Map<Int, String>? = null
    ): File? {
        return try {
            val timestamp = dateFormat.format(Date())
            val fileName = "report_${scanType}_$timestamp"

            // Generate JSON report
            val jsonFile = generateJsonReport(fileName, targetUrl, scanType, vulnerabilities, portResults)

            // Generate text report (simplified - PDF requires additional library setup)
            val textFile = generateTextReport(fileName, targetUrl, scanType, vulnerabilities, portResults)

            jsonFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate JSON format report
     */
    private fun generateJsonReport(
        fileName: String,
        targetUrl: String,
        scanType: String,
        vulnerabilities: List<Vulnerability>,
        portResults: Map<Int, String>?
    ): File {
        val jsonFile = File(reportsDir, "$fileName.json")

        val jsonObject = JSONObject()
        jsonObject.put("report_id", UUID.randomUUID().toString())
        jsonObject.put("generated_at", displayDateFormat.format(Date()))
        jsonObject.put("target", targetUrl)
        jsonObject.put("scan_type", scanType)

        // Summary
        val summary = JSONObject()
        summary.put("total_vulnerabilities", vulnerabilities.size)
        summary.put("critical", vulnerabilities.count { it.severity == Severity.CRITICAL })
        summary.put("high", vulnerabilities.count { it.severity == Severity.HIGH })
        summary.put("medium", vulnerabilities.count { it.severity == Severity.MEDIUM })
        summary.put("low", vulnerabilities.count { it.severity == Severity.LOW })
        summary.put("info", vulnerabilities.count { it.severity == Severity.INFO })
        jsonObject.put("summary", summary)

        // Vulnerabilities
        val vulnArray = JSONArray()
        vulnerabilities.forEach { vuln ->
            val vulnJson = JSONObject()
            vulnJson.put("name", vuln.name)
            vulnJson.put("severity", vuln.severity.displayName)
            vulnJson.put("description", vuln.description)
            vulnJson.put("location", vuln.affectedUrl)
            vulnJson.put("cvss_score", vuln.cvssScore)
            vulnJson.put("cve_id", vuln.cveId ?: "N/A")
            vulnJson.put("impact", vuln.impact)
            vulnJson.put("remediation", vuln.suggestedFix)
            vulnJson.put("references", JSONArray(vuln.references))
            vulnArray.put(vulnJson)
        }
        jsonObject.put("vulnerabilities", vulnArray)

        // Port scan results
        if (!portResults.isNullOrEmpty()) {
            val portJson = JSONObject()
            portResults.forEach { (port, service) ->
                portJson.put(port.toString(), service ?: "unknown")
            }
            jsonObject.put("open_ports", portJson)
        }

        // Write to file
        FileWriter(jsonFile).use { writer ->
            writer.write(jsonObject.toString(2))
        }

        return jsonFile
    }

    /**
     * Generate text format report
     */
    private fun generateTextReport(
        fileName: String,
        targetUrl: String,
        scanType: String,
        vulnerabilities: List<Vulnerability>,
        portResults: Map<Int, String>?
    ): File {
        val textFile = File(reportsDir, "$fileName.txt")

        val content = buildString {
            appendLine("════════════════════════════════════════════════════════════")
            appendLine("                    VULNERABILITY REPORT")
            appendLine("════════════════════════════════════════════════════════════")
            appendLine()
            appendLine("Generated: ${displayDateFormat.format(Date())}")
            appendLine("Target: $targetUrl")
            appendLine("Scan Type: $scanType")
            appendLine()

            appendLine("────────────────────────────────────────────────────────────")
            appendLine("                         SUMMARY")
            appendLine("────────────────────────────────────────────────────────────")
            appendLine("Total Vulnerabilities: ${vulnerabilities.size}")
            appendLine("  - Critical: ${vulnerabilities.count { it.severity == Severity.CRITICAL }}")
            appendLine("  - High: ${vulnerabilities.count { it.severity == Severity.HIGH }}")
            appendLine("  - Medium: ${vulnerabilities.count { it.severity == Severity.MEDIUM }}")
            appendLine("  - Low: ${vulnerabilities.count { it.severity == Severity.LOW }}")
            appendLine("  - Informational: ${vulnerabilities.count { it.severity == Severity.INFO }}")
            appendLine()

            if (!portResults.isNullOrEmpty()) {
                appendLine("────────────────────────────────────────────────────────────")
                appendLine("                      OPEN PORTS")
                appendLine("────────────────────────────────────────────────────────────")
                portResults.forEach { (port, service) ->
                    appendLine("  Port $port: ${service ?: "unknown"}")
                }
                appendLine()
            }

            if (vulnerabilities.isNotEmpty()) {
                appendLine("────────────────────────────────────────────────────────────")
                appendLine("                    VULNERABILITIES")
                appendLine("────────────────────────────────────────────────────────────")
                vulnerabilities.forEachIndexed { index, vuln ->
                    appendLine()
                    appendLine("${index + 1}. ${vuln.name} [${vuln.severity.displayName}]")
                    appendLine("   Location: ${vuln.affectedUrl}")
                    if (vuln.cvssScore > 0) {
                        appendLine("   CVSS Score: ${vuln.cvssScore}")
                    }
                    if (vuln.cveId != null) {
                        appendLine("   CVE: ${vuln.cveId}")
                    }
                    appendLine("   Description: ${vuln.description}")
                    appendLine("   Impact: ${vuln.impact}")
                    appendLine("   Remediation: ${vuln.suggestedFix}")
                }
            } else {
                appendLine("────────────────────────────────────────────────────────────")
                appendLine("                   NO VULNERABILITIES FOUND")
                appendLine("────────────────────────────────────────────────────────────")
            }

            appendLine()
            appendLine("════════════════════════════════════════════════════════════")
            appendLine("                    END OF REPORT")
            appendLine("════════════════════════════════════════════════════════════")
        }

        FileWriter(textFile).use { writer ->
            writer.write(content)
        }

        return textFile
    }

    /**
     * Get all saved reports
     */
    fun getSavedReports(): List<File> {
        return reportsDir.listFiles()?.filter {
            it.extension in listOf("json", "txt")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Delete a report
     */
    fun deleteReport(file: File): Boolean {
        return file.delete()
    }

    /**
     * Read report content
     */
    fun readReport(file: File): String? {
        return try {
            file.readText()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
