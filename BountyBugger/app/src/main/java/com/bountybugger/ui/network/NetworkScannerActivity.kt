package com.bountybugger.ui.network

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bountybugger.R
import com.bountybugger.databinding.ActivityNetworkScannerBinding
import com.bountybugger.domain.model.PortResult
import com.bountybugger.domain.model.PortState
import com.bountybugger.service.NetworkScanner
import com.bountybugger.service.ReportGenerator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Network Scanner Activity
 */
class NetworkScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNetworkScannerBinding
    private lateinit var networkScanner: NetworkScanner
    private lateinit var reportGenerator: ReportGenerator
    private lateinit var adapter: PortResultAdapter
    private var isScanning = false
    private var currentResults: List<PortResult> = emptyList()
    private var currentTarget: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNetworkScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkScanner = NetworkScanner()
        reportGenerator = ReportGenerator(this)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeScanProgress()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.network_scanner_title)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = PortResultAdapter()
        binding.recyclerResults.layoutManager = LinearLayoutManager(this)
        binding.recyclerResults.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnStartScan.setOnClickListener {
            val target = binding.editTargetIp.text.toString().trim()
            val portRange = binding.editPortRange.text.toString().trim()

            if (target.isEmpty()) {
                Toast.makeText(this, R.string.error_no_target, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentTarget = target
            startScan(target, portRange)
        }

        binding.btnStopScan.setOnClickListener {
            stopScan()
        }

        binding.btnQuickScan.setOnClickListener {
            val target = binding.editTargetIp.text.toString().trim()
            if (target.isEmpty()) {
                Toast.makeText(this, R.string.error_no_target, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentTarget = target
            quickScan(target)
        }

        binding.btnExportJson.setOnClickListener {
            exportReport("json")
        }

        binding.btnExportPdf.setOnClickListener {
            exportReport("text")
        }
    }

    private fun observeScanProgress() {
        lifecycleScope.launch {
            networkScanner.scanProgress.collectLatest { progress ->
                binding.progressBar.progress = (progress * 100).toInt()
                binding.textProgress.text = "${(progress * 100).toInt()}%"
            }
        }

        lifecycleScope.launch {
            networkScanner.scanResults.collectLatest { results ->
                adapter.submitList(results)
                currentResults = results
                updateResultsSummary(results)
            }
        }
    }

    private fun startScan(target: String, portRange: String) {
        isScanning = true
        binding.btnStartScan.visibility = View.GONE
        binding.btnStopScan.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE

        val (startPort, endPort) = parsePortRange(portRange)

        lifecycleScope.launch {
            val results = networkScanner.scanPorts(target, startPort, endPort)
            finishScan(results)
        }
    }

    private fun quickScan(target: String) {
        isScanning = true
        binding.btnStartScan.visibility = View.GONE
        binding.btnStopScan.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val results = networkScanner.quickScan(target)
            finishScan(results)
        }
    }

    private fun stopScan() {
        networkScanner.cancelScan()
        isScanning = false
        binding.btnStartScan.visibility = View.VISIBLE
        binding.btnStopScan.visibility = View.GONE
    }

    private fun finishScan(results: List<PortResult>) {
        isScanning = false
        binding.btnStartScan.visibility = View.VISIBLE
        binding.btnStopScan.visibility = View.GONE
        binding.progressBar.visibility = View.GONE

        Toast.makeText(this, "Scan completed. Found ${results.count { it.state == PortState.OPEN }} open ports", Toast.LENGTH_SHORT).show()
    }

    private fun updateResultsSummary(results: List<PortResult>) {
        val openPorts = results.count { it.state == PortState.OPEN }
        val closedPorts = results.count { it.state == PortState.CLOSED }

        binding.textOpenPorts.text = getString(R.string.open_ports) + ": $openPorts"
        binding.textClosedPorts.text = getString(R.string.closed_ports) + ": $closedPorts"
    }

    private fun parsePortRange(portRange: String): Pair<Int, Int> {
        return if (portRange.contains("-")) {
            val parts = portRange.split("-")
            Pair(parts[0].toIntOrNull() ?: 1, parts[1].toIntOrNull() ?: 1024)
        } else {
            val port = portRange.toIntOrNull() ?: 80
            Pair(port, port)
        }
    }

    private fun exportReport(format: String) {
        val results = currentResults
        if (results.isEmpty()) {
            Toast.makeText(this, "No results to export. Run a scan first.", Toast.LENGTH_SHORT).show()
            return
        }

        val target = if (currentTarget.isNotEmpty()) currentTarget else binding.editTargetIp.text.toString()

        lifecycleScope.launch {
            try {
                // Convert port results to a map for the report
                val portResults: Map<Int, String> = results
                    .filter { it.state == PortState.OPEN }
                    .associate { it.port to (it.service ?: "unknown") }

                // Since we don't have vulnerabilities, create an empty list
                val emptyVulns = emptyList<com.bountybugger.domain.model.Vulnerability>()

                val file = reportGenerator.generateReport(
                    targetUrl = target,
                    scanType = "NetworkScan",
                    vulnerabilities = emptyVulns,
                    portResults = portResults
                )

                if (file != null) {
                    Toast.makeText(this@NetworkScannerActivity, "Report saved: ${file.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@NetworkScannerActivity, "Failed to generate report", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NetworkScannerActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
