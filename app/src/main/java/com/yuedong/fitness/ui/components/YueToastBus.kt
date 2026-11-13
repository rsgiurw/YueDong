package com.yuedong.fitness.ui.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 全局轻提示总线：任意页面/ViewModel 调用 [show] 即可在根部弹出统一的 YueToast，
 * 替代系统 android.widget.Toast（样式与 APP 不符且无法定制）。
 * 用法：YueToaster.show("已保存")；根部 YueDongMainScreen 已挂载 YueToastHost。
 */
object YueToaster {
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun show(text: String) {
        _message.value = text
    }

    fun dismiss() {
        _message.value = null
    }
}
