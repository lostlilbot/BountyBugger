package com.bountybugger.ui.reports

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bountybugger.databinding.ActivityReportsBinding
import java.io.File
//import com.bountybugger.service.ReportGenerator

/**
 * Reports Activity - View saved vulnerability reports
 */
class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    //private lateinit var reportGenerator: ReportGenerator
    private lateinit var adapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //reportGenerator = ReportGenerator(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reports"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupRecyclerView()
        loadReports()
    }

    private fun setupRecyclerView() {
        adapter = ReportAdapter { reportFile ->
            // Open report
        }
        binding.recyclerReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerReports.adapter = adapter
    }

    private fun loadReports() {
        // Report generation disabled - ReportGenerator removed
        //val reports = reportGenerator.getSavedReports()
        val reports = emptyList<File>() // Placeholder - no reports without ReportGenerator
        if (reports.isEmpty()) {
            binding.textEmpty.visibility = android.view.View.VISIBLE
            binding.recyclerReports.visibility = android.view.View.GONE
        } else {
            binding.textEmpty.visibility = android.view.View.GONE
            binding.recyclerReports.visibility = android.view.View.VISIBLE
            adapter.submitList(reports)
        }
    }
}
