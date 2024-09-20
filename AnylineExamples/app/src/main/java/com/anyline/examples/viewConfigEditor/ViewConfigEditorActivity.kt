package com.anyline.examples.viewConfigEditor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.anyline.examples.ScanActivity
import com.anyline.examples.databinding.ActivityViewConfigEditorBinding
import org.json.JSONObject

class ViewConfigEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewConfigEditorBinding

    private var webContentAction: WebContentFragmentAction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewConfigEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val webContentFragment = ViewConfigEditorFragment(
            ViewConfigEditorDefinition.ScanViewConfig,
            JSONObject(intent.getStringExtra(EXTRA_CONTENT))
        ) { updatedJson ->
            val intent = ScanActivity.buildIntent(this@ViewConfigEditorActivity, updatedJson)
            startActivity(intent)
            this@ViewConfigEditorActivity.finish()
        }

        binding.fragmentContainerWebview.apply {
            supportFragmentManager.beginTransaction()
                .add(this.id, webContentFragment.also {
                    webContentAction = it
                })
                .commit()
        }

        setupToolbar()
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = intent.getStringExtra(EXTRA_TITLE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        webContentAction?.let {
            val canGoBack = it.goBack()
            if (!canGoBack) {
                super.onBackPressed()
            }
        }
    }

    companion object {

        private const val EXTRA_TITLE = "EXTRA_TITLE"
        private const val EXTRA_CONTENT = "EXTRA_CONTENT"

        fun buildIntent(
            context: Context,
            title: String,
            content: String
        ): Intent {
            return Intent(
                context, ViewConfigEditorActivity::class.java
            ).apply {
                this.putExtra(EXTRA_TITLE, title)
                this.putExtra(EXTRA_CONTENT, content)
            }
        }
    }
}
