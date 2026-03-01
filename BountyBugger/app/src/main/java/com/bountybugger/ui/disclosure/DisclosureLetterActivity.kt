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
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFTable
import org.apache.poi.xwpf.usermodel.XWPFTableRow
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
    private var currentSeverity = Severity.MEDIUM
    private var currentCVSSMetrics = CVSSMetrics()
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
        setupSeverityChips()
        setupCVSSMetrics()
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

    private fun setupSeverityChips() {
        binding.chipGroupSeverity.setOnCheckedChangeListener { _, checkedId ->
            currentSeverity = when (checkedId) {
                R.id.chipSeverityCritical -> Severity.CRITICAL
                R.id.chipSeverityHigh -> Severity.HIGH
                R.id.chipSeverityMedium -> Severity.MEDIUM
                R.id.chipSeverityLow -> Severity.LOW
                R.id.chipSeverityInfo -> Severity.INFO
                else -> Severity.MEDIUM
            }
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

        val score = calculateCVSS3_1Score(currentCVSSMetrics)
        
        binding.editCVSSScore.setText(String.format("%.1f", score))
        binding.editCVSSVector.setText(currentCVSSMetrics.toVectorString())
        
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
        val iss = calculateISS(metrics)
        val impact = if (metrics.scope == "C") iss * 1.08f else iss
        val exploitability = calculateExploitability(metrics)
        
        return if (impact <= 0) {
            0f
        } else {
            val baseScore = if (metrics.scope == "C") {
                minOf(impact + exploitability, 10f)
            } else {
                minOf(1.08f * (impact + exploitability), 10f)
            }
            baseScore
        }
    }

    private fun calculateISS(metrics: CVSSMetrics): Float {
        val confidentiality = when (metrics.confidentiality) {
            "H" -> 0.56f; "L" -> 0.22f; else -> 0f
        }
        val integrity = when (metrics.integrity) {
            "H" -> 0.56f; "L" -> 0.22f; else -> 0f
        }
        val availability = when (metrics.availability) {
            "H" -> 0.56f; "L" -> 0.22f; else -> 0f
        }
        return 1 - ((1 - confidentiality) * (1 - integrity) * (1 - availability))
    }

    private fun calculateExploitability(metrics: CVSSMetrics): Float {
        val av = when (metrics.attackVector) {
            "N" -> 0.85f; "A" -> 0.62f; "L" -> 0.55f; "P" -> 0.20f; else -> 0.85f
        }
        val ac = when (metrics.attackComplexity) {
            "L" -> 0.77f; "H" -> 0.44f; else -> 0.77f
        }
        val pr = when (metrics.privilegesRequired) {
            "N" -> 0.85f; "L" -> 0.62f; "H" -> 0.27f; else -> 0.85f
        }
        val ui = when (metrics.userInteraction) {
            "N" -> 0.85f; "R" -> 0.62f; else -> 0.85f
        }
        return 8.22f * av * ac * pr * ui
    }

    private fun populateSampleData() {
        // Pre-populate with sample vulnerability report data
        binding.editReportReference.setText(reportReference)
        
        binding.editTitle.setText("Reflected Cross-Site Scripting (XSS) in Login Form")
        binding.editSummary.setText("A reflected XSS vulnerability was discovered in the login form parameter that allows execution of arbitrary JavaScript code.")
        binding.editDescription.setText(
            """The vulnerability exists in the 'username' parameter of the login form at /login. 
The application does not properly sanitize user input before displaying it in the response page.
An attacker can craft a malicious URL that, when visited by a victim, executes arbitrary JavaScript code in their browser context.""".trimIndent()
        )
        
        binding.editVulnerableUrl.setText("https://example.com/login?username=<script>alert('XSS')</script>")
        binding.editAffectedEndpoint.setText("POST /login - username parameter")
        
        binding.editStepsToReproduce.setText(
            """1. Navigate to https://example.com/login
2. Enter the following payload in the username field: <script>alert(document.cookie)</script>
3. Click the Login button
4. Observe the XSS execution in the error message""".trimIndent()
        )
        
        binding.editProofOfConcept.setText(
            """```html
GET /login?username=<script>alert('XSS')</script> HTTP/1.1
Host: example.com
User-Agent: Mozilla/5.0

Response:
HTTP/1.1 200 OK
...
<div class="error">Welcome <script>alert('XSS')</script></div>""".trimIndent()
        )
        
        binding.editImpact.setText(
            """- Execution of arbitrary JavaScript code in victim's browser
- Session hijacking via cookie theft
- Phishing attacks by modifying page content
- Keylogging
- Defacement of the website""".trimIndent()
        )
        
        // Set CVSS values
        binding.editCVSSScore.setText("7.3")
        binding.editCVSSVector.setText("CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:U/C:H/I:H/A:N")
        
        // Pre-select severity
        binding.chipSeverityHigh.isChecked = true
        
        binding.editSuggestedFix.setText(
            """1. Implement input validation and output encoding
2. Use Content Security Policy (CSP) headers
3. Apply proper sanitization using libraries like OWASP Java Encoder
4. Enable X-XSS-Protection header
5. Consider using a WAF for additional protection""".trimIndent()
        )
        
        binding.editEnvironment.setText(
            """Browser: Chrome 120.0, Firefox 121.0
OS: Windows 11, macOS Sonoma
Network: Standard HTTP/HTTPS
Testing Tool: Burp Suite Professional 2024""".trimIndent()
        )
        
        binding.editProgramName.setText("Example Corp Bug Bounty Program")
        binding.editProgramUrl.setText("https://bugbounty.example.com")
        binding.editResearcherName.setText("Security Researcher")
        binding.editResearcherContact.setText("researcher@securitytester.com")
    }

    private fun applyTemplateDefaults() {
        when (currentTemplate) {
            DisclosureTemplate.XSS_REPORT -> {
                binding.editTitle.setText("Cross-Site Scripting (XSS)")
                binding.editDescription.setText("XSS vulnerability found in input field")
            }
            DisclosureTemplate.SQL_INJECTION -> {
                binding.editTitle.setText("SQL Injection")
                binding.editDescription.setText("SQL injection vulnerability in parameter")
            }
            DisclosureTemplate.API_SECURITY -> {
                binding.editTitle.setText("API Security Vulnerability")
                binding.editDescription.setText("API security issue discovered")
            }
            else -> {}
        }
    }

    private fun setupButtons() {
        binding.btnImportVuln.setOnClickListener {
            Toast.makeText(this, "Select vulnerability from scan results", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnNewReport.setOnClickListener {
            generateNewReference()
            clearForm()
            populateSampleData()
            Toast.makeText(this, "New report generated: $reportReference", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearForm() {
        binding.editTitle.text?.clear()
        binding.editSummary.text?.clear()
        binding.editDescription.text?.clear()
        binding.editVulnerableUrl.text?.clear()
        binding.editAffectedEndpoint.text?.clear()
        binding.editStepsToReproduce.text?.clear()
        binding.editProofOfConcept.text?.clear()
        binding.editImpact.text?.clear()
        binding.editSuggestedFix.text?.clear()
        binding.editEnvironment.text?.clear()
    }

    private fun handleImportIntent() {
        intent.getStringExtra("vuln_title")?.let { binding.editTitle.setText(it) }
        intent.getStringExtra("vuln_url")?.let { binding.editVulnerableUrl.setText(it) }
    }

    private fun createDisclosureLetter(): DisclosureLetter {
        val stepsToReproduce = binding.editStepsToReproduce.text.toString()
            .split("\n")
            .filter { it.isNotBlank() }
        
        val cvssScore = binding.editCVSSScore.text.toString().toFloatOrNull() ?: 0f
        val reference = binding.editReportReference.text.toString().ifBlank { reportReference }
        
        return DisclosureLetter(
            id = reference,
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
            researcherContact = binding.editResearcherContact.text.toString().takeIf { it.isNotBlank() },
            createdAt = System.currentTimeMillis()
        )
    }

    private fun exportReport(format: ExportFormat) {
        val letter = createDisclosureLetter()
        
        when (format) {
            ExportFormat.JSON -> exportJson(letter)
            ExportFormat.MARKDOWN -> exportMarkdown(letter)
            ExportFormat.EMAIL -> exportEmail(letter)
            ExportFormat.PDF -> exportPdf(letter)
        }
    }

    private fun exportJson(letter: DisclosureLetter) {
        val content = generateJsonExport(letter)
        saveAndShareFile(content, "json", "JSON")
    }

    private fun exportMarkdown(letter: DisclosureLetter) {
        val content = generateMarkdownExport(letter)
        saveAndShareFile(content, "md", "Markdown")
    }

    private fun exportEmail(letter: DisclosureLetter) {
        val content = generateEmailDraft(letter)
        saveAndShareFile(content, "txt", "Email")
    }

    private fun exportPdf(letter: DisclosureLetter) {
        try {
            val fileName = "disclosure_${letter.id}.pdf"
            val file = File(getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), fileName)
            
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)
            
            // Title
            document.add(
                Paragraph(letter.title)
                    .setFontSize(20f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )
            
            // Report Reference
            document.add(
                Paragraph("Report Reference: ${letter.id}")
                    .setFontSize(12f)
                    .setTextAlignment(TextAlignment.CENTER)
            )
            
            document.add(Paragraph("\n"))
            
            // Summary Section
            document.add(
                Paragraph("1. Summary")
                    .setFontSize(14f)
                    .setBold()
            )
            document.add(Paragraph(letter.summary))
            
            // Description Section
            document.add(
                Paragraph("2. Description")
                    .setFontSize(14f)
                    .setBold()
            )
            document.add(Paragraph(letter.description))
            
            // Vulnerable URL
            document.add(
                Paragraph("3. Vulnerable URL/Endpoint")
                    .setFontSize(14f)
                    .setBold()
            )
            document.add(Paragraph(letter.vulnerableUrl))
            letter.affectedEndpoint?.let {
                document.add(Paragraph("Affected Endpoint: $it"))
            }
            
            // Steps to Reproduce
            document.add(
                Paragraph("4. Steps to Reproduce")
                    .setFontSize(14f)
                    .setBold()
            )
            letter.stepsToReproduce.forEachIndexed { index, step ->
                document.add(Paragraph("${index + 1}. $step"))
            }
            
            // Proof of Concept
            document.add(
                Paragraph("5. Proof of Concept")
                    .setFontSize(14f)
                    .setBold()
            )
            document.add(Paragraph(letter.proofOfConcept))
            
            // Impact
            document.add(
                Paragraph("6. Impact")
                    .setFontSize(14f)
                    .setBold()
            )
            document.add(Paragraph(letter.impact))
            
            // Severity
            document.add(
                Paragraph("7. Severity Assessment")
                    .setFontSize(14f)
                    .setBold()
            )
            document.add(Paragraph("Level: ${letter.severity.displayName}"))
            document.add(Paragraph("CVSS Score: ${letter.cvssScore}"))
            letter.cvssVector?.let { document.add(Paragraph("CVSS Vector: $it")) }
            
            // Suggested Fix
            document.add(
                Paragraph("8. Suggested Fix")
                    .setFontSize(14f)
                    .setBold()
            )
            document.add(Paragraph(letter.suggestedFix))
            
            // Environment
            document.add(
                Paragraph("9. Testing Environment")
                    .setFontSize(14f)
                    .setBold()
            )
            document.add(Paragraph(letter.environmentDetails))
            
            // Footer
            document.add(Paragraph("\n"))
            document.add(
                Paragraph("Generated by BountyBugger - ${displayDateFormat.format(Date())}")
                    .setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER)
            )
            
            document.close()
            
            Toast.makeText(this, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            shareFile(file, "application/pdf")
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating PDF: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun exportWord(letter: DisclosureLetter) {
        try {
            val document = XWPFDocument()
            val fileName = "disclosure_${letter.id}.docx"
            val file = File(getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), fileName)
            
            // Title
            val title = document.createParagraph()
            title.run {
                createRun().apply {
                    text = letter.title
                    isBold = true
                    fontSize = 22
                }
            }
            
            // Report Reference
            val refPara = document.createParagraph()
            refPara.createRun().apply {
                text = "Report Reference: ${letter.id}"
                fontSize = 12
            }
            
            // Summary
            addHeading(document, "1. Summary", 14)
            addParagraph(document, letter.summary)
            
            // Description
            addHeading(document, "2. Description", 14)
            addParagraph(document, letter.description)
            
            // Vulnerable URL
            addHeading(document, "3. Vulnerable URL/Endpoint", 14)
            addParagraph(document, letter.vulnerableUrl)
            letter.affectedEndpoint?.let { addParagraph(document, "Affected Endpoint: $it") }
            
            // Steps to Reproduce
            addHeading(document, "4. Steps to Reproduce", 14)
            letter.stepsToReproduce.forEachIndexed { index, step ->
                addParagraph(document, "${index + 1}. $step")
            }
            
            // Proof of Concept
            addHeading(document, "5. Proof of Concept", 14)
            addParagraph(document, letter.proofOfConcept)
            
            // Impact
            addHeading(document, "6. Impact", 14)
            addParagraph(document, letter.impact)
            
            // Severity
            addHeading(document, "7. Severity Assessment", 14)
            addParagraph(document, "Level: ${letter.severity.displayName}")
            addParagraph(document, "CVSS Score: ${letter.cvssScore}")
            letter.cvssVector?.let { addParagraph(document, "CVSS Vector: $it") }
            
            // Suggested Fix
            addHeading(document, "8. Suggested Fix", 14)
            addParagraph(document, letter.suggestedFix)
            
            // Environment
            addHeading(document, "9. Testing Environment", 14)
            addParagraph(document, letter.environmentDetails)
            
            // Researcher Info
            addHeading(document, "10. Researcher Information", 14)
            addParagraph(document, "Name: ${letter.researcherName}")
            letter.researcherContact?.let { addParagraph(document, "Contact: $it") }
            letter.programName?.let { addParagraph(document, "Program: $it") }
            letter.programUrl?.let { addParagraph(document, "Program URL: $it") }
            
            // Footer
            addParagraph(document, "\nGenerated by BountyBugger - ${displayDateFormat.format(Date())}")
            
            // Save
            FileOutputStream(file).use { document.write(it) }
            document.close()
            
            Toast.makeText(this, "Word document saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            shareFile(file, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating Word document: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun addHeading(document: XWPFDocument, text: String, fontSize: Int) {
        val para = document.createParagraph()
        para.createRun().apply {
            this.text = text
            isBold = true
            this.fontSize = fontSize
        }
    }

    private fun addParagraph(document: XWPFDocument, text: String) {
        val para = document.createParagraph()
        para.createRun().apply {
            this.text = text
            fontSize = 11
        }
    }

    private fun generateJsonExport(letter: DisclosureLetter): String {
        val json = org.json.JSONObject()
        json.put("report_reference", letter.id)
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
        json.put("researcher_contact", letter.researcherContact ?: "")
        json.put("created_at", displayDateFormat.format(Date(letter.createdAt)))
        
        return json.toString(2)
    }

    private fun generateMarkdownExport(letter: DisclosureLetter): String {
        return buildString {
            appendLine("# ${letter.title}")
            appendLine()
            appendLine("**Report Reference:** ${letter.id}")
            appendLine()
            appendLine("## 1. Summary")
            appendLine(letter.summary)
            appendLine()
            appendLine("## 2. Description")
            appendLine(letter.description)
            appendLine()
            appendLine("## 3. Vulnerable URL/Endpoint")
            appendLine(letter.vulnerableUrl)
            letter.affectedEndpoint?.let { appendLine("**Affected Endpoint:** $it") }
            appendLine()
            appendLine("## 4. Steps to Reproduce")
            letter.stepsToReproduce.forEachIndexed { index, step ->
                appendLine("${index + 1}. $step")
            }
            appendLine()
            appendLine("## 5. Proof of Concept")
            appendLine("```")
            appendLine(letter.proofOfConcept)
            appendLine("```")
            appendLine()
            appendLine("## 6. Impact")
            appendLine(letter.impact)
            appendLine()
            appendLine("## 7. Severity Assessment")
            appendLine("- **Level:** ${letter.severity.displayName}")
            appendLine("- **CVSS Score:** ${letter.cvssScore}")
            letter.cvssVector?.let { appendLine("- **CVSS Vector:** $it") }
            appendLine()
            appendLine("## 8. Suggested Fix")
            appendLine(letter.suggestedFix)
            appendLine()
            appendLine("## 9. Testing Environment")
            appendLine(letter.environmentDetails)
            appendLine()
            appendLine("---")
            appendLine("*Generated by BountyBugger - ${displayDateFormat.format(Date())}*")
        }
    }

    private fun generateEmailDraft(letter: DisclosureLetter): String {
        return buildString {
            appendLine("Subject: Vulnerability Report - ${letter.title}")
            appendLine()
            appendLine("Hello Security Team,")
            appendLine()
            appendLine("I would like to report a security vulnerability in ${letter.programName ?: letter.vulnerableUrl}.")
            appendLine()
            appendLine("**Report Reference:** ${letter.id}")
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
            appendLine("## Suggested Fix")
            appendLine(letter.suggestedFix)
            appendLine()
            appendLine("Please let me know if you need any additional information.")
            appendLine()
            appendLine("Best regards,")
            appendLine(letter.researcherName)
            letter.researcherContact?.let { appendLine("Contact: $it") }
        }
    }

    private fun saveAndShareFile(content: String, extension: String, formatName: String) {
        try {
            val letter = createDisclosureLetter()
            val fileName = "disclosure_${letter.id}.$extension"
            val file = File(getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), fileName)
            file.writeText(content)
            
            Toast.makeText(this, "$formatName saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            shareFile(file, getMimeType(extension))
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeType(extension: String): String {
        return when (extension) {
            "json" -> "application/json"
            "md" -> "text/markdown"
            "txt" -> "text/plain"
            else -> "text/plain"
        }
    }

    private fun shareFile(file: File, mimeType: String) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
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
            R.id.action_new_report -> {
                generateNewReference()
                clearForm()
                populateSampleData()
                Toast.makeText(this, "New report: $reportReference", Toast.LENGTH_SHORT).show()
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
            R.id.action_export_pdf -> {
                exportReport(ExportFormat.PDF)
                true
            }
            R.id.action_export_word -> {
                exportWord(createDisclosureLetter())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
