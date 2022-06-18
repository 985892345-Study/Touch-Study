package com.ndhzs.touchstudy.dispatch

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.ScrollView
import com.ndhzs.touchstudy.VibratorUtil
import kotlin.math.abs

/**
 * 重写 dispatchTouchEvent 也可以实现需求，这个方法不是很推荐，但可以作为参考
 * （不推荐原因：重写 dispatchTouchEvent 会导致事件不按正常情况下分发，会导致其他人看不懂）
 *
 *
 * 关键思路是：不按照正常的事件分发机制来分发事件，在长按激活前不将事件分发给子 View，在长按激活后再分发。
 * 如果按照正常的分发机制，想要实现一段时间内不向子 View 分发事件是无法实现的，只能通过重写 dispatchTouchEvent 才能实现
 *
 *
 * 这个算是 外部拦截法 的一种特殊方法，外部拦截法需要在子 View 对长按未激活前的 Move 事件进行判断，但这个方法可以使子 View 不进行判断，
 * 原理就是不把那段事件分发给子 View
 *
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/6/18 20:47
 */
class DispatchScrollView @JvmOverloads constructor(
  context: Context?,
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
  
  // 是否需要在 dispatchTouchEvent 中断事件向下分发
  private var mIsNeedBreakOff = true
  
  /**
   * 很重要的一个变量，用来决定是滑动行为的最小移动距离，不同的手机该变量得到的值不同
   *
   * 意思就是只要你移动给的距离小于它，我就认为你手指没有移动
   *
   * 该变量在官方很多滑动控件中都用到了，比如：ScrollView、RecyclerView 等
   */
  private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
  
  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
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
        mLongPressRunnable.start()
        mIsNeedBreakOff = true // 还原
      }
      MotionEvent.ACTION_MOVE -> {
        mDiffMoveX = x - mLastMoveX
        mDiffMoveY = y - mLastMoveY
        mLastMoveX = x
        mLastMoveY = y
        if (mIsNeedBreakOff) {
          if (!mLongPressRunnable.isInLongPress()) {
            if (abs(mDiffMoveX) > mTouchSlop || abs(mDiffMoveY) > mTouchSlop) {
              mIsNeedBreakOff = false
              mLongPressRunnable.cancel()
            } else {
              /*
              * 这里 return true 可以终止事件向下传递，意思就是 MOVE 事件会一直卡在这里
              * onInterceptTouchEvent 和 onTouchEvent 将会收不到 MOVE 这个事件，将不会被调用
              * 所以这里可以用来等待长按时间结束。
              * */
              return true
            }
          }
        }
      }
      MotionEvent.ACTION_UP -> {
        mLongPressRunnable.cancel()
      }
      MotionEvent.ACTION_CANCEL -> {
        mLongPressRunnable.cancel()
      }
    }
    return super.dispatchTouchEvent(ev)
  }
  
  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    if (ev.action == MotionEvent.ACTION_DOWN) {
      // Down 事件必须调用 super，不然后面不能滚动
      val isIntercept = super.onInterceptTouchEvent(ev)
      if (isIntercept) {
        // 这个判断与 OuterScrollView 的注释类似，但这里是 cancel 掉长按，因为长按是在 dispatchTouchEvent 中开启的
        mLongPressRunnable.cancel()
      }
      return isIntercept
    }
    if (!mIsNeedBreakOff && !mLongPressRunnable.isInLongPress()) {
      // 此时肯定是自己拦截事件
      return true
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
    mIsNeedBreakOff = false
  }
}