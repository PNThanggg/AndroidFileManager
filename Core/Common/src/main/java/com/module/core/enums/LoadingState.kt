package com.module.core.enums

sealed class LoadingState {
    data object Init : LoadingState()

    data object Loading : LoadingState()

    data object Success : LoadingState()

    data class Error(val message: String?) : LoadingState()
}