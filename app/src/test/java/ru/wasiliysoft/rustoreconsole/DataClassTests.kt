package ru.wasiliysoft.rustoreconsole

import org.junit.Assert
import org.junit.Test
import ru.wasiliysoft.rustoreconsole.data.Purchase
import ru.wasiliysoft.rustoreconsole.data.UserReview

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
}