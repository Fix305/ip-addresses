package com.fix305.iploader

/** Конфигурация приложения */
class Config (
    /**
     * Увеличим буфер для чтения - чем выше значение,
     * тем меньше обращений к диску
     */
    val bufferSize: Int = 1024 * 1024,

    /** Сколько потоков используем для обработки файла. */
    val readThreadPool: Int = availableProcessors
) {
    companion object {
        private val availableProcessors = Runtime.getRuntime().availableProcessors()

        /** Конфиг по-умолчанию */
        val Default: Config = Config(
            bufferSize = System.getenv()["BUFFER_SIZE"]?.toIntOrNull()
                ?.takeIf { it in 1 .. 1024 * 1024 * 50 }
                ?: (1024 * 1024),
            readThreadPool = System.getenv()["READ_THREAD_POOL"]?.toIntOrNull()
                ?.takeIf { it in 1..availableProcessors }
                ?: availableProcessors,
        )
    }
}
