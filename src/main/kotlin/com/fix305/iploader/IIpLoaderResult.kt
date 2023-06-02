package com.fix305.iploader

import java.io.Closeable

/** Результат обработки IP адресов */
interface IIpLoaderResult: Closeable {
    /**
     * Проверяет наличие IP адреса в наборе
     * @param ip - ip адрес для проверки [String]
     * @return [Boolean]
     */
    fun isExist(ip: String): Boolean
    /**
     * Возвращает уникальное количество IP адресов в наборе
     * @return [Long]
     */
    fun uniqueCount(): Long

    /**
     * Возвращает флаг наличия ошибок при обработке файла
     * @return [Boolean] - true, если хотя бы одна строка не была обработана
     */
    fun hasErrors(): Boolean

    /**
     * Возвращает список ошибок
     * @return Список [String] с указанием IP адреса и номера строки с ошибкой
     */
    fun errors(): List<String>
}
