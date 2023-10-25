package ru.wasiliysoft.rustoreconsole

import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
//        '%Y-%m-%dT%H:%M:%S%z'

        val localDate = LocalDateTime.parse("2023-10-22T12:35:40+03", ISO_OFFSET_DATE_TIME)
        println(localDate.hour)
        println(localDate.minute)


    }

}