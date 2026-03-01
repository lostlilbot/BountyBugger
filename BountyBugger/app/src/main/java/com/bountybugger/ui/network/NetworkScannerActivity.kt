package com.bountybugger.ui.network

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    // Permission launcher for location permission (required for WiFi info on Android 10+)
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with auto scan
            performAutoScan()
        } else {
            Toast.makeText(this, "Location permission required for network detection. Please enter IP manually.", Toast.LENGTH_LONG).show()
            // Try alternative method without location
            performAlternativeScan()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNetworkScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkScanner = NetworkScanner(this)
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
            // Auto-detect network and scan automatically
            autoScanNetwork()
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

    /**
     * Auto-detect current network and scan automatically
     * Handles permission requests for Android 10+
     */
    private fun autoScanNetwork() {
        // Check if we have location permission (required for WiFi info on Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    performAutoScan()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    Toast.makeText(this, "Location permission is needed to detect WiFi network", Toast.LENGTH_LONG).show()
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                else -> {
                    // Request permission
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        } else {
            // For older Android versions, we can directly access WiFi
            performAutoScan()
        }
    }

    /**
     * Perform the actual auto scan after permissions are granted
     */
    private fun performAutoScan() {
        // Show that we're scanning
        binding.progressBar.visibility = View.VISIBLE
        binding.textProgress.text = "Scanning network..."
        
        lifecycleScope.launch {
            // First, try to find live hosts on the local subnet
            val liveHosts = networkScanner.scanLocalSubnet(1500)
            
            if (liveHosts.isNotEmpty()) {
                // Found live hosts, scan them for open ports
                Toast.makeText(this@NetworkScannerActivity, "Found ${liveHosts.size} live hosts, scanning for services...", Toast.LENGTH_SHORT).show()
                
                val allResults = mutableListOf<PortResult>()
                
                // Scan each live host
                for (host in liveHosts) {
                    val results = networkScanner.quickScan(host)
                    allResults.addAll(results)
                }
                
                // Update the results
                adapter.submitList(allResults)
                currentResults = allResults
                updateResultsSummary(allResults)
                
                // Show first live host in target field
                binding.editTargetIp.setText(liveHosts.first())
                binding.editPortRange.setText("1-1000")
                
                binding.progressBar.visibility = View.GONE
                binding.textProgress.text = ""
                
                Toast.makeText(this@NetworkScannerActivity, "Quick scan completed. Found ${allResults.count { it.state == PortState.OPEN }} open ports.", Toast.LENGTH_SHORT).show()
            } else {
                // No live hosts found, try the gateway
                val networkInfo = networkScanner.getCurrentNetworkInfo()
                
                if (networkInfo != null) {
                    currentTarget = networkInfo.gateway
                    binding.editTargetIp.setText(networkInfo.gateway)
                    binding.editPortRange.setText("1-1000")
                    
                    Toast.makeText(this@NetworkScannerActivity, "Scanning gateway: ${networkInfo.gateway}", Toast.LENGTH_SHORT).show()
                    
                    // Start the scan
                    startScan(networkInfo.gateway, "1-1000")
                } else {
                    // Try alternative method
                    performAlternativeScan()
                }
            }
        }
    }

    /**
     * Alternative scan method without location permission
     */
    private fun performAlternativeScan() {
        val localIp = networkScanner.getLocalIpAddress()
        if (localIp != null) {
            val subnet = localIp.substringBeforeLast(".")
            currentTarget = "$subnet.1"
            binding.editTargetIp.setText("$subnet.1")
            binding.editPortRange.setText("1-1000")
            
            Toast.makeText(this, "Scanning local network", Toast.LENGTH_SHORT).show()
            startScan("$subnet.1", "1-1000")
        } else {
            Toast.makeText(this, "Could not detect network. Please enter IP manually.", Toast.LENGTH_LONG).show()
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
