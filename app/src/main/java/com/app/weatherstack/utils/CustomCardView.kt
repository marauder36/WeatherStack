package com.app.weatherstack.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout

class CustomCardView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    private var cornerRadius = 0f

    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        invalidate() // Tell the view to redraw
    }

    override fun dispatchDraw(canvas: Canvas) {
        val path = Path()
        // Define a rect with the view's dimensions
        val rect = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat())
        // Add rounded corners to the path
        path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
    }
}
