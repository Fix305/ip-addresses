package com.fix305.iploader

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Наблюдатель за прогрессом обработки */
class Watcher : IWatcher {
    private val executors = Executors.newCachedThreadPool()

    private val workers: MutableList<StateKeeper> = mutableListOf()
    private var total: Long = 0
    private var isRunning = false

    override fun listen(stateKeeper: StateKeeper) {
        synchronized(workers) {
            workers.add(stateKeeper)
            total += stateKeeper.remaining
        }
    }

    override fun getRemaining(): Long {
        return workers.sumOf { it.remaining }
    }

    override fun getTotal(): Long = total

    override fun start() {
        isRunning = true

        executors.execute {
            var lastProgress = 0
            while (isRunning()) {
                val progress = getProgress()
                if (lastProgress != progress ) {
                    print("${progress}%..")
                    lastProgress = progress
                }
                Thread.sleep(1_000)
            }
        }
    }

    override fun isRunning(): Boolean = isRunning

    override fun close() {
        isRunning = false

        executors.shutdown()
        executors.awaitTermination(10, TimeUnit.SECONDS)
    }
}
