package com.anyline.examples

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import at.nineyards.anyline.core.LicenseException
import com.anyline.examples.databinding.ActivityNfcMrzBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.anyline.nfc.NFC.DataGroup1
import io.anyline.nfc.NFC.NFCResult
import io.anyline.nfc.NFC.SOD
import io.anyline.nfc.NfcDetector
import io.anyline.nfc.TagProvider
import io.anyline.plugin.result.MrzResult

class NFCMrzActivity : AppCompatActivity(), NfcDetector.NfcDetectionHandler {

    private lateinit var binding: ActivityNfcMrzBinding

    private lateinit var mrzResult: String
    private lateinit var passportNumber: String
    private lateinit var dateOfBirth: String
    private lateinit var dateOfExpiry: String

    private lateinit var mNfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent

    val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "AnylineNFCMRZActivity"
        private const val INTENT_EXTRA_MRZ_RESULT = "INTENT_EXTRA_MRZ_RESULT"
        private const val INTENT_EXTRA_MRZ_PASSPORT_NUMBER = "INTENT_EXTRA_MRZ_PASSPORT_NUMBER"
        private const val INTENT_EXTRA_MRZ_DATE_OF_BIRTH = "INTENT_EXTRA_MRZ_DATE_OF_BIRTH"
        private const val INTENT_EXTRA_MRZ_DATE_OF_EXPIRY = "INTENT_EXTRA_MRZ_DATE_OF_EXPIRY"

        fun buildIntent(context: Context, mrzResult: MrzResult): Intent {
            val intent = Intent(context, NFCMrzActivity::class.java)
            intent.putExtra(INTENT_EXTRA_MRZ_RESULT, mrzResult.toString())
            intent.putExtra(INTENT_EXTRA_MRZ_PASSPORT_NUMBER, mrzResult.documentNumber)
            intent.putExtra(INTENT_EXTRA_MRZ_DATE_OF_BIRTH, mrzResult.dateOfBirth)
            intent.putExtra(INTENT_EXTRA_MRZ_DATE_OF_EXPIRY, mrzResult.dateOfExpiry)
            return intent
        }

        fun isNfcEnabled(context: Context) : Boolean {
            val manager = context.getSystemService(Context.NFC_SERVICE) as NfcManager
            val adapter = manager.defaultAdapter
            if (adapter == null) {
                MaterialAlertDialogBuilder(context)
                    .setTitle(context.resources.getString(R.string.nfc_error_title))
                    .setMessage(context.resources.getString(R.string.nfc_error_message_not_supported))
                    .setPositiveButton(context.resources.getString(R.string.button_ok)) { dialog, which ->

                    }
                    .show()
                return false
            } else if (!adapter.isEnabled) {
                MaterialAlertDialogBuilder(context)
                    .setTitle(context.resources.getString(R.string.nfc_error_title))
                    .setMessage(context.resources.getString(R.string.nfc_error_message_enable_nfc))
                    .setPositiveButton(context.resources.getString(R.string.button_settings)) { dialog, which ->
                        context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }
                    .setNegativeButton(context.resources.getString(R.string.button_cancel)) { dialog, which ->

                    }
                    .show()
                return false
            }
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNfcMrzBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        intent?.let { intentSafe ->
            mrzResult = intentSafe.getStringExtra(INTENT_EXTRA_MRZ_RESULT)!!
            passportNumber = intentSafe.getStringExtra(INTENT_EXTRA_MRZ_PASSPORT_NUMBER)!!
            dateOfBirth = intentSafe.getStringExtra(INTENT_EXTRA_MRZ_DATE_OF_BIRTH)!!
            dateOfExpiry = intentSafe.getStringExtra(INTENT_EXTRA_MRZ_DATE_OF_EXPIRY)!!
        }

        binding.apply {
            tvMrzDocumentNumber.text = passportNumber
            tvMrzDateOfBirth.text = dateOfBirth
            tvMrzDateOfExpiry.text = dateOfExpiry
        }

        //already checked in starting activity if nfc exists and is switched on
        handleIntents(intent)
        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, this.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )

    }

    @SuppressLint("StringFormatInvalid")
    override fun onResume() {
        super.onResume()
        try {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)
        } catch (e: Exception) {
            showSnackbar(resources.getString(R.string.nfc_adapter_error_prefix, e))
        }
    }

    override fun onPause() {
        super.onPause()
        mNfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntents(intent)
    }

    private fun handleIntents(intent: Intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val parcelableTag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            TagProvider.setTag(IsoDep.get(parcelableTag))

            parcelableTag?.let { tag ->
                if (tag.techList.contains(IsoDep::class.java.name)) {
                    try {
                        val nfcDetector = NfcDetector(applicationContext, this)
                        nfcDetector.startNfcDetection(passportNumber, dateOfBirth, dateOfExpiry)
                    } catch (e: LicenseException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onNfcSuccess(nfcResult: NFCResult?) {

    }

    override fun onDg1Success(dataGroup1: DataGroup1?) {
        dataGroup1?.let { data1 ->
            val firstName = data1.firstName
            val lastName = data1.lastName
            val birthday = data1.dateOfBirth
            val documentNumber = data1.documentNumber
            val documentType = data1.documentType
            val gender = data1.gender
            val issuingStateCode = data1.issuingStateCode
            val nationality = data1.nationality
            val name = "$firstName $lastName"

            handler.post {
                binding.apply {
                    pbScanWaiting.visibility = View.INVISIBLE
                    tvLoadingHint.visibility = View.INVISIBLE
                    tvName.visibility = View.VISIBLE
                    tvIssuingAuthority.visibility = View.VISIBLE
                    tvNationality.visibility = View.VISIBLE
                    tvGender.visibility = View.VISIBLE
                    tvDocumentNumber.visibility = View.VISIBLE
                    tvDateOfBirth.visibility = View.VISIBLE
                    tvIssuingCountryCode.visibility = View.VISIBLE
                    tvValidFrom.visibility = View.VISIBLE
                    tvValidUntil.visibility = View.VISIBLE
                    tvIssuerCountry.visibility = View.VISIBLE
                    tvOrganizationalUnit.visibility = View.VISIBLE
                    tvCertificationAuthority.visibility = View.VISIBLE

                    tvNfcName.text = name
                    tvNfcNationality.text = nationality
                    tvNfcGender.text = gender
                    tvNfcDateOfBirth.text = birthday
                    tvNfcDocumentNumber.text = documentNumber
                    tvNfcIssuingCountryCode.text = issuingStateCode
                }
            }
        }
    }

    override fun onDg2Success(faceImage: Bitmap?) {

    }

    override fun onSODSuccess(sod: SOD?) {
        sod?.let { dataSOD ->
            handler.post {
                binding.apply {
                    tvNfcValidFrom.text = dataSOD.validFromString
                    tvNfcValidUntil.text = dataSOD.validUntilString
                    tvNfcIssuerCountry.text = dataSOD.issuerCountry
                    tvNfcOrganizationalUnit.text = dataSOD.issuerOrganizationalUnit
                    tvNfcCertificationAuthority.text = dataSOD.issuerCertificationAuthority
                    tvNfcIssuingAuthority.text = dataSOD.issuerOrganization
                }
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    override fun onNfcFailure(e: String?) {
        e?.let {
            showSnackbar(resources.getString(R.string.nfc_failure_error_prefix, it))
        }
    }

    private fun showSnackbar(message: String) {
        val snack = Snackbar.make(binding.scrollView, message, Snackbar.LENGTH_LONG)
        snack.show()
    }
}