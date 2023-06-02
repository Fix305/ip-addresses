import com.fix305.iploader.*
import java.io.File


fun main() {
    var isOK = true

    print("Enter the path to the file: ")
    val filePath = readlnOrNull() ?: ""

    if (filePath.isBlank()) {
        println("File not specified")
        isOK = false
    }

    val file = File(filePath)

    if (!file.isFile || !file.exists()) {
        println("File `$filePath` not found")
        isOK = false
    }

    if (isOK) {
        println("Processing...")
        println()

        val config = Config.Default

        val watcher: IWatcher = Watcher()
        val ipLoader: IIpLoader = IpLoader(
            config = config,
            watcher = watcher
        )

        watcher.use {
            ipLoader.use { loader ->
                val start = System.currentTimeMillis()

                val result = loader.load(file)

                val time = System.currentTimeMillis() - start

                result.use {
                    println()
                    println("unique count: " + it.uniqueCount())
                }

                println("processed time: $time ms")
            }
        }
    }
}