package com.ndhzs.touchstudy.outer

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import com.ndhzs.touchstudy.widget.BaseRectView
import kotlin.math.abs

/**
 * 外部拦截法比较简单，主要思想是在 ScrollView 中拦截，把长按的判断交给它处理，
 * 如果它在长按前滑动距离过大，就把事件给拦截了，进行滚动
 *
 * ## 注意
 * - Down 事件必须传递给子 View(即 Down 时不拦截)，如果 ScrollView 在 Down 事件拦截，那么子 View 不会收到任何事件
 *
 * ## 情况分析
 * 1、长按激活前
 *
 * 1.1、移动距离小于阈值
 * - ScrollView 和 RectView 都等待长按，不做其他处理
 * - RectView 此时虽然会收到 Move 事件，但不允许绘制矩形
 *
 * 1.2、移动距离大于阈值
 * - ScrollView 直接拦截 RectView 事件
 * - RectView 收到 CANCEL 事件，做收尾工作
 * - 取消长按的 Runnable
 *
 * 2、长按激活后
 * 因为前面在移动距离大于阈值时会取消长按，所以长按激活说明事件肯定是 RectView 在处理，且已经绘制了矩形。
 * 故这里 ScrollView 不做任何处理
 *
 *
 * ## 谁来开启长按
 * 意思是该由谁来开启整个事件的长按，因为长按是发送一个延时的 Runnable，这个主动权该交到谁手上？
 *
 * 因为是外部拦截法，会由 ScrollView 来决定事件是否拦截，所以该由 ScrollView 开启长按
 *
 *
 * ### ScrollView 开启长按，那 RectView 什么时候才能开始绘图呢？
 * 意思是 ScrollView 决定了长按的开启和取消，但在长按激活前 RectView 不允许绘图，如果长按激活了，需要 ScrollView 来通知它，
 * 这样就会造成耦合，这也是外部拦截法的一个缺点
 *
 * 想优雅解决的话可以去看 重写事件分发法 和 内部拦截法
 *
 *
 * ## 在移动到屏幕边缘时该怎样使 ScrollView 滚动
 * 这个对于 外部拦截法 和 内部拦截法 都无解，只能使用接口来解耦合
 *
 * 这里我就不上代码了，大致思路是写个接口，定义向上滚动和向下滚动的方法，ScrollView 实现该接口，
 * 给 RectView 传入该接口，在滑到边缘时调用即可（滑到边缘还涉及到坐标系的装换，如果在 ScrollView 中实现就不用）。
 * 但注意：ScrollView 滑动后会导致 RectView 坐标系的移动，难度还是挺高的
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/6/18 17:48
 */
class OuterRectView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : BaseRectView(context, attrs, defStyleAttr, defStyleRes), OuterScrollView.OnLongPressListener {
  
  private var mInitialX = 0
  private var mInitialY = 0
  private var mLastMoveX = 0
  private var mLastMoveY = 0
  private var mDiffMoveX = 0
  private var mDiffMoveY = 0
  
  // 是否允许绘制矩形，由是否激活了长按来决定
  private var mIsAllowDraw = false
  
  
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
        mIsAllowDraw = false // 还原
      }
      MotionEvent.ACTION_MOVE -> {
        mDiffMoveX = x - mLastMoveX
        mDiffMoveY = y - mLastMoveY
        mLastMoveX = x
        mLastMoveY = y
        if (mIsAllowDraw) {
          /*
          * 只有允许绘制的时候才能绘制矩形
          *
          * 在什么时候会允许绘制？
          * 答：长按激活过后
          *
          * 为什么要额外提供这个变量？
          * 答：因为 RectView 会收到长按激活前的 Move 事件，所以需要做这个判断，
          * 因此外部拦截法增加了 ScrollView 与 RectView 之间的耦合度。
          * 如果只想解决这一个单独的问题，可以去看 重写 dispatchTouchEvent 法；
          * 如果想彻底解决，可以去看 内部拦截法
          * */
          drawRect(mInitialX, mInitialY, x, y) // 绘制矩形
        }
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
  
  override fun onLongPressActivate() {
    mIsAllowDraw = true
    // 在长按激活时得立马绘制矩形
    drawRect(mInitialX, mInitialX, mLastMoveX, mLastMoveY)
  }
  
  init {
    post {
      /*
      * 外面使用 post 是因为需要在完全布局后才有父布局
      *
      * 这里是设置长按激活的监听，下面的代码会去寻找 ScrollView，并且设置长按监听，因此增加了耦合度
      *
      * 但注意：虽然这里使用了接口，但并不是就能彻底解耦，因为 ScrollView 通过该接口持有 RectView 的引用，
      * 如果你的 RectView 存在被 remove 的情况，还需要单独去 remove 掉这个长按监听
      * */
      var parent = parent
      while (parent is ViewGroup) {
        if (parent is OuterScrollView) {
          parent.addOnLongPress(this)
          break
        }
        parent = parent.parent
      }
      if (parent !is OuterScrollView) {
        throw RuntimeException("${this::class.java.simpleName} 必须以 ${OuterScrollView::class.java.simpleName} 作为父布局！")
      }
    }
  }
}