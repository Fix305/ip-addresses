package com.fix305.iploader

/**
 * Интерфейс для хранилища IP адресов
 */
interface IIpStore {
    /**
     * Добавить IP адрес в хранилище
     * @param ip - ip адрес для добавления
     */
    fun set(ip: String)
}
