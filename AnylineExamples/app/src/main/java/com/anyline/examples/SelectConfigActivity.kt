package com.anyline.examples

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anyline.examples.barcodeOverlay.BarcodeOverlayScanActivity
import com.anyline.examples.databinding.ActivitySelectViewConfigBinding
import com.anyline.examples.scanViewConfig.ScanViewConfigFile
import com.anyline.examples.scanViewConfig.ScanViewConfigFolder
import com.anyline.examples.viewConfigEditor.ViewConfigEditorActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectViewConfigBinding
    private val viewConfigAssetsRootFolder = "viewConfigs"

    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main
    private val loadingFilesState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectViewConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.swiperefresh.apply {
            isEnabled = false
            lifecycleScope.launch {
                loadingFilesState.collect { loadingState ->
                    isRefreshing = loadingState
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (loadingFilesState.value) {
                    return
                }
                currentScanViewConfigFolder?.parent?.let {
                    navigateToFolder(it)
                    return
                }
                finish()
            }
        })

        binding.viewConfigs.layoutManager = LinearLayoutManager(this@SelectConfigActivity)
        rootScanViewConfigFolder?.let {
            navigateToFolder(it)
        } ?: run {
            loadFromFolder() { scanViewConfigFolder ->
                navigateToFolder(scanViewConfigFolder)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(Menu.NONE, VIEW_OPTIONS_MENU_ID, Menu.NONE, R.string.selectconfig_view_options)
            ?.setIcon(R.drawable.ic_action_filter)
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        else if (item.itemId == VIEW_OPTIONS_MENU_ID) {
            showViewOptionsDialog()
        }
        return false
    }

    private fun loadFromFolder(onFinish: ((ScanViewConfigFolder) -> Unit)) {
        loadingFilesState.update { true }
        lifecycleScope.launch(dispatcherIO) {
            rootScanViewConfigFolder = ScanViewConfigFolder.loadFromFolder(
                this@SelectConfigActivity,
                viewConfigAssetsRootFolder,
                "Scan View Configs",
                listOptions)
            withContext(dispatcherMain) {
                currentScanViewConfigFolder = rootScanViewConfigFolder
                loadingFilesState.update { false }
                currentScanViewConfigFolder?.let {
                    onFinish.invoke(it)
                }
            }
        }
    }

    private fun navigateToFolder(scanViewConfigFolder: ScanViewConfigFolder) {
        currentScanViewConfigFolder = scanViewConfigFolder
        binding.viewConfigs.adapter = ConfigRecyclerViewAdapter(
            scanViewConfigList = scanViewConfigFolder.asOrderedList(),
            onFolderSelected = ::navigateToFolder,
            onViewConfigSelected = ::onFileSelected,
            onViewConfigHelpRequested = ::onFileHelpSelected
        ) { viewConfig -> onViewConfigEdit(viewConfig) }
        title = scanViewConfigFolder.friendlyName
    }

    private fun onFileSelected(viewConfig: ScanViewConfigFile) {
        if (viewConfig.isValid()) {
            val intent = if (viewConfig.shouldUseOverlayActivity()) {
                BarcodeOverlayScanActivity.buildIntent(this, viewConfig.fileNameWithPath())
            }
            else {
                ScanActivity.buildIntent(this, viewConfig.fileNameWithPath())
            }
            startActivity(intent)
        }
        else {
            showAlertDialog(
                 resources.getString(R.string.selectconfig_validation_error), viewConfig.errorMessage?: ""
            )
        }
    }

    private fun onViewConfigEdit(viewConfig: ScanViewConfigFile): Boolean {
        val intent = ViewConfigEditorActivity.buildIntent(this, "Edit ViewConfig", viewConfig.fileContent)
        startActivity(intent)
        return true
    }

    private fun onFileHelpSelected(viewConfig: ScanViewConfigFile) {
        if (viewConfig.isValid() && viewConfig.descriptionLinks.isNotEmpty()) {
            Intent().also { linkIntent ->
                val uri = Uri.parse(viewConfig.descriptionLinks.first())
                linkIntent.setAction(Intent.ACTION_VIEW)
                linkIntent.setData(uri)
                startActivity(linkIntent)
            }
        }
    }

    private fun showAlertDialog(title: String, message: String, onDismiss: (() -> Unit)? = null) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
            .setMessage(message)
            .setOnDismissListener { onDismiss?.invoke() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showViewOptionsDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.selectconfig_view_options)
            .setSingleChoiceItems(
                ScanViewConfigFolder.ListOptions.asDescriptionList(),
                listOptions.ordinal
            ) { dialogInterface, intChoice ->
                listOptions = ScanViewConfigFolder.ListOptions.values()[intChoice]
                loadFromFolder() { scanViewConfigFolder ->
                    navigateToFolder(scanViewConfigFolder)
                }
                dialogInterface.dismiss()
            }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    class ConfigRecyclerViewAdapter(
        private val scanViewConfigList: List<Any>,
        private val onFolderSelected: (ScanViewConfigFolder) -> Unit,
        private val onViewConfigSelected: (ScanViewConfigFile) -> Unit,
        private val onViewConfigHelpRequested: (ScanViewConfigFile) -> Unit,
        private val onViewConfigEditRequested: (ScanViewConfigFile) -> Boolean
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
            return ConfigRecyclerViewHolder(view,
                onFolderClick = onFolderSelected,
                onFileClick = onViewConfigSelected,
                onFileHelpClick = onViewConfigHelpRequested,
                onFileLongClick = onViewConfigEditRequested)
        }

        override fun onBindViewHolder(holder: ConfigRecyclerViewHolder, position: Int) {
            holder.bind(scanViewConfigList[position])
        }

        override fun getItemCount(): Int = scanViewConfigList.size
    }

    class ConfigRecyclerViewHolder(
        private val item: View,
        private val onFolderClick: (ScanViewConfigFolder) -> Unit,
        private val onFileClick: (ScanViewConfigFile) -> Unit,
        private val onFileHelpClick: (ScanViewConfigFile) -> Unit,
        private val onFileLongClick: (ScanViewConfigFile) -> Boolean
    ) : RecyclerView.ViewHolder(item) {

        fun bind(viewConfigFileOrFolder: Any) {
            if (viewConfigFileOrFolder is ScanViewConfigFile) {
                item.findViewById<ImageView>(R.id.view_config_image).apply {
                    setImageResource(R.drawable.ic_file)
                }
                item.findViewById<TextView>(R.id.view_config_name).apply {
                    text = viewConfigFileOrFolder.fileName
                }
                item.findViewById<TextView>(R.id.view_config_description_text).apply {
                    text = viewConfigFileOrFolder.description ?: run {"(error validating view config)"}
                }
                item.findViewById<ImageView>(R.id.view_config_help_image).apply {
                    if (viewConfigFileOrFolder.descriptionLinks.isNotEmpty()) {
                        setImageResource(R.drawable.ic_help_circle)
                        setOnClickListener { onFileHelpClick(viewConfigFileOrFolder) }
                        visibility = View.VISIBLE
                    }
                    else {
                        setOnClickListener(null)
                        visibility = View.GONE
                    }
                }
                item.setOnClickListener { onFileClick(viewConfigFileOrFolder) }
                item.setOnLongClickListener { onFileLongClick(viewConfigFileOrFolder) }
            }
            else if (viewConfigFileOrFolder is ScanViewConfigFolder) {
                item.findViewById<ImageView>(R.id.view_config_image).apply {
                    setImageResource(R.drawable.ic_folder)
                }
                item.findViewById<TextView>(R.id.view_config_name).apply {
                    text = viewConfigFileOrFolder.friendlyName
                }
                item.findViewById<TextView>(R.id.view_config_description_text).apply {
                    text = ""
                }
                item.setOnClickListener { onFolderClick(viewConfigFileOrFolder) }
            }
        }
    }

    companion object {
        private const val VIEW_OPTIONS_MENU_ID = 1

        var rootScanViewConfigFolder: ScanViewConfigFolder? = null
        var currentScanViewConfigFolder: ScanViewConfigFolder? = null
        var listOptions: ScanViewConfigFolder.ListOptions = ScanViewConfigFolder.ListOptions.GroupByCategory

        fun buildIntent(context: Context): Intent {
            return Intent(context, SelectConfigActivity::class.java)
        }
    }
}