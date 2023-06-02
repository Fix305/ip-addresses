package com.fix305.iploader

import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.Callable

class Worker(
    private val file: File,
    private val range: FileRange,
    private val store: IIpStore,
    private val bufferSize: Int,
    private val watcher: IWatcher? = null,
) : Callable<Unit> {
    private val buffer = ByteArray(bufferSize)

    private val state: StateKeeper = StateKeeper(remaining = range.toInclusive - range.from + 1).also {
        watcher?.listen(it)
    }

    // Если в конце буфера окажется часть IP адреса, запомним ее сюда,
    // чтоб на следующем чтении в буфер учесть и ее
    private var leftover = ""

    override fun call() {
        RandomAccessFile(file, "r").use { randomAccessFile ->
            randomAccessFile.seek(range.from)

            while (state.remaining > 0) {
                val bytesRead = randomAccessFile.read(buffer, 0, minOf(bufferSize.toLong(), state.remaining).toInt())
                if (bytesRead > 0) {
                    state.remaining -= bytesRead

                    // Начало новой строки с IP
                    var lineStart = 0
                    // Признак, что последний прочитанный символ - конец строки / файла
                    var lastByteIsEndingSymbol = false

                    for (i in 0 until bytesRead) {
                        if (buffer[i] == EolN || buffer[i] == EolR || buffer[i] == Eof) {
                            if (!lastByteIsEndingSymbol) {
                                val ip = leftover + String(buffer, lineStart, i - lineStart)

                                leftover = ""
                                store.set(ip)
                            }

                            lastByteIsEndingSymbol = true
                        } else {
                            if (lastByteIsEndingSymbol) {
                                lineStart = i
                            }

                            lastByteIsEndingSymbol = false
                        }
                    }

                    // Если конец буфера заканчивается на окончание строки, то берем в обработку последний IP
                    // Иначе кладем что есть в остатки для следующего буфера
                    if (buffer[bytesRead - 1] == EolN || buffer[bytesRead - 1] == EolR || buffer[bytesRead - 1] == Eof) {
                        leftover = ""
                    } else {
                        leftover += String(buffer, lineStart, bytesRead - lineStart)
                    }
                } else {
                    break
                }
            }
        }
    }

    companion object {
        private const val EolN = '\n'.code.toByte()
        private const val EolR = '\r'.code.toByte()
        private const val Eof = (-1).toByte()
    }
}
