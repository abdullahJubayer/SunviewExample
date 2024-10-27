package com.sample.daynightviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

class TimerView  @JvmOverloads constructor(ctx: Context, attrSet: AttributeSet? = null, defStyleAttr : Int = 0, defStyleRes:Int =0): View(ctx,attrSet,defStyleAttr,defStyleRes)  {
    private val sunImage: Drawable? = ContextCompat.getDrawable(context, R.drawable.baseline_sunny)
    private var sunPosition = PointF()
    private var percentage = 0.0
    private var paddingX = 0
    private var paddingY = 0
    private var circleRadius = 0f

    private val paintShape = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintText = Paint().apply {
        textSize = 40f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateSize()
    }

    private fun calculateSize(){
        paddingX = paddingLeft + paddingRight + marginLeft + marginRight
        paddingY = paddingTop + paddingBottom + marginTop + marginBottom
        circleRadius = (0.5f * width) - paddingX
        sunPosition = getXYofAngle(180.0, circleRadius)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSun(canvas)
        drawText(canvas)
    }

    private fun drawSun(canvas: Canvas) {
        drawCompleteArc(canvas)
        drawRemainingArc(canvas)
        sunImage?.setBounds(
            sunPosition.x.toInt() - 50, sunPosition.y.toInt() - 50,
            sunPosition.x.toInt() + 50, sunPosition.y.toInt() + 50
        )
        sunImage?.draw(canvas)
    }

    private fun drawCompleteArc(canvas: Canvas) {
        paintShape.apply {
            color = Color.GRAY
        }
        for (i in 180.. (180 + percentage).toInt()) {
            if ((i - 180) % 10 < 5) {
                val points = getXYofAngle(i.toDouble(), circleRadius)
                canvas.drawCircle(points.x, points.y, 5f, paintShape)
            }
        }
    }

    private fun drawRemainingArc(canvas: Canvas) {
        paintShape.apply {
            color = Color.GRAY
        }
        for (i in (180 + percentage).toInt()..364) {
            if ((i - 180) % 10 < 5) {
                val points = getXYofAngle(i.toDouble(), circleRadius)
                canvas.drawCircle(points.x, points.y, 5f, paintShape)
            }
        }
    }

    private fun drawText(canvas: Canvas){
        val textBounds = Rect()

        val startPoint = getXYofAngle(180.0, circleRadius)
        drawTime(canvas,textBounds,startPoint,"07:09 AM","Sunrise")


        val endPoint = getXYofAngle(364.0, circleRadius)
        drawTime(canvas,textBounds,endPoint,"07:09 AM","Sunset")
    }

    private fun drawTime(canvas: Canvas, textBounds: Rect,point:PointF,time: String,name:String) {
        paintText.apply {
            color = Color.GREEN
            textSize = 50f
            getTextBounds(time, 0, time.length, textBounds)
        }
        val timeX = point.x - (textBounds.width() / 2f)
        val timeY = point.y - (textBounds.height() / 2f) + 50f
        canvas.drawText(time,timeX,timeY,paintText)

        paintText.apply {
            color = Color.GRAY
            textSize = 30f
            getTextBounds(name, 0, name.length, textBounds)
        }
        val nameX = point.x - (textBounds.width() / 2f)
        val nameY = point.y - (textBounds.height() / 2f) + 80f
        canvas.drawText(name,nameX,nameY,paintText)
    }

    private fun getXYofAngle(angle: Double, radius: Float): PointF {
        val radians = Math.toRadians(angle)
        val x = (radius * Math.cos(radians) +  radius).toFloat() +  (paddingX / 2f)
        val y = (radius * Math.sin(radians) + radius).toFloat()  + (paddingY / 2f)
        return PointF(x, y)
    }

    fun moveViews(progress : Int){
        percentage = 180.0 * (progress / 100.0)
        sunPosition = getXYofAngle(180.0 + percentage , 0.4f * width)
        invalidate()
    }

}