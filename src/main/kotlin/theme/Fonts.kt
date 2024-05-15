package theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp

object Fonts {

    val WeightMedium = FontWeight.Medium
    val WeightRegular = FontWeight.Normal
    
    val BodyLargeLineHeight = 24.0.sp
    val BodyLargeSize = 16.sp
    val BodyLargeTracking = 0.5.sp
    val BodyLargeWeight = WeightRegular
    val BodyMediumLineHeight = 20.0.sp
    val BodyMediumSize = 14.sp
    val BodyMediumTracking = 0.2.sp
    val BodyMediumWeight = WeightRegular
    val BodySmallLineHeight = 16.0.sp
    val BodySmallSize = 12.sp
    val BodySmallTracking = 0.4.sp
    val BodySmallWeight = WeightRegular
    val DisplayLargeLineHeight = 64.0.sp
    val DisplayLargeSize = 57.sp
    val DisplayLargeTracking = -0.2.sp
    val DisplayLargeWeight = WeightRegular
    val DisplayMediumLineHeight = 52.0.sp
    val DisplayMediumSize = 45.sp
    val DisplayMediumTracking = 0.0.sp
    val DisplayMediumWeight = WeightRegular
    val DisplaySmallLineHeight = 44.0.sp
    val DisplaySmallSize = 36.sp
    val DisplaySmallTracking = 0.0.sp
    val DisplaySmallWeight = WeightRegular
    val HeadlineLargeLineHeight = 40.0.sp
    val HeadlineLargeSize = 32.sp
    val HeadlineLargeTracking = 0.0.sp
    val HeadlineLargeWeight = WeightRegular
    val HeadlineMediumLineHeight = 36.0.sp
    val HeadlineMediumSize = 28.sp
    val HeadlineMediumTracking = 0.0.sp
    val HeadlineMediumWeight = WeightRegular
    val HeadlineSmallLineHeight = 32.0.sp
    val HeadlineSmallSize = 24.sp
    val HeadlineSmallTracking = 0.0.sp
    val HeadlineSmallWeight = WeightRegular
    val LabelLargeLineHeight = 20.0.sp
    val LabelLargeSize = 14.sp
    val LabelLargeTracking = 0.1.sp
    val LabelLargeWeight = WeightMedium
    val LabelMediumLineHeight = 16.0.sp
    val LabelMediumSize = 12.sp
    val LabelMediumTracking = 0.5.sp
    val LabelMediumWeight = WeightMedium
    val LabelSmallLineHeight = 16.0.sp
    val LabelSmallSize = 11.sp
    val LabelSmallTracking = 0.5.sp
    val LabelSmallWeight = WeightMedium
    val TitleLargeLineHeight = 28.0.sp
    val TitleLargeSize = 22.sp
    val TitleLargeTracking = 0.0.sp
    val TitleLargeWeight = WeightRegular
    val TitleMediumLineHeight = 24.0.sp
    val TitleMediumSize = 16.sp
    val TitleMediumTracking = 0.2.sp
    val TitleMediumWeight = WeightMedium
    val TitleSmallLineHeight = 20.0.sp
    val TitleSmallSize = 14.sp
    val TitleSmallTracking = 0.1.sp
    val TitleSmallWeight = WeightMedium

    val open_sans = FontFamily(
        Font(
            resource = "fonts/open-sans.ttf"
        )
    )

    @Composable
    fun getTypography () : Typography {
        return MaterialTheme.typography.copy(
            displayLarge = TextStyle(
                fontFamily = open_sans,
                fontWeight = DisplayLargeWeight,
                fontSize = DisplayLargeSize,
                lineHeight = DisplayLargeLineHeight,
                letterSpacing = DisplayLargeTracking,
            ),
            displayMedium = TextStyle(
                fontFamily = open_sans,
                fontWeight = DisplayMediumWeight,
                fontSize = DisplayMediumSize,
                lineHeight = DisplayMediumLineHeight,
                letterSpacing = DisplayMediumTracking,
            ),
            displaySmall = TextStyle(
                fontFamily = open_sans,
                fontWeight = DisplaySmallWeight,
                fontSize = DisplaySmallSize,
                lineHeight = DisplaySmallLineHeight,
                letterSpacing = DisplaySmallTracking,
            ),
            headlineLarge = TextStyle(
                fontFamily = open_sans,
                fontWeight = HeadlineLargeWeight,
                fontSize = HeadlineLargeSize,
                lineHeight = HeadlineLargeLineHeight,
                letterSpacing = HeadlineLargeTracking,
            ),
            headlineMedium = TextStyle(
                fontFamily = open_sans,
                fontWeight = HeadlineMediumWeight,
                fontSize = HeadlineMediumSize,
                lineHeight = HeadlineMediumLineHeight,
                letterSpacing = HeadlineMediumTracking,
            ),
            headlineSmall = TextStyle(
                fontFamily = open_sans,
                fontWeight = HeadlineSmallWeight,
                fontSize = HeadlineSmallSize,
                lineHeight = HeadlineSmallLineHeight,
                letterSpacing = HeadlineSmallTracking,
            ),
            titleLarge = TextStyle(
                fontFamily = open_sans,
                fontWeight = TitleLargeWeight,
                fontSize = TitleLargeSize,
                lineHeight = TitleLargeLineHeight,
                letterSpacing = TitleLargeTracking,
            ),
            titleMedium = TextStyle(
                fontFamily = open_sans,
                fontWeight = TitleMediumWeight,
                fontSize = TitleMediumSize,
                lineHeight = TitleMediumLineHeight,
                letterSpacing = TitleMediumTracking,
            ),
            titleSmall = TextStyle(
                fontFamily = open_sans,
                fontWeight = TitleSmallWeight,
                fontSize = TitleSmallSize,
                lineHeight = TitleSmallLineHeight,
                letterSpacing = TitleSmallTracking,
            ),
            bodyLarge = TextStyle(
                fontFamily = open_sans,
                fontWeight = BodyLargeWeight,
                fontSize = BodyLargeSize,
                lineHeight = BodyLargeLineHeight,
                letterSpacing = BodyLargeTracking,
            ),
            bodyMedium = TextStyle(
                fontFamily = open_sans,
                fontWeight = BodyMediumWeight,
                fontSize = BodyMediumSize,
                lineHeight = BodyMediumLineHeight,
                letterSpacing = BodyMediumTracking,
            ),
            bodySmall = TextStyle(
                fontFamily = open_sans,
                fontWeight = BodySmallWeight,
                fontSize = BodySmallSize,
                lineHeight = BodySmallLineHeight,
                letterSpacing = BodySmallTracking,
            ),
            labelLarge = TextStyle(
                fontFamily = open_sans,
                fontWeight = LabelLargeWeight,
                fontSize = LabelLargeSize,
                lineHeight = LabelLargeLineHeight,
                letterSpacing = LabelLargeTracking,
            ),
            labelMedium = TextStyle(
                fontFamily = open_sans,
                fontWeight = LabelMediumWeight,
                fontSize = LabelMediumSize,
                lineHeight = LabelMediumLineHeight,
                letterSpacing = LabelMediumTracking,
            ),
            labelSmall = TextStyle(
                fontFamily = open_sans,
                fontWeight = LabelSmallWeight,
                fontSize = LabelSmallSize,
                lineHeight = LabelSmallLineHeight,
                letterSpacing = LabelSmallTracking,
            )
        )
    }


}