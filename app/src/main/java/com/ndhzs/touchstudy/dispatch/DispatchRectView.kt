package com.ndhzs.touchstudy.dispatch

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.ndhzs.touchstudy.widget.BaseRectView

/**
 * 由于 [DispatchScrollView] 重写了 dispatchTouchEvent，会中途停止事件向下分发，
 * 但自身收到第一个 Move 事件时一定就是长按已经激活的时候，
 * 所以这个类就可以设计得很简单，直接绘制矩形即可
 *
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/6/18 21:09
 */
class DispatchRectView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : BaseRectView(context, attrs, defStyleAttr, defStyleRes) {
  
  private var mInitialX = 0
  private var mInitialY = 0
  
  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    val x = event.x.toInt()
    val y = event.y.toInt()
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        mInitialX = x
        mInitialY = y
      }
      MotionEvent.ACTION_MOVE -> {
        // 收到 Move 事件时一定是长按激活的时候
        drawRect(mInitialX, mInitialY, x, y)
      }
      MotionEvent.ACTION_UP -> {
        /*
        * 走到这里只有一种情况：
        * 1、ScrollView 没有拦截事件
        * */
      }
      MotionEvent.ACTION_CANCEL -> {
        /*
        * 走到这里只有一种情况：
        * 1、父布局拦截了事件，但并不一定是 ScrollView 拦截的
        * */
      }
    }
    return true // 这里需要返回 true，代表子 View 会处理事件
  }
}