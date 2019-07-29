package com.ivianuu.compose.material

import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import androidx.compose.Ambient
import androidx.compose.ambient
import androidx.compose.unaryPlus
import androidx.ui.graphics.Color
import androidx.ui.material.ripple.CurrentRippleTheme
import com.ivianuu.compose.view.ViewDsl
import com.ivianuu.compose.view.set

val RippleSurfaceAmbient = Ambient.of<Color>("RippleSurfaceAmbient")

fun <T : View> ViewDsl<T>.rippleBackground(bounded: Boolean) {
    val surfaceColor = +ambient(RippleSurfaceAmbient)
    val rippleTheme = +ambient(CurrentRippleTheme)
    val rippleColor = rippleTheme.colorCallback(surfaceColor)
    rippleBackground(rippleColor, bounded)
}

fun <T : View> ViewDsl<T>.rippleBackground(
    color: Color,
    bounded: Boolean
) {
    set(listOf(color, bounded)) {
        background = createRippleDrawable(color, bounded)
    }
}

private fun createRippleDrawable(color: Color, bounded: Boolean): RippleDrawable {
    return RippleDrawable(
        ColorStateList(arrayOf(intArrayOf()), intArrayOf(color.toArgb())),
        null,
        if (bounded) ColorDrawable(Color.White.toArgb()) else null
    )
}