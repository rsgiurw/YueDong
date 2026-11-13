package com.yuedong.fitness.util

/**
 * 页面异步状态的标准载体（模块契约见设计包 §8 ErrorPresenter）。
 * 全应用统一使用，杜绝"每页自己发明一套状态"。
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Empty(val message: String) : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
