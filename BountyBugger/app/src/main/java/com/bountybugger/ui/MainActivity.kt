package com.bountybugger.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bountybugger.R
import com.bountybugger.databinding.ActivityMainBinding
import com.bountybugger.ui.network.NetworkScannerActivity
import com.bountybugger.ui.web.WebScannerActivity
import com.bountybugger.ui.mobile.MobileAnalysisActivity
import com.bountybugger.ui.tools.ToolManagerActivity
import com.bountybugger.ui.reports.ReportsActivity
//import com.bountybugger.ui.bounty.BountySearchActivity
//import com.bountybugger.ui.disclosure.DisclosureLetterActivity

/**
 * Main Activity - Home screen with tool categories
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCategoryCards()
        showDisclaimer()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupCategoryCards() {
        // Network Scanner Card
        binding.cardNetwork.setOnClickListener {
            startActivity(Intent(this, NetworkScannerActivity::class.java))
        }

        // Web Vulnerability Card
        binding.cardWeb.setOnClickListener {
            startActivity(Intent(this, WebScannerActivity::class.java))
        }

        // Mobile Analysis Card
        binding.cardMobile.setOnClickListener {
            startActivity(Intent(this, MobileAnalysisActivity::class.java))
        }

        // Tool Manager Card
        binding.cardTools.setOnClickListener {
            startActivity(Intent(this, ToolManagerActivity::class.java))
        }

        // Reports Card
        binding.cardReports.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        // Bounty Search Card - Commented out until fixed
        // binding.cardBounty.setOnClickListener {
        //     startActivity(Intent(this, BountySearchActivity::class.java))
        // }

        // Disclosure Letter Card - Commented out until fixed
        // binding.cardDisclosure.setOnClickListener {
        //     startActivity(Intent(this, DisclosureLetterActivity::class.java))
        // }
    }

    private fun showDisclaimer() {
        binding.textDisclaimer.text = getString(R.string.disclaimer)
        binding.textDisclaimer.setTextColor(ContextCompat.getColor(this, R.color.warning))
    }
}
