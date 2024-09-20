package com.anyline.examples.scanViewConfig

import android.content.Context
import io.anyline.plugin.config.ScanViewConfiguration
import io.anyline.plugin.config.ViewPluginCompositeConfig
import io.anyline2.sdk.ScanViewConfigHolder
import io.anyline2.sdk.ScanViewConfigHolder.ScanViewJsonValidationResult
import org.json.JSONObject
import java.io.File
import java.util.regex.Pattern

data class ScanViewConfigFile(
    val assetFolderName: String,
    val fileName: String,
    val fileContent: String,
    private val scanViewConfig: ScanViewConfiguration?,
    val errorMessage: String? = null) {

    enum class ScanPluginWorkflow(val description: String) {
        Simple("Simple"),
        CompositeParallel("Composite (Parallel / ParallelFirst)"),
        CompositeSequential("Composite (Sequential)")
    }

    enum class ScanPluginCategory(val description: String) {
        Vehicle("Vehicle"),
        MeterReading("Meter Reading"),
        IdentityDocuments("Identity Documents"),
        Barcode("Barcode"),
        Others("Others")
    }

    enum class ScanPluginType(val description: String, val categories: List<ScanPluginCategory>) {
        Barcode("Barcode", listOf(
            ScanPluginCategory.Barcode
        )),
        Meter("Meter", listOf(
            ScanPluginCategory.MeterReading
        )),
        DigitalOdometer("Digital Odometer", listOf(
            ScanPluginCategory.Vehicle
        )),
        ID("ID", listOf(
            ScanPluginCategory.IdentityDocuments
        )),
        JapaneseLandingPermission("Japanese Landing Permission", listOf()),
        MRZ("MRZ", listOf(
            ScanPluginCategory.IdentityDocuments
        )),
        VehicleRegistrationCertificate("Vehicle Registration Certificate", listOf(
            ScanPluginCategory.Vehicle
        )),
        LicensePlate("License Plate", listOf(
            ScanPluginCategory.Vehicle
        )),
        TINDOT("TIN DOT", listOf(
            ScanPluginCategory.Vehicle
        )),
        TireSizeSpecification("Tire Size Specification", listOf(
            ScanPluginCategory.Vehicle
        )),
        CommercialTireID("Commercial Tire ID", listOf(
            ScanPluginCategory.Vehicle
        )),
        TireMake("Tire Make", listOf(
            ScanPluginCategory.Vehicle
        )),
        VIN("VIN", listOf(
            ScanPluginCategory.Vehicle
        )),
        ShippingContainerNumber("Shipping Container Number", listOf(
            ScanPluginCategory.Others
        )),
        CustomOCR("Custom OCR", listOf(
            ScanPluginCategory.MeterReading,
            ScanPluginCategory.Others
        ))
    }

    val description: String?
    val descriptionLinks: List<String>

    val pluginTypeList: List<ScanPluginType>
    var pluginWorkflow: ScanPluginWorkflow = ScanPluginWorkflow.Simple

    init {
        pluginTypeList = mutableListOf()
        descriptionLinks = mutableListOf()
        if (isValid()) {
            description = scanViewConfig?.scanViewConfigDescription?.let {
                loadLinksFromDescription(
                    scanViewConfig.scanViewConfigDescription,
                    descriptionLinks)
            } ?: run { "" }
            scanViewConfig?.let { scanViewConfig ->
                val viewPluginConfigs =
                    scanViewConfig.viewPluginConfig?.let { viewPluginConfig ->
                        pluginWorkflow = ScanPluginWorkflow.Simple
                        listOf(viewPluginConfig)
                    } ?: run {
                        scanViewConfig.viewPluginCompositeConfig?.let { viewPluginCompositeConfig ->
                            pluginWorkflow = when (viewPluginCompositeConfig.processingMode) {
                                ViewPluginCompositeConfig.ProcessingMode.PARALLEL,
                                ViewPluginCompositeConfig.ProcessingMode.PARALLEL_FIRST_SCAN ->
                                    ScanPluginWorkflow.CompositeParallel
                                ViewPluginCompositeConfig.ProcessingMode.SEQUENTIAL ->
                                    ScanPluginWorkflow.CompositeSequential
                            }
                            viewPluginCompositeConfig.viewPlugins.map { it.viewPluginConfig }
                        }
                    }
                viewPluginConfigs?.forEach { viewPluginConfig ->
                    with(viewPluginConfig.pluginConfig) {
                        val pluginTypeName = when {
                            barcodeConfig != null -> ScanPluginType.Barcode
                            meterConfig != null -> ScanPluginType.Meter
                            odometerConfig != null -> ScanPluginType.DigitalOdometer
                            universalIdConfig != null -> ScanPluginType.ID
                            japaneseLandingPermissionConfig != null -> ScanPluginType.JapaneseLandingPermission
                            mrzConfig != null -> ScanPluginType.MRZ
                            vehicleRegistrationCertificateConfig != null -> ScanPluginType.VehicleRegistrationCertificate
                            licensePlateConfig != null -> ScanPluginType.LicensePlate
                            tinConfig != null -> ScanPluginType.TINDOT
                            tireSizeConfig != null -> ScanPluginType.TireSizeSpecification
                            commercialTireIdConfig != null -> ScanPluginType.CommercialTireID
                            tireMakeConfig != null -> ScanPluginType.TireMake
                            vinConfig != null -> ScanPluginType.VIN
                            containerConfig != null -> ScanPluginType.ShippingContainerNumber
                            ocrConfig != null -> ScanPluginType.CustomOCR

                            else -> null
                        }
                        pluginTypeName?.let {
                            pluginTypeList.add(it)
                        }
                    }
                }
            }
        }
        else {
            description = null
        }
    }

    fun fileNameWithPath() = "$assetFolderName${File.separatorChar}$fileName"

    fun isValid() = scanViewConfig != null

    private fun loadLinksFromDescription(originalDescription: String, containedUrls: MutableList<String>)
    : String {
        var updatedDescription = originalDescription
        val urlMatcher = urlPattern.matcher(originalDescription)
        containedUrls.clear()
        while (urlMatcher.find()) {
            val newLink = originalDescription.substring(
                urlMatcher.start(0),
                urlMatcher.end(0))
            containedUrls.add(newLink.trim())

            updatedDescription = updatedDescription.removeRange(
                urlMatcher.start(0),
                urlMatcher.end(0))
        }

        return updatedDescription
    }

    companion object {
        private val urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL)

        fun loadFromFile(context: Context, assetFolderName: String, fileName: String): ScanViewConfigFile {
            val fileContent = context.assets.open("$assetFolderName/$fileName").bufferedReader().use {
                it.readText()
            }
            val scanViewJsonValidationResult = ScanViewConfigHolder.validateJsonObject(context, JSONObject(fileContent))
            when (scanViewJsonValidationResult) {
                is ScanViewJsonValidationResult.ValidationSucceeded -> {
                    var scanViewConfig: ScanViewConfiguration? = null
                    scanViewJsonValidationResult.scanViewConfigHolder.modifyViewConfig { scanViewConfigContent ->
                        scanViewConfig = scanViewConfigContent
                        ScanViewConfigHolder.ModifyViewConfigResult.Discard
                    }
                    return ScanViewConfigFile(assetFolderName,
                        fileName,
                        fileContent,
                        scanViewConfig)
                }
                is ScanViewJsonValidationResult.ValidationFailed -> {
                    return ScanViewConfigFile(assetFolderName,
                        fileName,
                        fileContent,
                        null,
                        scanViewJsonValidationResult.exception.localizedMessage)
                }
            }
        }
    }
}
