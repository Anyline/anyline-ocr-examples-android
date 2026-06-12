package com.anyline.examples.viewConfigEditor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.anyline.examples.ScanActivity
import com.anyline.examples.databinding.ActivityViewConfigEditorBinding
import com.anyline.examples.extensions.setContentViewUsingEdgeToEdge
import org.json.JSONObject

class ViewConfigEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewConfigEditorBinding

    private var webContentAction: WebContentFragmentAction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewConfigEditorBinding.inflate(layoutInflater)
        setContentViewUsingEdgeToEdge(binding.root)

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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val canGoBack = webContentAction?.goBack() ?: false
                if (!canGoBack) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.title = intent.getStringExtra(EXTRA_TITLE)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
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
