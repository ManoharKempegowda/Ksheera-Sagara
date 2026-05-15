package com.example.dairyfarm.utils

import android.content.Context
import android.content.res.Configuration
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

object LocaleManager {
    val currentLocale = MutableStateFlow(Locale("en"))

    fun toggleLocale() {
        val nextLang = if (currentLocale.value.language == "en") "kn" else "en"
        currentLocale.value = Locale(nextLang)
    }

    fun getLocalizedContext(baseContext: Context, locale: Locale): Context {
        val configuration = Configuration(baseContext.resources.configuration)
        configuration.setLocale(locale)
        return baseContext.createConfigurationContext(configuration)
    }
}
