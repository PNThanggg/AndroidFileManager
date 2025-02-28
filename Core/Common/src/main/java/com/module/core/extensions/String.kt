package com.module.core.extensions

import java.util.Locale

val String?.formatLanguage: String?
    get() {
        return this?.let { lang ->
            Locale.forLanguageTag(lang).displayLanguage.takeIf {
                it.isNotEmpty()
            }
        }
    }