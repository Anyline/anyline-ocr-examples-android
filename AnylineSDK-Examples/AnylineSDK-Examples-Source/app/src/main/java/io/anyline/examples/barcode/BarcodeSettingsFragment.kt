package io.anyline.examples.barcode

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.preference.*
import io.anyline.examples.R
import io.anyline.examples.barcode.BarcodePreferences.*

class BarcodeSettingsFragment: PreferenceFragmentCompat() {

    private var preferenceSymbologies: Preference? = null
    private var preferenceSingleScanButton: SwitchPreference? = null
    private var preferenceMultiScanButton: SwitchPreference? = null
    private var preferenceReset: Preference? = null

    private lateinit var barcodePreferences: BarcodePreferences

    companion object {
        const val ACTIVITYRESULT_CODE_BARCODE_LIST = 2
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SHARED_PREFS_ANYLINE
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.barcode_settings, rootKey);

        barcodePreferences = getInstance(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        preferenceSymbologies = preferenceManager.findPreference("barcodesetting_symbologies")
        preferenceSymbologies?.let {
            it.setOnPreferenceClickListener { symbologies ->
                val intent = Intent(requireActivity(), BarcodeListViewActivity::class.java)
                startActivityForResult(intent, ACTIVITYRESULT_CODE_BARCODE_LIST)
                true
            }
        }

        preferenceSingleScanButton = preferenceManager.findPreference(SingleScanButtonPreference.key)
        preferenceMultiScanButton = preferenceManager.findPreference(MultiScanButtonPreference.key)

        preferenceReset = preferenceManager.findPreference("barcodesetting_reset")
        preferenceReset?.let {
            it.setOnPreferenceClickListener { symbologies ->
                AlertDialog.Builder(requireActivity())
                    .setTitle(getString(R.string.barcode_reset_settings_title))
                    .setMessage(getString(R.string.barcode_reset_settings_text))
                    .setPositiveButton(
                        android.R.string.yes
                    ) { dialog, which ->
                        barcodePreferences.setBarcodeTypes(barcodePreferences.default)
                        preferenceSingleScanButton?.let { prefScanButton ->
                            prefScanButton.isChecked = SingleScanButtonPreference.defaultValue
                        }
                        preferenceMultiScanButton?.let { prefScanButton ->
                            prefScanButton.isChecked = MultiScanButtonPreference.defaultValue
                        }
                        refresh()
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
                true
            }
        }
        refresh()

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITYRESULT_CODE_BARCODE_LIST) {
            if (resultCode == RESULT_OK) {
                refresh()
            }
        }
    }

    private fun refresh() {
        preferenceSymbologies?.summary = barcodePreferences.get().size.toString() + " Selected"
    }
}