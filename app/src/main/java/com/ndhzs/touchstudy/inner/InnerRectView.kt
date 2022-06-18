package com.ndhzs.touchstudy.inner

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.ndhzs.touchstudy.VibratorUtil
import com.ndhzs.touchstudy.widget.BaseRectView
import kotlin.math.abs

/**
 * 内部拦截法是将事件交给自己处理，相比外部拦截法可以降低滑动处理的耦合性
 *
 * 因为内部拦截法主动权在子 View 上，所以由 RectView 来决定长按的开启和关闭
 *
 *
 * ## 在移动到屏幕边缘时该怎样使 ScrollView 滚动
 * 这个对于 外部拦截法 和 内部拦截法 都无解，只能使用接口来解耦合，想优雅的解决只有使用嵌套滑动解决方案了
 *
 * 这里我就不上代码了，大致思路是写个接口，定义向上滚动和向下滚动的方法，ScrollView 实现该接口，
 * 给 ectView 传入该接口，在滑到边缘时调用即可。但注意：ScrollView 滑动后会导致 RectView 坐标系的移动，难度还是挺高的
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/6/18 19:50
 */
class InnerRectView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : BaseRectView(context, attrs, defStyleAttr, defStyleRes) {
  
  private var mInitialX = 0
  private var mInitialY = 0
  private var mLastMoveX = 0
  private var mLastMoveY = 0
  private var mDiffMoveX = 0
  private var mDiffMoveY = 0
  
  private var mIsAllowDraw = false
  
  /**
   * 很重要的一个变量，用来决定是滑动行为的最小移动距离，不同的手机该变量得到的值不同
   *
   * 意思就是只要你移动给的距离小于它，我就认为你手指没有移动
   *
   * 该变量在官方很多滑动控件中都用到了，比如：ScrollView、RecyclerView 等
   */
  private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
  
  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    val x = event.x.toInt()
    val y = event.y.toInt()
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        mInitialX = x
        mInitialY = y
        mLastMoveX = x
        mLastMoveY = y
        mDiffMoveX = 0
        mDiffMoveY = 0
        mIsAllowDraw = false // 还原
        mLongPressRunnable.start()
        parent.requestDisallowInterceptTouchEvent(true) // 一来就让所有父布局不允许拦截事件
      }
      MotionEvent.ACTION_MOVE -> {
        mDiffMoveX = x - mLastMoveX
        mDiffMoveY = y - mLastMoveY
        mLastMoveX = x
        mLastMoveY = y
        if (!mIsAllowDraw) {
          if (!mLongPressRunnable.isInLongPress()) {
            if (abs(mDiffMoveX) > mTouchSlop || abs(mDiffMoveY) > mTouchSlop) {
              // 走到这里说明长按没有激活，并且移动距离大于了 mTouchSlop
              mLongPressRunnable.cancel()
              parent.requestDisallowInterceptTouchEvent(false) // 允许父布局拦截事件
            }
          } else {
            // 这里说明已经处于长按状态了
            mIsAllowDraw = true
          }
        } else {
          drawRect(mInitialX, mInitialY, x, y) // 绘制矩形
        }
      }
      MotionEvent.ACTION_UP -> {
        /*
        * 走到这里只有一种情况：
        * 1、ScrollView 没有拦截事件
        * */
        mLongPressRunnable.cancel() // 可能它在长按激活前就抬起了手
      }
      MotionEvent.ACTION_CANCEL -> {
        /*
        * 走到这里只有一种情况：
        * 1、父布局拦截了事件，但并不一定是 ScrollView 拦截的
        * */
        mLongPressRunnable.cancel()
      }
    }
    return true // 这里需要返回 true，代表子 View 会处理事件
  }
  
  /**
   * 长按的 Runnable
   *
   * 使用匿名内部类，封装长按开启和取消的逻辑，官方源码中也经常这样写
   */
  private val mLongPressRunnable = object : Runnable {
    
    private var mIsInLongPress= false
    
    // 与前面 mTouchSlop 类似，也是系统中定义好了的长按需要的时间
    private val mLongPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
    
    override fun run() {
      mIsInLongPress = true
      onLongPress()
    }
    
    fun isInLongPress(): Boolean = mIsInLongPress
    
    fun start() {
      postDelayed(this, mLongPressTimeout)
      mIsInLongPress = false
    }
    
    fun cancel() {
      removeCallbacks(this)
      mIsInLongPress = false
    }
  }
  
  private fun onLongPress() {
    // 来个震动
    VibratorUtil.start(context, 30)
    // 内部拦截法就可以在刚激活时就绘制矩形
    drawRect(mInitialX, mInitialY, mLastMoveX, mLastMoveY)
  }
}