package ru.wasiliysoft.rustoreconsole

import org.junit.Assert
import org.junit.Test
import ru.wasiliysoft.rustoreconsole.data.Purchase
import ru.wasiliysoft.rustoreconsole.data.UserReview
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DataClassTests {
    @Test
    fun reviewDateFormat() {
        val localDate = UserReview.demo().commentDate
        Assert.assertEquals(2023, localDate.year)
        Assert.assertEquals(9, localDate.minute)
    }

    @Test
    fun invoiceDateFormat() {
        val localDate = Purchase.demo().invoiceDate
        Assert.assertEquals(2023, localDate.year)
        Assert.assertEquals(35, localDate.minute)
    }

    @Test
    fun generateFieldDateFrom() {
        val dateFrom = LocalDate.now()
            .minusMonths(3)
            .withDayOfMonth(1)
            .format(DateTimeFormatter.ISO_DATE)

        val dateTo = LocalDate.now()
            .plusDays(1)
            .format(DateTimeFormatter.ISO_DATE)

        println(dateFrom)
        println(dateTo)
    }
}