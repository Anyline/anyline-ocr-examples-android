package io.anyline.examples.util

import java.util.regex.Pattern

/**
 * Checks the given String against a Regex Expression to determine if it's a potentially valid
 * Email address. Based on this Stackoverflow Post: https://stackoverflow.com/a/12947706/970998
 */
object EmailValidator {
    fun isValidEmailPattern(email: String): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }

    private val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
}