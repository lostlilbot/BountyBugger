package com.bountybugger.ui.reports

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bountybugger.databinding.ActivityReportsBinding
import com.bountybugger.service.ReportGenerator
import java.io.File

/**
 * Reports Activity - View saved vulnerability reports
 */
class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var reportGenerator: ReportGenerator
    private lateinit var adapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reportGenerator = ReportGenerator(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reports"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadReports()
    }

    private fun setupRecyclerView() {
        adapter = ReportAdapter { reportFile ->
            openReport(reportFile)
        }
        binding.recyclerReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerReports.adapter = adapter
    }

    private fun loadReports() {
        val reports = reportGenerator.getSavedReports()
        if (reports.isEmpty()) {
            binding.textEmpty.visibility = android.view.View.VISIBLE
            binding.recyclerReports.visibility = android.view.View.GONE
            binding.textEmpty.text = "No reports yet.\nRun a scan to generate reports."
        } else {
            binding.textEmpty.visibility = android.view.View.GONE
            binding.recyclerReports.visibility = android.view.View.VISIBLE
            adapter.submitList(reports)
        }
    }

    private fun openReport(reportFile: File) {
        val content = reportGenerator.readReport(reportFile)
        if (content != null) {
            // Open report detail activity or show in dialog
            val intent = Intent(this, ScanResultDetailActivity::class.java)
            intent.putExtra("report_file", reportFile.absolutePath)
            intent.putExtra("report_content", content)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Failed to open report", Toast.LENGTH_SHORT).show()
        }
    }
}
