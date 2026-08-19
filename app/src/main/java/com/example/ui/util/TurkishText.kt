package com.example.ui.util

fun String.cleanTurkishText(): String {
    if (isEmpty()) return this

    var text = this
    val replacements = listOf(
        "Ã‡" to "Ç",
        "Ã§" to "ç",
        "Ã–" to "Ö",
        "Ã¶" to "ö",
        "Ãœ" to "Ü",
        "Ã¼" to "ü",
        "Ä°" to "İ",
        "Ä±" to "ı",
        "ÄŸ" to "ğ",
        "Ä" to "Ğ",
        "ÅŸ" to "ş",
        "Å" to "Ş",
        "â€¢" to "•",
        "â€“" to "-",
        "â€”" to "-",
        "â€™" to "'",
        "â€œ" to "\"",
        "â€�" to "\"",
        "Â " to " ",
        "Â" to "",
        "ÃƒÂ‡" to "Ç",
        "ÃƒÂ§" to "ç",
        "Ãƒâ€¡" to "Ç",
        "ÃƒÂ–" to "Ö",
        "ÃƒÂ¶" to "ö",
        "ÃƒÅ“" to "Ü",
        "ÃƒÂ¼" to "ü",
        "Ã„Â°" to "İ",
        "Ã„Â±" to "ı",
        "Ã„Å¸" to "ğ",
        "Ã„Å¾" to "Ğ",
        "Ã…Å¸" to "ş",
        "Ã…Å¾" to "Ş",
        "Ã¢â‚¬Â¢" to "•",
        "Ã¢â‚¬â€œ" to "-",
        "Ã¢â‚¬â„¢" to "'"
    )

    repeat(3) {
        replacements.forEach { (broken, fixed) ->
            text = text.replace(broken, fixed)
        }
    }

    return text
}
