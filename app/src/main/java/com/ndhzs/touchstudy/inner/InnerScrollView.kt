package com.ndhzs.touchstudy.inner

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

/**
 * 内部拦截法的 ScrollView 不需要修改什么，
 * 因为在官方的 onInterceptTouch() 中做了一些特殊处理，会造成以下效果：
 * - 在 Down 事件，ScrollView 会把事件交给子 View
 * - 在 Move 事件，要分两情况判断
 *    - 移动距离小于 mTouchSlop，会把 Move 事件给子 View
 *    - 移动距离一旦大于 mTouchSlop，直接拦截，然后之后的事件都不会再发给子 View
 *
 * 总结一句就是：ScrollView 默认会直接拦截子 View 事件，只是有个阈值，
 *            在这个阈值内子 View 可以得到事件
 *
 * 所以内部拦截法的重点就在于：在这个阈值内子 View 调用 parent.requestDisallowInterceptTouchEvent(false) 来禁止所有父布局拦截事件
 *
 * 剩下的注释可以去看 [InnerRectView]
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/6/18 19:48
 */
class InnerScrollView @JvmOverloads constructor(
  context: Context?,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : ScrollView(context, attrs, defStyleAttr, defStyleRes) {


}