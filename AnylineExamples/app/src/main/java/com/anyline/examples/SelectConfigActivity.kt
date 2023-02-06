package com.anyline.examples

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anyline.examples.databinding.ActivitySelectViewConfigBinding

class SelectConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectViewConfigBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectViewConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewConfigs: Array<String> = assets.list("viewConfigs") ?: arrayOf()

        with(binding.viewConfigs) {
            layoutManager = LinearLayoutManager(this@SelectConfigActivity)
            adapter = ConfigRecyclerViewAdapter(viewConfigs, ::onViewConfigSelected)
        }
    }

    private fun onViewConfigSelected(viewConfig: String) {
        val intent = ScanActivity.buildIntent(this, "viewConfigs/$viewConfig")
        startActivity(intent)
    }

    class ConfigRecyclerViewAdapter(
        private val viewConfigs: Array<String>,
        private val onViewConfigSelected: (String) -> Unit
    ) : RecyclerView.Adapter<ConfigRecyclerViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ConfigRecyclerViewHolder {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_view_select_config,
                    parent,
                    false
                )
            return ConfigRecyclerViewHolder(view, onViewConfigSelected)
        }

        override fun onBindViewHolder(holder: ConfigRecyclerViewHolder, position: Int) {
            holder.bind(viewConfigs[position])
        }

        override fun getItemCount(): Int = viewConfigs.size
    }

    class ConfigRecyclerViewHolder(
        private val item: View,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(item) {

        fun bind(viewConfig: String) {
            item.findViewById<TextView>(R.id.view_config).text = viewConfig
            item.setOnClickListener { onItemClick(viewConfig) }
        }
    }

    companion object {

        fun buildIntent(context: Context): Intent {
            return Intent(context, SelectConfigActivity::class.java)
        }
    }
}