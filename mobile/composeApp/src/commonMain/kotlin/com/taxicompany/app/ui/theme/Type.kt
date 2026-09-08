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

// mobileMultiplier: globalny mnoznik czcionki na telefonie (1.0 = bez zmian).
// desktopExtraMultiplier: DODATKOWY mnoznik na wierzch mobileMultiplier, tylko
// na desktopie (>= 700dp) - stad na desktopie efektywny mnoznik to
// mobileMultiplier * desktopExtraMultiplier.
// lineHeight skalujemy tym samym stosunkiem co fontSize, zeby linie tekstu
// nie zaczely sie zageszczac/zachodzic na siebie przy wiekszym foncie.
private fun TextStyle.withScale(isDesktop: Boolean, mobileMultiplier: Float, desktopExtraMultiplier: Float = 1f): TextStyle {
    val multiplier = mobileMultiplier * if (isDesktop) desktopExtraMultiplier else 1f
    val newSize = fontSize.value * multiplier
    val newLineHeight = if (lineHeight.isSp) (lineHeight.value * multiplier).sp else lineHeight
    return this.copy(fontSize = newSize.sp, lineHeight = newLineHeight)
}

private val baseline = Typography()

@Composable
fun getAppTypography(isDesktop: Boolean): Typography {
    val display = displayFontFamily
    val body = bodyFontFamily

    // tu jest globalny procent do zwiększenia/zmniejszenia czcionki (1.15f 115%)
    val mobileScale = 1.15f

    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = display).withScale(isDesktop, mobileScale, 1.25f),
        displayMedium = baseline.displayMedium.copy(fontFamily = display).withScale(isDesktop, mobileScale, 1.18f),
        displaySmall = baseline.displaySmall.copy(fontFamily = display).withScale(isDesktop, mobileScale, 1.18f),

        headlineLarge = baseline.headlineLarge.copy(fontFamily = display).withScale(isDesktop, mobileScale, 1.12f),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = display).withScale(isDesktop, mobileScale, 1.08f),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = display).withScale(isDesktop, mobileScale, 1.08f),

        titleLarge = baseline.titleLarge.copy(fontFamily = display).withScale(isDesktop, mobileScale, 1.08f),
        titleMedium = baseline.titleMedium.copy(fontFamily = display).withScale(isDesktop, mobileScale, 1.08f),
        titleSmall = baseline.titleSmall.copy(fontFamily = display).withScale(isDesktop, mobileScale, 1.08f),

        bodyLarge = baseline.bodyLarge.copy(fontFamily = body).withScale(isDesktop, mobileScale, 1.06f),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = body).withScale(isDesktop, mobileScale, 1.06f),
        bodySmall = baseline.bodySmall.copy(fontFamily = body).withScale(isDesktop, mobileScale, 1.04f),

        labelLarge = baseline.labelLarge.copy(fontFamily = body).withScale(isDesktop, mobileScale, 1.04f),
        labelMedium = baseline.labelMedium.copy(fontFamily = body).withScale(isDesktop, mobileScale, 1.04f),
        labelSmall = baseline.labelSmall.copy(fontFamily = body).withScale(isDesktop, mobileScale, 1.04f),
    )
}