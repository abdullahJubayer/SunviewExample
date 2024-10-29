package com.sample.daynightviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

class TimerView @JvmOverloads constructor(
    ctx: Context,
    attrSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(ctx, attrSet, defStyleAttr, defStyleRes) {
    private var sunImage: Drawable? = ContextCompat.getDrawable(context, R.drawable.baseline_sunny)
    private var sunImageBounds = Rect()
    private var sunPosition = PointF()
    private var progress = 0f
    private var circleRadius = 0f
    private var completedColor = Color.GREEN
    private var remainingColor = Color.GRAY
    private var startTime = ""
    private var startTimeTitle = ""
    private var timerTitleSize = 40f
    private var endTime = ""
    private var endTimeTitle = ""
    private var timerTextSize = 30f
    private val paintText = Paint()
    private var dX = 0f
    private var completedPath= Path()
    private var remainingPath= Path()

    private val paintShape = Paint().apply {
        strokeWidth = 10f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    init {
        context.theme.obtainStyledAttributes(
            attrSet,
            R.styleable.timer_view,
            0, 0
        ).apply {
            try {
                getColor(R.styleable.timer_view_completeColor, completedColor).let {
                    completedColor = it
                }
                getColor(R.styleable.timer_view_remainingColor, remainingColor).let {
                    remainingColor = it
                }
                getDrawable(R.styleable.timer_view_icon)?.let {
                    sunImage = it
                }
                getString(R.styleable.timer_view_startTime)?.let {
                    startTime = it
                }
                getString(R.styleable.timer_view_startTimeTitle)?.let {
                    startTimeTitle = it
                }
                getDimension(R.styleable.timer_view_timerTitleSize,timerTitleSize).let {
                    timerTitleSize = it
                }
                getString(R.styleable.timer_view_endTime)?.let {
                    endTime = it
                }
                getString(R.styleable.timer_view_endTimeTitle)?.let {
                    endTimeTitle = it
                }
                getDimension(R.styleable.timer_view_timerTextSize, timerTextSize).let {
                    timerTextSize = it
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        circleRadius = (0.4f * width)
        sunPosition = getXYofAngle(180.0, circleRadius)
    }

    fun setProgress(useProgress: Int) {
        progress = (180.0 * (useProgress / 100.0)).toFloat()
        sunPosition = getXYofAngle(180.0 + progress, circleRadius)
        invalidate()
    }

    fun setStartTime(startTime:String,startTimeTitle:String) {
        this.startTime= startTime
        this.startTimeTitle= startTimeTitle
    }

    fun setEndTime(endTime:String,endTimeTitle:String) {
        this.endTime= endTime
        this.endTimeTitle= endTimeTitle
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawHalfCycle(canvas)
        drawText(canvas)
    }

    private fun drawHalfCycle(canvas: Canvas){
        drawRemainingArc(canvas)
        drawCompleteArc(canvas)
        drawSun(canvas)
    }

    private fun drawRemainingArc(canvas: Canvas) {
        paintShape.apply {
            color = remainingColor
        }
        val start = getXYofAngle((180.0 + progress), circleRadius)
        remainingPath.reset()
        remainingPath.moveTo(start.x,start.y)
        for (i in (180 + progress).toInt()..364) {
            val points = getXYofAngle(i.toDouble(), circleRadius)
            if ((i - 180) % 10 < 5) {
                remainingPath.lineTo(points.x,points.y)
            }else{
                remainingPath.moveTo(points.x,points.y)
            }
        }
        canvas.drawPath(remainingPath,paintShape)
    }

    private fun drawCompleteArc(canvas: Canvas) {
        paintShape.apply {
            color = completedColor
        }
        val start = getXYofAngle(180.0, circleRadius)
        completedPath.reset()
        completedPath.moveTo(start.x,start.y)
        for (i in 180..(180 + progress).toInt()) {
            val points = getXYofAngle(i.toDouble(), circleRadius)
            if ((i - 180) % 10 < 5) {
                completedPath.lineTo(points.x,points.y)
            }else{
                completedPath.moveTo(points.x,points.y)
            }
        }
        canvas.drawPath(completedPath,paintShape)
    }

    private fun drawSun(canvas: Canvas) {
        sunImage?.setBounds(
            sunPosition.x.toInt() - 50, sunPosition.y.toInt() - 50,
            sunPosition.x.toInt() + 50, sunPosition.y.toInt() + 50
        )
        sunImage?.let {
            it.draw(canvas)
            sunImageBounds = it.copyBounds()
        }
    }

    private fun drawText(canvas: Canvas) {
        val textBounds = Rect()

        val startPoint = getXYofAngle(180.0, circleRadius)
        drawTime(canvas, textBounds, startPoint, startTime, startTimeTitle)


        val endPoint = getXYofAngle(364.0, circleRadius)
        drawTime(canvas, textBounds, endPoint, endTime, endTimeTitle)
    }

    private fun drawTime(
        canvas: Canvas,
        textBounds: Rect,
        point: PointF,
        time: String,
        name: String
    ) {
        paintText.apply {
            color = completedColor
            textSize = timerTitleSize
            getTextBounds(time, 0, time.length, textBounds)
        }
        val timeX = point.x - (textBounds.width() / 2f)
        val timeY = point.y - (textBounds.height() / 2f) + (50f + textBounds.height())
        canvas.drawText(time, timeX, timeY, paintText)

        paintText.apply {
            color = remainingColor
            textSize = timerTextSize
            getTextBounds(name, 0, name.length, textBounds)
        }
        val nameX = point.x - (textBounds.width() / 2f)
        val nameY = timeY + timerTextSize
        canvas.drawText(name, nameX, nameY, paintText)
    }

    private fun getXYofAngle(angle: Double, radius: Float): PointF {
        val radians = Math.toRadians(angle)
        val x = (radius * Math.cos(radians) + radius).toFloat() + (0.1f * width)
        val y = (radius * Math.sin(radians) + radius).toFloat() + (0.1f * width)
        return PointF(x, y)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (sunImageBounds.contains(event.x.toInt(), event.y.toInt())) {
                    dX = event.x - sunPosition.x
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = (event.x - dX).coerceIn(0.1f * width, 0.9f * width)
                val progress = calculatePercentage(moveX,0.9f * width)
                setProgress(progress.toInt())
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun calculatePercentage(part: Float, whole: Float): Float {
        return if (whole != 0f) {
            (part / whole) * 100
        } else {
            0f
        }
    }
}