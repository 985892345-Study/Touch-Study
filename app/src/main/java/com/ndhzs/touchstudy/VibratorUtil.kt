package com.ndhzs.touchstudy

import android.content.Context
import android.os.*
import android.os.Vibrator

/**
 * 用于长按震动
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/2 10:24
 */
internal object VibratorUtil {
  
  /**
   * 根据不同的 Android 版本调用不同的震动方法
   */
  fun start(context: Context, milliseconds: Long) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibrator = context.getSystemService(VibratorManager::class.java)
      vibrator.vibrate(
        CombinedVibration.createParallel(
          VibrationEffect.createOneShot(
            milliseconds,
            VibrationEffect.DEFAULT_AMPLITUDE
          )
        )
      )
    } else {
      val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        vibrator.vibrate(
          VibrationEffect.createOneShot(
            milliseconds,
            VibrationEffect.DEFAULT_AMPLITUDE
          )
        )
      } else {
        vibrator.vibrate(milliseconds)
      }
    }
  }
}