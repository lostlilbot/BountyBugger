package com.bountybugger.ui.tools

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bountybugger.databinding.ActivityToolManagerBinding
import com.bountybugger.service.ToolManagerService
import kotlinx.coroutines.launch

/**
 * Tool Manager Activity
 */
class ToolManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityToolManagerBinding
    private lateinit var toolManager: ToolManagerService
    private lateinit var adapter: ToolAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToolManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolManager = ToolManagerService(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tool Manager"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupRecyclerView()
        setupSearch()
        checkTermux()
        loadTools()
    }

    private fun setupRecyclerView() {
        adapter = ToolAdapter(
            onDownloadClick = { tool ->
                lifecycleScope.launch {
                    val result = toolManager.downloadTool(tool)
                    result.onSuccess {
                        Toast.makeText(this@ToolManagerActivity, "Downloaded: ${tool.name}", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(this@ToolManagerActivity, "Download failed", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onInstallClick = { tool ->
                if (toolManager.isTermuxInstalled()) {
                    val intent = toolManager.installViaTermux(tool)
                    if (intent != null) {
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this@ToolManagerActivity, "Failed to open Termux", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val intent = toolManager.getTermuxInstallIntent()
                    startActivity(intent)
                }
            }
        )
        binding.recyclerTools.layoutManager = LinearLayoutManager(this)
        binding.recyclerTools.adapter = adapter
    }

    private fun setupSearch() {
        binding.editSearch.setOnEditorActionListener { textView, _, _ ->
            val query = textView.text.toString()
            if (query.isNotEmpty()) {
                searchTools(query)
            }
            true
        }
    }

    private fun searchTools(query: String) {
        lifecycleScope.launch {
            val tools = toolManager.searchGitHubTools(query)
            adapter.submitList(tools)
        }
    }

    private fun checkTermux() {
        val termuxInstalled = toolManager.isTermuxInstalled()
        binding.textTermuxStatus.text = if (termuxInstalled) "Termux: Installed" else "Termux: Not installed"
    }

    private fun loadTools() {
        lifecycleScope.launch {
            val tools = toolManager.getBuiltinTools()
            adapter.submitList(tools)
        }
    }
}
