package com.fix305.iploader

/** Интервал обработки файла */
data class FileRange(
    /** Начальная позиция */
    val from: Long,
    /** Конечная позиция, включительно */
    val toInclusive: Long,
)
