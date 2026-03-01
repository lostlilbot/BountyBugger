package com.bountybugger.ui.web

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bountybugger.R
import com.bountybugger.databinding.ActivityWebScannerBinding
import com.bountybugger.domain.model.ScanResult
import com.bountybugger.domain.model.ScanStatus
import com.bountybugger.domain.model.ScanType
import com.bountybugger.domain.model.Vulnerability
//import com.bountybugger.service.ReportGenerator
import com.bountybugger.service.ScanOptions
import com.bountybugger.service.WebVulnerabilityScanner
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Web Vulnerability Scanner Activity
 */
class WebScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebScannerBinding
    private lateinit var webScanner: WebVulnerabilityScanner
    //private lateinit var reportGenerator: ReportGenerator
    private lateinit var adapter: VulnerabilityAdapter
    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webScanner = WebVulnerabilityScanner()
        //reportGenerator = ReportGenerator(this)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeScanProgress()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.web_scanner_title)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = VulnerabilityAdapter()
        binding.recyclerResults.layoutManager = LinearLayoutManager(this)
        binding.recyclerResults.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnStartScan.setOnClickListener {
            val target = binding.editTargetUrl.text.toString().trim()

            if (target.isEmpty()) {
                Toast.makeText(this, R.string.error_no_target, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val url = if (!target.startsWith("http")) "http://$target" else target
            startScan(url)
        }

        binding.btnStopScan.setOnClickListener {
            webScanner.cancelScan()
            isScanning = false
            binding.btnStartScan.visibility = View.VISIBLE
            binding.btnStopScan.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
        }

        binding.btnExportJson.setOnClickListener { exportReport("json") }
        binding.btnExportPdf.setOnClickListener { exportReport("pdf") }
    }

    private fun observeScanProgress() {
        lifecycleScope.launch {
            webScanner.scanProgress.collectLatest { progress ->
                binding.progressBar.progress = (progress * 100).toInt()
                binding.textProgress.text = "${(progress * 100).toInt()}%"
            }
        }

        lifecycleScope.launch {
            webScanner.vulnerabilities.collectLatest { vulns ->
                adapter.submitList(vulns)
                updateSummary(vulns)
            }
        }
    }

    private fun startScan(targetUrl: String) {
        isScanning = true
        binding.btnStartScan.visibility = View.GONE
        binding.btnStopScan.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE

        val options = ScanOptions(
            sqlInjection = binding.checkboxSqli.isChecked,
            xss = binding.checkboxXss.isChecked,
            securityHeaders = binding.checkboxHeaders.isChecked,
            sslAnalysis = binding.checkboxSsl.isChecked
        )

        lifecycleScope.launch {
            val vulnerabilities = webScanner.scan(targetUrl, options)
            finishScan(vulnerabilities, targetUrl)
        }
    }

    private fun finishScan(vulnerabilities: List<Vulnerability>, target: String) {
        isScanning = false
        binding.btnStartScan.visibility = View.VISIBLE
        binding.btnStopScan.visibility = View.GONE
        binding.progressBar.visibility = View.GONE

        if (vulnerabilities.isEmpty()) {
            Toast.makeText(this, R.string.no_vulnerabilities, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Found ${vulnerabilities.size} vulnerabilities", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSummary(vulnerabilities: List<Vulnerability>) {
        val critical = vulnerabilities.count { it.severity == com.bountybugger.domain.model.Severity.CRITICAL }
        val high = vulnerabilities.count { it.severity == com.bountybugger.domain.model.Severity.HIGH }
        val medium = vulnerabilities.count { it.severity == com.bountybugger.domain.model.Severity.MEDIUM }
        val low = vulnerabilities.count { it.severity == com.bountybugger.domain.model.Severity.LOW }

        binding.textCritical.text = "Critical: $critical"
        binding.textHigh.text = "High: $high"
        binding.textMedium.text = "Medium: $medium"
        binding.textLow.text = "Low: $low"
    }

    private fun exportReport(format: String) {
        val vulns = webScanner.vulnerabilities.value
        if (vulns.isEmpty()) {
            Toast.makeText(this, "No results to export", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val scanResult = ScanResult(
                id = ScanResult.generateId(),
                scanType = ScanType.WEB_SCAN,
                target = binding.editTargetUrl.text.toString(),
                startTime = System.currentTimeMillis() - 60000,
                endTime = System.currentTimeMillis(),
                status = ScanStatus.COMPLETED,
                vulnerabilities = vulns
            )

            // Report generation disabled - ReportGenerator removed
            /*val file = if (format == "json") {
                reportGenerator.generateJsonReport(scanResult)
            } else {
                reportGenerator.generatePdfReport(scanResult)
            }

            Toast.makeText(this@WebScannerActivity, "Report saved: ${file.name}", Toast.LENGTH_SHORT).show()*/
            Toast.makeText(this@WebScannerActivity, "Report feature disabled", Toast.LENGTH_SHORT).show()
        }
    }
}
