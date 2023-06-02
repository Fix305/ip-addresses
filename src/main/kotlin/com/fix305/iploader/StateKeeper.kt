package com.fix305.iploader

/** Модель хранения результата обработки */
data class StateKeeper(
    /** Количество байт, которые необходимо обработать */
    var remaining: Long
)
