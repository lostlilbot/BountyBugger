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
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity for creating vulnerability disclosure letters/reports
 */
class DisclosureLetterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDisclosureLetterBinding
    
    private var currentTemplate = DisclosureTemplate.BASIC_BOUNTY
    private var reportReference: String = ""
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val refDateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisclosureLetterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Generate unique reference number
        generateNewReference()
        
        setupToolbar()
        setupTemplateSpinner()
        setupButtons()
        
        // Populate sample data
        populateSampleData()

        // Check if importing from scan
        handleImportIntent()
    }

    private fun generateNewReference() {
        reportReference = "BB-${refDateFormat.format(Date())}"
        binding.editReportReference.setText(reportReference)
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

    private fun applyTemplateDefaults() {
        when (currentTemplate) {
            DisclosureTemplate.BASIC_BOUNTY -> {}
            DisclosureTemplate.DETAILED_TECHNICAL -> {}
            DisclosureTemplate.VDP_POLICY -> {}
            DisclosureTemplate.CREDENTIAL_LEAK -> {}
            DisclosureTemplate.API_SECURITY -> {}
            DisclosureTemplate.XSS_REPORT -> {}
            DisclosureTemplate.SQL_INJECTION -> {}
            DisclosureTemplate.MOBILE_SECURITY -> {}
            DisclosureTemplate.NETWORK_FINDING -> {}
        }
    }

    private fun setupButtons() {
        binding.btnNewReport.setOnClickListener {
            generateNewReference()
            clearForm()
            Toast.makeText(this, "New report generated", Toast.LENGTH_SHORT).show()
        }

        binding.btnImportVuln.setOnClickListener {
            Toast.makeText(this, "Import from scan - Select a vulnerability from reports", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateSampleData() {
        binding.editTitle.setText("Sample Stored XSS Vulnerability")
        binding.editSummary.setText("A stored cross-site scripting vulnerability was found in the user profile field.")
        binding.editDescription.setText("The application fails to properly sanitize user input in the bio field, allowing malicious JavaScript to be stored and executed when other users view the profile.")
        binding.editVulnerableUrl.setText("https://example.com/profile")
        binding.editAffectedEndpoint.setText("POST /api/profile/update")
        binding.editStepsToReproduce.setText("1. Navigate to profile settings\n2. Enter XSS payload in bio field\n3. Save profile\n4. View profile to trigger XSS")
        binding.editProofOfConcept.setText("<script>alert('XSS')</script>")
        binding.editImpact.setText("An attacker could steal session cookies, perform actions as the victim, or redirect users to malicious sites.")
    }

    private fun handleImportIntent() {}

    private fun clearForm() {
        binding.editTitle.setText("")
        binding.editSummary.setText("")
        binding.editDescription.setText("")
        binding.editVulnerableUrl.setText("")
        binding.editAffectedEndpoint.setText("")
        binding.editStepsToReproduce.setText("")
        binding.editProofOfConcept.setText("")
        binding.editImpact.setText("")
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
            R.id.action_new_report -> {
                generateNewReference()
                clearForm()
                Toast.makeText(this, "New report generated", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_save -> {
                saveReport()
                true
            }
            R.id.action_export_pdf -> {
                exportToPDF()
                true
            }
            R.id.action_export_json -> {
                exportToJSON()
                true
            }
            R.id.action_export_markdown -> {
                exportToMarkdown()
                true
            }
            R.id.action_share_email -> {
                shareViaEmail()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveReport() {
        Toast.makeText(this, "Report saved: $reportReference", Toast.LENGTH_SHORT).show()
    }

    private fun exportToPDF() {
        try {
            val fileName = "disclosure_${reportReference}.pdf"
            val file = File(getExternalFilesDir(null), fileName)
            
            val content = buildString {
                appendLine("Vulnerability Disclosure Report")
                appendLine("Reference: $reportReference")
                appendLine("Date: ${displayDateFormat.format(Date())}")
                appendLine()
                appendLine("Title: ${binding.editTitle.text}")
                appendLine()
                appendLine("Summary:")
                appendLine(binding.editSummary.text.toString())
                appendLine()
                appendLine("Description:")
                appendLine(binding.editDescription.text.toString())
                appendLine()
                appendLine("Vulnerable URL: ${binding.editVulnerableUrl.text}")
                appendLine()
                appendLine("Impact:")
                appendLine(binding.editImpact.text.toString())
            }
            
            file.writeText(content)
            Toast.makeText(this, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun exportToJSON() {
        try {
            val fileName = "disclosure_${reportReference}.json"
            val file = File(getExternalFilesDir(null), fileName)
            
            val json = """
            {
                "reference": "$reportReference",
                "date": "${displayDateFormat.format(Date())}",
                "title": "${binding.editTitle.text}",
                "summary": "${binding.editSummary.text}",
                "description": "${binding.editDescription.text}",
                "vulnerableUrl": "${binding.editVulnerableUrl.text}",
                "affectedEndpoint": "${binding.editAffectedEndpoint.text}",
                "stepsToReproduce": "${binding.editStepsToReproduce.text}",
                "proofOfConcept": "${binding.editProofOfConcept.text}",
                "impact": "${binding.editImpact.text}"
            }
            """.trimIndent()
            
            file.writeText(json)
            Toast.makeText(this, "JSON saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun exportToMarkdown() {
        try {
            val fileName = "disclosure_${reportReference}.md"
            val file = File(getExternalFilesDir(null), fileName)
            
            val markdown = """
            # Vulnerability Disclosure Report
            
            **Reference:** $reportReference
            **Date:** ${displayDateFormat.format(Date())}
            
            ## Title
            ${binding.editTitle.text}
            
            ## Summary
            ${binding.editSummary.text}
            
            ## Description
            ${binding.editDescription.text}
            
            ## Vulnerable URL
            ${binding.editVulnerableUrl.text}
            
            ## Affected Endpoint
            ${binding.editAffectedEndpoint.text}
            
            ## Steps to Reproduce
            ${binding.editStepsToReproduce.text}
            
            ## Proof of Concept
            ${binding.editProofOfConcept.text}
            
            ## Impact
            ${binding.editImpact.text}
            """.trimIndent()
            
            file.writeText(markdown)
            Toast.makeText(this, "Markdown saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareViaEmail() {
        try {
            val subject = "Vulnerability Disclosure - $reportReference"
            val body = buildString {
                appendLine("Vulnerability Disclosure Report")
                appendLine("Reference: $reportReference")
                appendLine("Date: ${displayDateFormat.format(Date())}")
                appendLine()
                appendLine("Title: ${binding.editTitle.text}")
                appendLine()
                appendLine("Summary:")
                appendLine(binding.editSummary.text.toString())
                appendLine()
                appendLine("Description:")
                appendLine(binding.editDescription.text.toString())
                appendLine()
                appendLine("Vulnerable URL: ${binding.editVulnerableUrl.text}")
                appendLine()
                appendLine("Impact:")
                appendLine(binding.editImpact.text.toString())
            }
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            
            startActivity(Intent.createChooser(intent, "Send vulnerability report via email"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
