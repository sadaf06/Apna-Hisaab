package com.example.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

object PremiumToast {
    fun show(context: Context, message: String, longDuration: Boolean = false) {
        val density = context.resources.displayMetrics.density
        fun dp(value: Float): Int = (value * density).toInt()

        // Root horizontal LinearLayout
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(24f), dp(18f), dp(24f), dp(18f))
            minimumWidth = dp(240f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = dp(18f).toFloat()
                clipToOutline = true
            }
        }

        // Background GradientDrawable TOP_BOTTOM colors #F21D2230 -> #F2131725, cornerRadius 30dp, stroke 1dp #2EFFFFFF
        val bgDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.parseColor("#F21D2230"),
                Color.parseColor("#F2131725")
            )
        ).apply {
            cornerRadius = dp(30f).toFloat()
            setStroke(dp(1f), Color.parseColor("#2EFFFFFF"))
        }
        rootLayout.background = bgDrawable

        // Accent dot: bump to 10dp (was 8), rightMargin 14dp, add a soft mint glow halo
        val dotContainer = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(dp(18f), dp(18f)).apply {
                rightMargin = dp(14f)
            }
        }

        // Faint mint glow halo (slightly larger background circle with low opacity mint color)
        val glowView = View(context).apply {
            layoutParams = FrameLayout.LayoutParams(dp(18f), dp(18f))
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#4D2DD4BF")) // ~30% opacity mint glow
            }
        }

        // Solid core dot: 10dp, GradientDrawable TL_BR #A78BFA -> #2DD4BF
        val solidDotView = View(context).apply {
            val solidDotSize = dp(10f)
            layoutParams = FrameLayout.LayoutParams(solidDotSize, solidDotSize).apply {
                gravity = Gravity.CENTER
            }
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.parseColor("#A78BFA"),
                    Color.parseColor("#2DD4BF")
                )
            ).apply {
                shape = GradientDrawable.OVAL
            }
        }

        dotContainer.addView(glowView)
        dotContainer.addView(solidDotView)
        rootLayout.addView(dotContainer)

        // TextView: text=message, color #F6F7FB, 16sp, typeface sans-serif-medium, letterSpacing 0.01, lineSpacingExtra 2dp, maxLines 3
        val textView = TextView(context).apply {
            text = message
            setTextColor(Color.parseColor("#F6F7FB"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                letterSpacing = 0.01f
            }
            setLineSpacing(dp(2f).toFloat(), 1.0f)
            maxLines = 3
        }
        rootLayout.addView(textView)

        // Create and show the toast
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navBarHeight = if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
        val totalYOffset = dp(110f) + navBarHeight

        val toast = Toast(context.applicationContext ?: context).apply {
            duration = if (longDuration) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            view = rootLayout
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, totalYOffset)
        }
        toast.show()
    }
}
