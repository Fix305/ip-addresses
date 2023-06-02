package com.fix305.iploader

import java.io.Closeable
import java.io.File

/** Интерфейс обработки IP адресов */
interface IIpLoader : Closeable {
    /**
     * Загружает файл с IP адресами
     * @param file - файл с IP адресами
     * @return [IIpLoaderResult]
     */
    fun load(file: File): IIpLoaderResult
}
