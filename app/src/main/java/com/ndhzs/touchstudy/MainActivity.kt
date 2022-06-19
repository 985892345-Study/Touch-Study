package com.ndhzs.touchstudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

/**
 * # 需求：
 * 能在 ScrollView 下长按移动绘制矩形，并且如果长按移动到了屏幕边缘可以使 ScrollView 滚动
 *
 * # 需求分析
 *
 * ## 难点分析
 * ### 哪点最难？
 * 长按的判断
 * - 在长按前如果用户滑动一定的距离，那么代表用户是想上下滑动
 * - 长按激活后才允许它绘制矩形
 *
 * ### 可以使用的方法
 * - 外部拦截法
 * - 内部拦截法
 * - 重写 dispatchTouchEvent 也可以实现
 *
 * # 坑
 * - ScrollView 的坐标系与里面子 View 的坐标系是不同的
 * - ScrollView 在 Down 事件时需要调用 super，不然之后不能滚动
 * - ScrollView 在处于惯性滑动时再次触摸会直接拦截事件
 *
 * # 观看顺序（包名）
 * - outer
 * - dispatch
 * - inner
 *
 *
 * 最后推荐使用 内部拦截法，耦合度最低
 */
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.layout_outer)
//    setContentView(R.layout.layout_dispatch)
//    setContentView(R.layout.layout_inner)
  }
}