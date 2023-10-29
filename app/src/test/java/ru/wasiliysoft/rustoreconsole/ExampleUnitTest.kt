package ru.wasiliysoft.rustoreconsole

import org.junit.Assert
import org.junit.Test
import ru.wasiliysoft.rustoreconsole.utils.formatFromInvoice
import ru.wasiliysoft.rustoreconsole.utils.formatFromReview

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun reviewDateFormat() {
        val localDate = formatFromReview("2023-09-03 19:24:10.010")
        Assert.assertEquals(2023, localDate.year)
        Assert.assertEquals(24, localDate.minute)
    }

    @Test
    fun invoiceDateFormat() {
        val localDate = formatFromInvoice("2023-10-22T12:35:40+03")
        Assert.assertEquals(2023, localDate.year)
        Assert.assertEquals(35, localDate.minute)
    }


}