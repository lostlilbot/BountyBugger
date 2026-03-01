package com.bountybugger.ui.reports

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bountybugger.databinding.ActivityScanResultDetailBinding

/**
 * Scan Result Detail Activity
 */
class ScanResultDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanResultDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanResultDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Scan Details"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
}
