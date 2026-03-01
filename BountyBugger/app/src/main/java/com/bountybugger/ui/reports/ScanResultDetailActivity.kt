package com.bountybugger.ui.reports

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bountybugger.databinding.ActivityScanResultDetailBinding

/**
 * Scan Result Detail Activity - Display report content
 */
class ScanResultDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanResultDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanResultDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Report Details"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Get report content from intent
        val reportFile = intent.getStringExtra("report_file")
        val reportContent = intent.getStringExtra("report_content")

        if (reportContent != null) {
            binding.textReportContent.text = reportContent
            setupShareButton(reportFile, reportContent)
        } else {
            Toast.makeText(this, "No report content found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupShareButton(reportFile: String?, content: String) {
        binding.buttonShare.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Vulnerability Report")
                shareIntent.putExtra(Intent.EXTRA_TEXT, content)
                startActivity(Intent.createChooser(shareIntent, "Share Report"))
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to share report", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
