import com.fix305.iploader.Config
import com.fix305.iploader.IIpLoader
import com.fix305.iploader.IpLoader
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.inspectors.forAll
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.random.Random

class IpTest : FunSpec() {
    private val loader: IIpLoader = IpLoader(
        config = Config.Default,
        watcher = null
    )

    override suspend fun afterSpec(spec: Spec) {
        loader.close()
    }

    init {
        context("Обработка файла с IP") {
            test("Файл с валидными адресами") {
                val total = 150L
                val unique = 30L

                val file = tempfile().also {
                    it.fillFile(total, genUniqAddresses(unique))
                }

                /** Action */
                val result = loader.load(file)

                with(result) {
                    uniqueCount() shouldBe unique
                    hasErrors().shouldBeFalse()
                    errors().shouldBeEmpty()
                }
            }

            test("Поддерживаем разные символы окончания строки") {
                listOf(
                    "\n",
                    "\r\n",
                    "\n\r", // ну а вдруг
                    "\r",
                ).forAll { endingSymbol ->
                    val total = 10L
                    val unique = 5L

                    val file = tempfile().also {
                        it.fillFile(total, genUniqAddresses(unique), endingSymbol)
                    }

                    /** Action */
                    val result = loader.load(file)

                    with(result) {
                        uniqueCount() shouldBe unique
                        hasErrors().shouldBeFalse()
                        errors().shouldBeEmpty()
                    }
                }
            }

            test("Файл с адресами неверного формата") {
                val wrongIp = listOf(
                    "",
                    " ",
                    "1.2.3",
                    "1.2.ip.4",
                    "1.2.3.4.5",
                    "256.1.1.1",
                    "just-text",
                    "ip.ip.ip.ip",
                )

                val file = tempfile().also { file ->
                    file.bufferedWriter().use { writer ->
                        wrongIp.forEach { ip ->
                            writer.write(ip + "\n")
                        }
                    }
                }

                /** Action */
                val result = loader.load(file)

                with(result) {
                    hasErrors().shouldBeTrue()
                    errors().shouldContainExactlyInAnyOrder(
                        wrongIp.map { ip -> "Wrong format `$ip`" }
                    )
                }
            }

            context("Поиск IP") {
                test("Поиск существующих IP") {
                    val total = 100L
                    val unique = 50L

                    val uniqueList = genUniqAddresses(unique)

                    val file = tempfile().also {
                        it.fillFile(total, uniqueList)
                    }

                    val loadResult = loader.load(file)

                    uniqueList.forAll {
                        /** Action */
                        val result = loadResult.isExist(it)

                        result.shouldBeTrue()
                    }
                }

                test("Поиск несуществующих IP") {
                    val total = 100L
                    val unique = 50L

                    var uniqueList = genUniqAddresses(unique)
                    // Заберем последний элемент из списка уникальных ip адресов,
                    // и не будем добавлять его в файл
                    val nonExistentIp = uniqueList.last()
                    uniqueList = uniqueList.dropLast(1)

                    val file = tempfile().also {
                        it.fillFile(total, uniqueList)
                    }

                    val loadResult = loader.load(file)

                    /** Action */
                    val result = loadResult.isExist(nonExistentIp)

                    uniqueList.shouldNotContain(nonExistentIp)
                    result.shouldBeFalse()
                }
            }
        }
    }

    private fun genUniqAddresses(count: Long): List<String> {
        val list = mutableListOf<String>()

        while (list.size < count) {
            val ip = "${Random.nextInt(0, 255)}" +
                    "." +
                    "${Random.nextInt(0, 255)}" +
                    "." +
                    "${Random.nextInt(0, 255)}" +
                    "." +
                    "${Random.nextInt(0, 255)}"

            if (ip !in list) {
                list.add(ip)
            }
        }

        return list
    }

    private fun File.fillFile(total: Long, uniqueIps: List<String>, endingSymbol: String = "\n") {
        var wroteCount = total

        bufferedWriter().use { writer ->
            uniqueIps.forEach {
                writer.write(it + endingSymbol)
                wroteCount--
            }

            while (wroteCount > 0) {
                writer.write(uniqueIps[Random.nextInt(0, uniqueIps.size)] + endingSymbol)
                wroteCount--
            }
        }
    }
}
