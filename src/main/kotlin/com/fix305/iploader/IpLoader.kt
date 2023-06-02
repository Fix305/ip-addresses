package com.fix305.iploader

import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Обработчик файла с IP адресами */
class IpLoader(
    private val config: Config = Config.Default,
    private val watcher: IWatcher? = null,
) : IIpLoader {
    private val executors = Executors.newFixedThreadPool(config.readThreadPool)

    private fun detektFileSegments(filed: File, parts: Int = 12): List<FileRange> {
        val lastPos = filed.length() - 1
        val segmentSize = filed.length() / parts

        val fileSegments = mutableListOf<Long>()
        fileSegments.add(0)

        var currentPos = segmentSize

        RandomAccessFile(filed, "r").use { file ->
            var stop = false
            while (!stop) {
                file.seek(currentPos)
                var endIp = false

                while (true) {
                    val currentByte = file.read()
                    val filePointer = file.filePointer - 1

                    if (currentByte == -1) {
                        stop = true
                        break
                    }

                    if (currentByte in listOf(10, 13)) {
                        endIp = true
                    } else if (endIp) {
                        if (currentByte !in listOf(10, 13)) {
                            fileSegments.add(filePointer)
                            currentPos = filePointer + segmentSize
                            break
                        }
                    }
                }
            }
        }

        return fileSegments.mapIndexed { index, value ->
            if (index < fileSegments.size - 1) {
                FileRange(value, fileSegments[index + 1] - 1)
            } else {
                FileRange(value, lastPos)
            }
        }
    }

    override fun load(file: File): IIpLoaderResult {
        val store = IpStore()

        val segments = detektFileSegments(file, config.readThreadPool)

        watcher?.start()

        val tasks = segments.map { range ->
            Worker(
                file = file,
                range = range,
                bufferSize = config.bufferSize,
                store = store,
                watcher = watcher
            )
        }

        executors.invokeAll(tasks)

        return store
    }

    override fun close() {
        executors.shutdown()
        executors.awaitTermination(1, TimeUnit.HOURS)
    }
}
