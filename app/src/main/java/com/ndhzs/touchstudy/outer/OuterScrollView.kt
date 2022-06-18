package com.ndhzs.touchstudy.outer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.ScrollView
import com.ndhzs.touchstudy.VibratorUtil
import kotlin.math.abs

/**
 * 自定义 ScrollView，注释写在了 [OuterRectView] 中
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/6/18 17:47
 */
class OuterScrollView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : ScrollView(context, attrs, defStyleAttr, defStyleRes) {
  
  private var mInitialX = 0
  private var mInitialY = 0
  private var mLastMoveX = 0
  private var mLastMoveY = 0
  private var mDiffMoveX = 0
  private var mDiffMoveY = 0
  
  /**
   * 很重要的一个变量，用来决定是滑动行为的最小移动距离，不同的手机该变量得到的值不同
   *
   * 意思就是只要你移动给的距离小于它，我就认为你手指没有移动
   *
   * 该变量在官方很多滑动控件中都用到了，比如：ScrollView、RecyclerView 等
   */
  private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
  
  /**
   * 在之前我的课上总结过 onInterceptTouchEvent() 方法一些规律：
   * - 只有一次返回 true 的机会，返回就拦截
   */
  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    val x = ev.x.toInt()
    val y = ev.y.toInt()
    when (ev.action) {
      MotionEvent.ACTION_DOWN -> {
        mInitialX = x
        mInitialY = y
        mLastMoveX = x
        mLastMoveY = y
        mDiffMoveX = 0
        mDiffMoveY = 0
        // 注意 ScrollView Down 事件必须要调用 super，不然之后不能滚动
        val isIntercept = super.onInterceptTouchEvent(ev)
        if (!isIntercept) {
          /*
          * 可能你会对这里感觉很奇怪，为什么要这样？
          * 因为如果 ScrollView 正处于惯性滑动，此时你立马触摸屏幕，ScrollView 它会直接拦截这个事件，
          * 所以这里只对 isIntercept = false 时才开启长按延时
          * */
          mLongPressRunnable.start()
        }
        return isIntercept
      }
      MotionEvent.ACTION_MOVE -> {
        mDiffMoveX = x - mLastMoveX
        mDiffMoveY = y - mLastMoveY
        mLastMoveX = x
        mLastMoveY = y
        if (!mLongPressRunnable.isInLongPress()) {
          // 在 Move 中判断滑动的距离是否超过 mTouchSlop
          if (abs(mDiffMoveX) > mTouchSlop || abs(mDiffMoveY) > mTouchSlop) {
            mLongPressRunnable.cancel()
            // 这里 true 会导致直接拦截子 View 事件，子 View 会立马收到一个 CANCEL 事件
            return true
          }
        }
      }
      MotionEvent.ACTION_UP -> {
        mLongPressRunnable.cancel() // 可能它在长按激活前就抬起了手
      }
      MotionEvent.ACTION_CANCEL -> {
        mLongPressRunnable.cancel()
      }
    }
    return false
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
  }
}