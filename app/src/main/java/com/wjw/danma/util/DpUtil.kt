package com.wjw.danma.util

import android.content.Context
import android.util.TypedValue

object DpUtil {

    /**
     * 获取屏幕宽度（以像素为单位）
     * @param context 上下文对象
     * @return 屏幕宽度（px）
     */
    fun getScreenSizeWidth(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.widthPixels
    }

    fun getScreenWidthInDp(context: Context): Float {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.widthPixels / displayMetrics.density
    }

    /**
     * 将 dp 转换为 px
     * @param context 上下文对象
     * @param dp 需要转换的 dp 值
     * @return 转换后的 px 值
     */
    fun dp2px(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }
}