package com.bountybugger.ui.disclosure

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bountybugger.R
import com.bountybugger.databinding.ActivityDisclosureLetterBinding
import com.bountybugger.domain.model.*

/**
 * Activity for creating vulnerability disclosure letters/reports
 */
class DisclosureLetterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDisclosureLetterBinding
    
    private var currentTemplate = DisclosureTemplate.BASIC_BOUNTY
    private var currentSeverity = Severity.MEDIUM
    private var currentCVSSMetrics = CVSSMetrics()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisclosureLetterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTemplateSpinner()
        setupSeverityChips()
        setupCVSSMetrics()
        setupButtons()

        // Check if importing from scan
        handleImportIntent()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.disclosure_letter_title)
    }

    private fun setupTemplateSpinner() {
        val templates = DisclosureTemplate.entries.map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, templates)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTemplate.adapter = adapter

        binding.spinnerTemplate.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentTemplate = DisclosureTemplate.entries[position]
                applyTemplateDefaults()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setupSeverityChips() {
        val severities = listOf(
            Severity.CRITICAL to R.id.chipSeverityCritical,
            Severity.HIGH to R.id.chipSeverityHigh,
            Severity.MEDIUM to R.id.chipSeverityMedium,
            Severity.LOW to R.id.chipSeverityLow,
            Severity.INFO to R.id.chipSeverityInfo
        )

        binding.chipGroupSeverity.setOnCheckedChangeListener { _, checkedId ->
            currentSeverity = severities.find { it.second == checkedId }?.first ?: Severity.MEDIUM
        }
        
        // Set default
        binding.chipSeverityMedium.isChecked = true
    }

    private fun setupCVSSMetrics() {
        binding.btnCalculateCVSS.setOnClickListener {
            calculateCVSS()
        }
    }

    private fun calculateCVSS() {
        // Get metrics from radio buttons
        val attackVector = when (binding.radioGroupAV.checkedRadioButtonId) {
            R.id.radioAVNetwork -> "N"
            R.id.radioAVAdjacent -> "A"
            R.id.radioAVLocal -> "L"
            R.id.radioAVPhysical -> "P"
            else -> "N"
        }

        val attackComplexity = when (binding.radioGroupAC.checkedRadioButtonId) {
            R.id.radioACLow -> "L"
            R.id.radioACHigh -> "H"
            else -> "L"
        }

        val privilegesRequired = when (binding.radioGroupPR.checkedRadioButtonId) {
            R.id.radioPRNone -> "N"
            R.id.radioPRLow -> "L"
            R.id.radioPRHigh -> "H"
            else -> "N"
        }

        val userInteraction = when (binding.radioGroupUI.checkedRadioButtonId) {
            R.id.radioUINone -> "N"
            R.id.radioUIRequired -> "R"
            else -> "N"
        }

        val scope = when (binding.radioGroupScope.checkedRadioButtonId) {
            R.id.radioScopeUnchanged -> "U"
            R.id.radioScopeChanged -> "C"
            else -> "U"
        }

        val confidentiality = when (binding.radioGroupC.checkedRadioButtonId) {
            R.id.radioCNone -> "N"
            R.id.radioCLow -> "L"
            R.id.radioCHigh -> "H"
            else -> "N"
        }

        val integrity = when (binding.radioGroupI.checkedRadioButtonId) {
            R.id.radioINone -> "N"
            R.id.radioILow -> "L"
            R.id.radioIHigh -> "H"
            else -> "N"
        }

        val availability = when (binding.radioGroupA.checkedRadioButtonId) {
            R.id.radioANone -> "N"
            R.id.radioALow -> "L"
            R.id.radioAHigh -> "H"
            else -> "N"
        }

        currentCVSSMetrics = CVSSMetrics(
            attackVector = attackVector,
            attackComplexity = attackComplexity,
            privilegesRequired = privilegesRequired,
            userInteraction = userInteraction,
            scope = scope,
            confidentiality = confidentiality,
            integrity = integrity,
            availability = availability
        )

        // Calculate CVSS score (simplified calculation)
        val score = calculateCVSS3_1Score(currentCVSSMetrics)
        
        binding.editCVSSScore.setText(String.format("%.1f", score))
        binding.editCVSSVector.setText(currentCVSSMetrics.toVectorString())
        
        // Update severity based on score
        currentSeverity = when {
            score >= 9.0 -> Severity.CRITICAL
            score >= 7.0 -> Severity.HIGH
            score >= 4.0 -> Severity.MEDIUM
            score >= 0.1 -> Severity.LOW
            else -> Severity.INFO
        }
        
        Toast.makeText(this, "CVSS Score: ${String.format("%.1f", score)}", Toast.LENGTH_SHORT).show()
    }

    private fun calculateCVSS3_1Score(metrics: CVSSMetrics): Float {
        // Simplified CVSS 3.1 calculation
        // This is a basic implementation - real CVSS calculation is more complex
        
        val iss = calculateISS(metrics)
        val impact = if (metrics.scope == "C") iss * 1.08f else iss
        val exploitability = calculateExploitability(metrics)
        
        var baseScore = 0f
        if (impact <= 0) {
            baseScore = 0f
        } else {
            if (metrics.scope == "C") {
                baseScore = minOf(impact + exploitability, 10f)
            } else {
                baseScore = minOf(1.08f * (impact + exploitability), 10f)
            }
        }
        
        return baseScore
    }

    private fun calculateISS(metrics: CVSSMetrics): Float {
        val confidentiality = when (metrics.confidentiality) {
            "H" -> 0.56f
            "L" -> 0.22f
            else -> 0f
        }
        val integrity = when (metrics.integrity) {
            "H" -> 0.56f
            "L" -> 0.22f
            else -> 0f
        }
        val availability = when (metrics.availability) {
            "H" -> 0.56f
            "L" -> 0.22f
            else -> 0f
        }
        
        return 1 - ((1 - confidentiality) * (1 - integrity) * (1 - availability))
    }

    private fun calculateExploitability(metrics: CVSSMetrics): Float {
        val av = when (metrics.attackVector) {
            "N" -> 0.85f
            "A" -> 0.62f
            "L" -> 0.55f
            "P" -> 0.20f
            else -> 0.85f
        }
        
        val ac = when (metrics.attackComplexity) {
            "L" -> 0.77f
            "H" -> 0.44f
            else -> 0.77f
        }
        
        val pr = when (metrics.privilegesRequired) {
            "N" -> 0.85f
            "L" -> 0.62f
            "H" -> 0.27f
            else -> 0.85f
        }
        
        val ui = when (metrics.userInteraction) {
            "N" -> 0.85f
            "R" -> 0.62f
            else -> 0.85f
        }
        
        return 8.22f * av * ac * pr * ui
    }

    private fun applyTemplateDefaults() {
        // Apply template-specific defaults
        when (currentTemplate) {
            DisclosureTemplate.BASIC_BOUNTY -> {
                binding.editSummary.setText("Brief summary of the vulnerability")
                binding.editDescription.setText("Detailed description of the vulnerability")
                binding.editImpact.setText("Potential impact of this vulnerability")
            }
            DisclosureTemplate.DETAILED_TECHNICAL -> {
                binding.editProofOfConcept.setText("Step-by-step reproduction:\n1. \n2. \n3. ")
            }
            DisclosureTemplate.XSS_REPORT -> {
                binding.editTitle.setText("Cross-Site Scripting (XSS)")
                binding.editDescription.setText("XSS vulnerability found in input field")
                binding.editStepsToReproduce.setText("1. Navigate to affected page\n2. Enter payload in input field\n3. Observe XSS execution")
            }
            DisclosureTemplate.SQL_INJECTION -> {
                binding.editTitle.setText("SQL Injection")
                binding.editDescription.setText("SQL injection vulnerability in parameter")
                binding.editStepsToReproduce.setText("1. Identify input field\n2. Enter SQL payload\n3. Observe SQL error or data leak")
            }
            else -> {}
        }
    }

    private fun setupButtons() {
        binding.btnImportVuln.setOnClickListener {
            // This would normally open a dialog to select from saved vulnerabilities
            Toast.makeText(this, "Select vulnerability from scan results", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleImportIntent() {
        // Check if launched from scan result
        val vulnTitle = intent.getStringExtra("vuln_title")
        val vulnUrl = intent.getStringExtra("vuln_url")
        val vulnSeverity = intent.getStringExtra("vuln_severity")
        
        vulnTitle?.let { binding.editTitle.setText(it) }
        vulnUrl?.let { binding.editVulnerableUrl.setText(it) }
    }

    private fun createDisclosureLetter(): DisclosureLetter {
        val stepsToReproduce = binding.editStepsToReproduce.text.toString()
            .split("\n")
            .filter { it.isNotBlank() }
        
        val cvssScore = binding.editCVSSScore.text.toString().toFloatOrNull() ?: 0f
        
        return DisclosureLetter(
            id = DisclosureLetter.generateId(),
            title = binding.editTitle.text.toString(),
            template = currentTemplate,
            summary = binding.editSummary.text.toString(),
            description = binding.editDescription.text.toString(),
            vulnerableUrl = binding.editVulnerableUrl.text.toString(),
            affectedEndpoint = binding.editAffectedEndpoint.text.toString().takeIf { it.isNotBlank() },
            stepsToReproduce = stepsToReproduce,
            proofOfConcept = binding.editProofOfConcept.text.toString(),
            impact = binding.editImpact.text.toString(),
            severity = currentSeverity,
            cvssScore = cvssScore,
            cvssVector = binding.editCVSSVector.text.toString().takeIf { it.isNotBlank() },
            suggestedFix = binding.editSuggestedFix.text.toString(),
            environmentDetails = binding.editEnvironment.text.toString(),
            programName = binding.editProgramName.text.toString().takeIf { it.isNotBlank() },
            programUrl = binding.editProgramUrl.text.toString().takeIf { it.isNotBlank() },
            researcherName = binding.editResearcherName.text.toString().ifBlank { "Anonymous" },
            researcherContact = binding.editResearcherContact.text.toString().takeIf { it.isNotBlank() }
        )
    }

    private fun exportReport(format: ExportFormat) {
        val letter = createDisclosureLetter()
        
        val content = when (format) {
            ExportFormat.JSON -> generateJsonExport(letter)
            ExportFormat.MARKDOWN -> generateMarkdownExport(letter)
            ExportFormat.EMAIL -> generateEmailDraft(letter)
            ExportFormat.PDF -> "PDF export requires additional library"
        }

        if (format == ExportFormat.PDF) {
            Toast.makeText(this, "PDF export requires additional library setup", Toast.LENGTH_LONG).show()
            return
        }

        // Save to file
        val fileName = "disclosure_${letter.id}_${System.currentTimeMillis()}.${format.extension}"
        try {
            val file = java.io.File(getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), fileName)
            file.writeText(content)
            Toast.makeText(this, "Saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            
            // Share the file
            shareFile(file, format)
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateJsonExport(letter: DisclosureLetter): String {
        val json = org.json.JSONObject()
        json.put("id", letter.id)
        json.put("title", letter.title)
        json.put("template", letter.template.displayName)
        json.put("summary", letter.summary)
        json.put("description", letter.description)
        json.put("vulnerable_url", letter.vulnerableUrl)
        json.put("affected_endpoint", letter.affectedEndpoint ?: "")
        
        val stepsArray = org.json.JSONArray()
        letter.stepsToReproduce.forEach { stepsArray.put(it) }
        json.put("steps_to_reproduce", stepsArray)
        
        json.put("proof_of_concept", letter.proofOfConcept)
        json.put("impact", letter.impact)
        json.put("severity", letter.severity.displayName)
        json.put("cvss_score", letter.cvssScore)
        json.put("cvss_vector", letter.cvssVector ?: "")
        json.put("suggested_fix", letter.suggestedFix)
        json.put("environment_details", letter.environmentDetails)
        json.put("program_name", letter.programName ?: "")
        json.put("program_url", letter.programUrl ?: "")
        json.put("researcher_name", letter.researcherName)
        json.put("created_at", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(letter.createdAt)))
        
        return json.toString(2)
    }

    private fun generateMarkdownExport(letter: DisclosureLetter): String {
        return buildString {
            appendLine("# ${letter.title}")
            appendLine()
            appendLine("## Summary")
            appendLine(letter.summary)
            appendLine()
            appendLine("## Description")
            appendLine(letter.description)
            appendLine()
            appendLine("## Vulnerable URL")
            appendLine(letter.vulnerableUrl)
            letter.affectedEndpoint?.let {
                appendLine("## Affected Endpoint")
                appendLine(it)
            }
            appendLine()
            appendLine("## Steps to Reproduce")
            letter.stepsToReproduce.forEachIndexed { index, step ->
                appendLine("${index + 1}. $step")
            }
            appendLine()
            appendLine("## Proof of Concept")
            appendLine(letter.proofOfConcept)
            appendLine()
            appendLine("## Impact")
            appendLine(letter.impact)
            appendLine()
            appendLine("## Severity")
            appendLine("- **Level**: ${letter.severity.displayName}")
            appendLine("- **CVSS Score**: ${letter.cvssScore}")
            letter.cvssVector?.let { appendLine("- **CVSS Vector**: $it") }
            appendLine()
            appendLine("## Suggested Fix")
            appendLine(letter.suggestedFix)
            appendLine()
            appendLine("## Environment")
            appendLine(letter.environmentDetails)
            appendLine()
            appendLine("---")
            appendLine("*Report generated by BountyBugger*")
        }
    }

    private fun generateEmailDraft(letter: DisclosureLetter): String {
        return buildString {
            appendLine("Subject: Vulnerability Report - ${letter.title}")
            appendLine()
            appendLine("Hello,")
            appendLine()
            appendLine("I would like to report a security vulnerability I found in ${letter.programName ?: letter.vulnerableUrl}.")
            appendLine()
            appendLine("## Summary")
            appendLine(letter.summary)
            appendLine()
            appendLine("## Details")
            appendLine(letter.description)
            appendLine()
            appendLine("## Reproduction Steps")
            letter.stepsToReproduce.forEachIndexed { index, step ->
                appendLine("${index + 1}. $step")
            }
            appendLine()
            appendLine("## Impact")
            appendLine(letter.impact)
            appendLine()
            appendLine("## Severity")
            appendLine("${letter.severity.displayName} (CVSS: ${letter.cvssScore})")
            appendLine()
            appendLine("Please let me know if you need any additional information.")
            appendLine()
            appendLine("Best regards,")
            appendLine(letter.researcherName)
        }
    }

    private fun shareFile(file: java.io.File, format: ExportFormat) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = when (format) {
                    ExportFormat.JSON -> "application/json"
                    ExportFormat.MARKDOWN -> "text/markdown"
                    ExportFormat.EMAIL -> "text/plain"
                    ExportFormat.PDF -> "application/pdf"
                }
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_disclosure, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_export_json -> {
                exportReport(ExportFormat.JSON)
                true
            }
            R.id.action_export_markdown -> {
                exportReport(ExportFormat.MARKDOWN)
                true
            }
            R.id.action_export_email -> {
                exportReport(ExportFormat.EMAIL)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
