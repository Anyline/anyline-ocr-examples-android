package io.anyline.examples

data class HubspotRequestBody(
        val fields: ArrayList<Field>,
        val legalConsentOptions: LegalConsentOptions
) {

    data class Field(
            val name: String,
            val value: String
    )

    data class LegalConsentOptions(
            val consent: Consent
    ) {

        data class Consent(
                val consentToProcess: Boolean,
                val text: String,
                val communications: ArrayList<Communication>
        ) {

            data class Communication(
                    val value: Boolean,
                    val subscriptionTypeId: Int,
                    val text: String
            )
        }
    }
}