package com.ramotion.circlemenu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

import androidx.annotation.FloatRange


class RingEffectView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

  private val mPaint: Paint
  private val mPath = Path()

  private var mAngle: Float = 0.toFloat()
  var startAngle: Float = 0.toFloat()
    set(@FloatRange(from = 0.0, to = 360.0) startAngle) {
      field = startAngle
      mAngle = 0f

      val sw = mPaint.strokeWidth * 0.5f
      val radius = this.radius - sw

      mPath.reset()
      val x = Math.cos(Math.toRadians(startAngle.toDouble())).toFloat() * radius
      val y = Math.sin(Math.toRadians(startAngle.toDouble())).toFloat() * radius
      mPath.moveTo(x, y)
    }
  var radius: Int = 0

  var angle: Float
    get() = mAngle
    set(@FloatRange(from = 0.0, to = 360.0) angle) {
      val diff = angle - mAngle
      val stepCount = (diff / STEP_DEGREE).toInt()
      val stepMod = diff % STEP_DEGREE

      val sw = mPaint.strokeWidth * 0.5f
      val radius = this.radius - sw

      for (i in 1..stepCount) {
        val stepAngel = startAngle + mAngle + (STEP_DEGREE * i).toFloat()
        val x = Math.cos(Math.toRadians(stepAngel.toDouble())).toFloat() * radius
        val y = Math.sin(Math.toRadians(stepAngel.toDouble())).toFloat() * radius
        mPath.lineTo(x, y)
      }

      val stepAngel = startAngle + mAngle + (STEP_DEGREE * stepCount).toFloat() + stepMod
      val x = Math.cos(Math.toRadians(stepAngel.toDouble())).toFloat() * radius
      val y = Math.sin(Math.toRadians(stepAngel.toDouble())).toFloat() * radius
      mPath.lineTo(x, y)

      mAngle = angle

      invalidate()
    }

  init {

    mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    mPaint.style = Paint.Style.STROKE
    mPaint.strokeCap = Paint.Cap.ROUND
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    if (!mPath.isEmpty) {
      canvas.save()
      canvas.translate((width / 2).toFloat(), (height / 2).toFloat())
      canvas.drawPath(mPath, mPaint)
      canvas.restore()
    }
  }

  override fun setAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float) {
    mPaint.alpha = (255 * alpha).toInt()
    invalidate()
  }

  override fun getAlpha(): Float {
    return (mPaint.alpha / 255).toFloat()
  }

  fun setStrokeColor(color: Int) {
    mPaint.color = color
  }

  fun setStrokeWidth(width: Int) {
    mPaint.strokeWidth = width.toFloat()
  }

  companion object {

    private val STEP_DEGREE = 5
  }

}
