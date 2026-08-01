package com.taxicompany.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import taxicompanyapp.composeapp.generated.resources.Res
import taxicompanyapp.composeapp.generated.resources.anton_regular
import taxicompanyapp.composeapp.generated.resources.bebas_neue_regular
import taxicompanyapp.composeapp.generated.resources.merriweather_bold
import taxicompanyapp.composeapp.generated.resources.merriweather_regular

val displayFontFamily: FontFamily
    @Composable get() {
        val font = Font(resource = Res.font.anton_regular, weight = FontWeight.Bold)
        return FontFamily(font)
    }

val bodyFontFamily: FontFamily
    @Composable get() {
        val regular = Font(resource = Res.font.merriweather_regular, weight = FontWeight.Normal)
        val bold = Font(resource = Res.font.merriweather_bold, weight = FontWeight.Bold)
        return FontFamily(regular, bold)
    }

val decoratedFontFamily: FontFamily
    @Composable get() {
        val font = Font(resource = Res.font.bebas_neue_regular, weight = FontWeight.Normal)
        return FontFamily(font)
    }

val Typography.decorated: TextStyle
    @Composable get() = TextStyle(
        fontFamily = decoratedFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    )

private fun TextStyle.withScale(isDesktop: Boolean, sizePlus: Int): TextStyle {
    return if (isDesktop) {
        this.copy(fontSize = (this.fontSize.value + sizePlus).sp)
    } else {
        this
    }
}
private val baseline = Typography()

@Composable
fun getAppTypography(isDesktop: Boolean): Typography {
    val display = displayFontFamily
    val body = bodyFontFamily

    return Typography(
        // Nagłówki na desktopie powiększamy np. o 4sp lub 6sp
        displayLarge = baseline.displayLarge.copy(fontFamily = display).withScale(isDesktop, 12),
        displayMedium = baseline.displayMedium.copy(fontFamily = display).withScale(isDesktop, 8),
        displaySmall = baseline.displaySmall.copy(fontFamily = display).withScale(isDesktop, 8),

        headlineLarge = baseline.headlineLarge.copy(fontFamily = display).withScale(isDesktop, 4),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = display).withScale(isDesktop, 2),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = display).withScale(isDesktop, 2),

        titleLarge = baseline.titleLarge.copy(fontFamily = display).withScale(isDesktop, 2),
        titleMedium = baseline.titleMedium.copy(fontFamily = display).withScale(isDesktop, 2),
        titleSmall = baseline.titleSmall.copy(fontFamily = display).withScale(isDesktop, 2),

        // Tekst główny (body) powiększamy np. o 2sp, żeby był czytelniejszy na monitorze
        bodyLarge = baseline.bodyLarge.copy(fontFamily = body).withScale(isDesktop, 2),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = body).withScale(isDesktop, 2),
        bodySmall = baseline.bodySmall.copy(fontFamily = body).withScale(isDesktop, 1),

        labelLarge = baseline.labelLarge.copy(fontFamily = body).withScale(isDesktop, 1),
        labelMedium = baseline.labelMedium.copy(fontFamily = body).withScale(isDesktop, 1),
        labelSmall = baseline.labelSmall.copy(fontFamily = body).withScale(isDesktop, 1),
    )
}