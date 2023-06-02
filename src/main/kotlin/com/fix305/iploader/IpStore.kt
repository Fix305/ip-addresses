package com.fix305.iploader

import java.io.Closeable
import java.util.*

/**
 * Хранилище обработанных адресов.
 * В худшем случае может занимать до ~500мб памяти,
 * после использования результата обработки - закрыть (наследуется от [Closeable])
 */
class IpStore : IIpLoaderResult, IIpStore {
    private var data = Array(256) { BitSet() }
    private val errors: MutableList<String> = mutableListOf()

    override fun set(ip: String) {
        try {
            val segments = getSegments(ip)
            val position = getPosition(segments)

            synchronized(data[segments[0]]) {
                data[segments[0]].set(position)
            }
        } catch (e: IpFormatException) {
            synchronized(errors) {
                errors.add("Wrong format `$ip`")
            }
        }
    }

    override fun isExist(ip: String): Boolean {
        val segments = getSegments(ip)
        val position = getPosition(segments)

        return data[segments[0]].get(position)
    }

    override fun uniqueCount(): Long {
        return data.sumOf { it.cardinality() }.toLong()
    }

    override fun hasErrors(): Boolean = errors.isNotEmpty()

    override fun errors(): List<String> = errors

    override fun close() {
        data = Array(0) { BitSet() }
    }

    private fun getPosition(segments: IntArray): Int {
        return segments[1].shl(16) or segments[2].shl(8) or segments[3]
    }

    /**
     * Оптимизированный вариант `ip.split('.').map { it.toInt() }`
     * с проверкой на формат IP адреса.
     * Если формант неверный, выкидывает исключение [IpFormatException]
     */
    private fun getSegments(ip: String): IntArray {
        val segments = IntArray(4)

        var index = 0
        var currentValue = 0

        for (char in ip) {
            if (char == '.') {
                segments[index++] = currentValue
                currentValue = 0
            } else {
                currentValue = currentValue * 10 + (char - '0')
            }
        }

        if (index != 3) throw IpFormatException

        segments[index] = currentValue

        for (seg in segments) {
            if (seg !in 0..255) throw IpFormatException
        }

        return segments
    }

    private object IpFormatException : Exception()
}
