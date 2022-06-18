package com.ndhzs.touchstudy.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.min

/**
 *
 * 先写一个父类，实现绘制按住后绘制矩形的操作
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/6/18 17:26
 */
open class BaseRectView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
  
  private val mRect = Rect()
  private val mPaint = Paint().apply {
    color = Color.GRAY
    style = Paint.Style.FILL
  }
  
  fun drawRect(l: Int, t: Int, r: Int, b: Int) {
    mRect.set(minOf(l, r), minOf(t, b), maxOf(l, r), maxOf(t, b))
    invalidate()
  }
  
  override fun onDraw(canvas: Canvas) {
    canvas.drawRect(mRect, mPaint)
  }
}