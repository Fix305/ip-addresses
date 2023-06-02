package com.fix305.iploader

import java.io.Closeable

/** Интерфейс наблюдателя за прогрессом обработки файла */
interface IWatcher : Closeable {
    /**
     * Добавить объект наблюдения
     * @param stateKeeper [StateKeeper] - обертка для хранения результата обработки
     */
    fun listen(stateKeeper: StateKeeper)

    /**
     * Возвращает количество байт, которые необходимо обработать во всех добавленных stateKeeper'ах
     */
    fun getRemaining(): Long

    /**
     * Возвращает количество байт всего во всех добавленных stateKeeper'ах
     */
    fun getTotal(): Long

    /**
     * Начать наблюдение
     */
    fun start()

    /**
     * Возвращает статус наблюдения [Boolean]
     */
    fun isRunning(): Boolean
}

/**
 * Возвращает процент обработки для всех stateKeep, в %
 * Если Watcher не запущен - исключение [IllegalStateException]
 * */
fun IWatcher.getProgress(): Int {
    if (!isRunning()) error("Watcher isn't running")

    return if (getTotal() > 0 ) {
        100 - (getRemaining() * 100 / getTotal()).toInt()
    } else {
        0
    }
}
