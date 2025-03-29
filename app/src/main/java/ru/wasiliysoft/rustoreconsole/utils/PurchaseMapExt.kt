package ru.wasiliysoft.rustoreconsole.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wasiliysoft.rustoreconsole.screen.purchases.AmountSumPerMonth
import ru.wasiliysoft.rustoreconsole.screen.purchases.PurchaseMap
import java.time.LocalDate
import java.time.temporal.ChronoUnit


/**
 * Функция для расчета среднего дохода за день (исключая текущий день)
 * @param dayCount количество дней для расчета, по умочанию 28
 * @return средняя сумма по полю amountCurrent за день
 */
fun PurchaseMap.calculateAverageDailyAmmount(dayCount: Int = 28): Int {
    if (dayCount <= 0 || isEmpty()) return 0

    // Получаем текущую дату в формате yyyy-MM-dd
    val today = LocalDate.now()
    val startDate = today.minusDays(dayCount.toLong())

    val dateRange = startDate..today.minusDays(1) // Исключаем текущий день

    // Собираем данные: сумма и уникальные дни с чеками
    var totalAmount = 0
    val daysWithInvoices = mutableSetOf<LocalDate>()

    for (dayInvoices in this.values) {
        for (purchase in dayInvoices) {
            try {
                val date = purchase.invoiceDate.toLocalDate()
                if (date in dateRange) {
                    totalAmount += purchase.amountCurrent
                    daysWithInvoices.add(date)
                }
            } catch (e: Exception) {

            }
        }
    }

    val upperDate = daysWithInvoices.max()
    val lowerDate = daysWithInvoices.min()
    val daysInnerSet = (ChronoUnit.DAYS.between(lowerDate, upperDate) + 1).toInt()

    return totalAmount / 100 / daysInnerSet
}

suspend fun PurchaseMap.toAmountSumPerMonth(): AmountSumPerMonth {
    return withContext(Dispatchers.Default) {
        return@withContext values.flatten()
            .groupBy { it.invoiceDate.toYearAndMonthString() }
            .map { entry -> Pair(entry.key, entry.value.sumOf { it.amountCurrent / 100 }) }
            .sortedByDescending { it.first }
    }
}