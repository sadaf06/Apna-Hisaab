package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.utils.IncomeVisibilityManager
import com.example.ui.theme.Palette

// Premium Custom Vector Eye Icon (Visible state)
private var _eyeIcon: ImageVector? = null
val EyeIcon: ImageVector
    get() {
        if (_eyeIcon != null) return _eyeIcon!!
        _eyeIcon = ImageVector.Builder(
            name = "CustomEyeOpen",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White), // Tint will override color
                stroke = null,
                strokeAlpha = 1.0f,
                fillAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f
            ) {
                moveTo(12f, 4.5f)
                curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
                curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
                curveTo(17f, 19.5f, 21.27f, 16.39f, 23f, 12f)
                curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
                close()
                moveTo(12f, 17f)
                curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
                curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
                curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
                curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
                close()
                moveTo(12f, 9f)
                curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
                curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
                curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
                curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
                close()
            }
        }.build()
        return _eyeIcon!!
    }

// Premium Custom Vector Eye Slash Icon (Hidden state)
private var _eyeOffIcon: ImageVector? = null
val EyeOffIcon: ImageVector
    get() {
        if (_eyeOffIcon != null) return _eyeOffIcon!!
        _eyeOffIcon = ImageVector.Builder(
            name = "CustomEyeClosed",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White), // Tint will override color
                stroke = null,
                strokeAlpha = 1.0f,
                fillAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f
            ) {
                moveTo(12f, 4.5f)
                curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
                curveTo(2.13f, 14.18f, 3.86f, 15.93f, 6f, 17.15f)
                lineTo(3.27f, 19.88f)
                lineTo(4.68f, 21.29f)
                lineTo(20.32f, 5.66f)
                lineTo(18.91f, 4.25f)
                lineTo(15.9f, 7.25f)
                curveTo(14.71f, 6.7f, 13.39f, 4.5f, 12f, 4.5f)
                close()
                moveTo(12f, 17f)
                curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
                curveTo(7f, 11.23f, 7.18f, 10.5f, 7.5f, 9.85f)
                lineTo(14.15f, 16.5f)
                curveTo(13.5f, 16.82f, 12.77f, 17f, 12f, 17f)
                close()
                moveTo(12f, 7f)
                curveTo(12.77f, 7f, 13.5f, 7.18f, 14.15f, 7.5f)
                lineTo(12.3f, 9.35f)
                curveTo(12.2f, 9.35f, 12.1f, 9.35f, 12f, 9.35f)
                curveTo(10.54f, 9.35f, 9.35f, 10.54f, 9.35f, 12f)
                curveTo(9.35f, 12.1f, 9.35f, 12.2f, 9.35f, 12.3f)
                lineTo(11.15f, 10.5f)
                curveTo(11.39f, 10.18f, 11.68f, 9.89f, 12f, 7f)
                close()
                moveTo(23f, 12f)
                curveTo(21.27f, 16.39f, 17f, 19.5f, 12f, 19.5f)
                curveTo(11.11f, 19.5f, 10.25f, 19.4f, 9.42f, 19.22f)
                lineTo(11.02f, 17.62f)
                curveTo(11.34f, 17.62f, 11.67f, 17.68f, 12f, 17.68f)
                curveTo(15.13f, 17.68f, 17.68f, 15.13f, 17.68f, 12f)
                curveTo(17.68f, 11.67f, 17.66f, 11.34f, 17.62f, 11.02f)
                lineTo(19.22f, 9.42f)
                curveTo(19.4f, 10.25f, 19.5f, 11.11f, 19.5f, 12f)
                close()
            }
        }.build()
        return _eyeOffIcon!!
    }

@Composable
fun IncomeHideButton(
    userId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isVisible by IncomeVisibilityManager.isIncomeVisible.collectAsState()
    
    val icon = if (isVisible) EyeIcon else EyeOffIcon
    val tint = if (isVisible) Palette.Success else Color.Gray
    
    Icon(
        imageVector = icon,
        contentDescription = if (isVisible) "Hide Income" else "Show Income",
        tint = tint,
        modifier = modifier
            .size(24.dp)
            .clickable {
                IncomeVisibilityManager.toggleVisibility(context, userId)
            }
    )
}
