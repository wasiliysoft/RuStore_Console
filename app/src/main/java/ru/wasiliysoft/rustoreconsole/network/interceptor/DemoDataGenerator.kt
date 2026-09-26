package ru.wasiliysoft.rustoreconsole.network.interceptor

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

/**
 * Генерирует JSON-ответы, идентичные по структуре ответам RuStore API,
 * но с датами, привязанными к текущему моменту.
 *
 * Детерминированность: используем seed на основе appId + дня,
 * чтобы при повторном открытии в тот же день данные не "прыгали".
 */
object DemoDataGenerator {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+03:00'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Europe/Moscow")
    }

    private data class AppProfile(
        val appId: String,
        val packageName: String,
        val appName: String,
        val shortDescription: String,
        val companyName: String,
        val companyId: Long,
        val basePriceKopecks: Int,          // 14900 = 149 ₽
        val totalPaidCount: Int,            // сколько всего покупок "за всю историю"
        val avgPerDay: Double,              // среднее число покупок в день
        val dailyWeight: Double             // вес для "сегодня"
    )

    private val profiles = List(6) {
        val index = it + 1
        AppProfile(
            appId = "20$index",
            packageName = "ru.wasiliysoft.app$index",
            appName = "My $index demo app name",
            shortDescription = "My $index demo app name - app small description",
            companyName = "First name developer",
            companyId = 101,
            basePriceKopecks = 14900 + index * 100,
            totalPaidCount = 2813 + index * 10,
            avgPerDay = 2.3,
            dailyWeight = 1.0
        )
    }

    // ---------- Публичное API ----------

    fun retrieveUserApps(): String {
        val content = JSONArray()
        profiles.forEach { p ->
            content.put(JSONObject().apply {
                put("appId", p.appId.toLong())
                put("packageName", p.packageName)
                put("appName", p.appName)
                put("iconUrl", "https://static.rustore.ru/apk/${p.appId}/content/ICON/demo.png")
                put("appStatus", "PUBLISHED")
                put("versionName", "1.0.3 build 11")
                put("versionCode", 11)
                put("companyName", p.companyName)
                put("companyId", p.companyId)
                put("shortDescription", p.shortDescription)
                put("appVerUpdatedAt", isoFormat.format(daysAgo(30)))
                put("activePrice", p.basePriceKopecks)
                put("paid", true)
                put("deviceType", "MOBILE")
                put("role", "OWNER")
                put("versionType", "REGULAR")
                put("platform", "ANDROID")
            })
        }
        return JSONObject().apply {
            put("code", "OK")
            put("message", "OK")
            put("body", JSONObject().apply {
                put("content", content)
                put("pageSize", profiles.size)
            })
            put("timestamp", isoFormat.format(Date()))
        }.toString()
    }

    fun invoices(appId: String): String {
        val profile = profiles.firstOrNull { it.appId == appId } ?: return emptyInvoices()
        val rnd = Random(appId.hashCode() + dayIndex())
        val invoices = JSONArray()

        // 2 месяца = 60 дней
        for (dayOffset in 0 until 60) {
            val date = daysAgo(dayOffset)
            // Детерминированное число покупок в день
            val count = poissonSample(profile.avgPerDay, rnd)
            repeat(count) { i ->
                invoices.put(buildInvoice(profile, date, rnd, dayOffset, i))
            }
        }

        return JSONObject().apply {
            put("code", "OK")
            put("message", JSONObject.NULL)
            put("body", JSONObject().apply {
                put("invoices", invoices)
                put("pageNumber", 0)
                put("pageSize", 250)
                put("totalElements", invoices.length())
                put("totalPages", 1)
            })
            put("timestamp", isoFormat.format(Date()))
        }.toString()
    }

    fun stats(appId: String): String {
        val profile = profiles.firstOrNull { it.appId == appId }
            ?: return emptyStats()

        val rnd = Random(appId.hashCode() + dayIndex() + 7)
        var totalSum = 0L
        var day0 = 0;
        var day1 = 0
        var week = 0;
        var month = 0

        for (dayOffset in 0 until 60) {
            val count = poissonSample(profile.avgPerDay, rnd)
            totalSum += count * profile.basePriceKopecks
            if (dayOffset == 0) day0 += count
            if (dayOffset == 1) day1 += count
            if (dayOffset < 7) week += count
            month += count
        }

        val dailyCount = day0 + day1
        val dailySum = dailyCount * profile.basePriceKopecks
        val weeklySum = week * profile.basePriceKopecks
        val monthlySum = month * profile.basePriceKopecks

        val income = JSONObject().apply {
            put("transactions", JSONObject().apply {
                put("paidAppsCount", counters(dailyCount, week, month, profile.totalPaidCount))
                put("overallCount", counters(dailyCount, week, month, profile.totalPaidCount))
            })
            put("percent", JSONObject().apply {
                put("paidAppsPercent", percents(if (dailyCount > 0) 100 else 0))
            })
            put("sum", JSONObject().apply {
                put(
                    "paidAppsSum", sums(
                        dailySum / 100.0, weeklySum / 100.0,
                        monthlySum / 100.0, totalSum / 100.0
                    )
                )
                put(
                    "overallSum", sums(
                        dailySum / 100.0, weeklySum / 100.0,
                        monthlySum / 100.0, totalSum / 100.0
                    )
                )
            })
        }

        return JSONObject().apply {
            put("code", "OK")
            put("message", JSONObject.NULL)
            put("body", JSONObject().apply { put("income", income) })
            put("timestamp", isoFormat.format(Date()))
        }.toString()
    }

    fun feedbacks(appId: String): String {
        val profile = profiles.firstOrNull { it.appId == appId }
            ?: return """{"elements":[],"continuation":"0","elementsCount":0}"""

        val seed = appId.hashCode() + dayIndex()
        val rnd = Random(seed)

        val names = listOf(
            "Павел", "Илья", "Рустам", "Михаил", "Юрий",
            "Марио", "Алексей", "Денис", "Андрей", "Артур",
            "Влад", "Александр", "Дмитрий", "Сергей", "Роман",
            "Евгений", "Николай", "Антон", "Игорь", "Максим"
        )

        val texts = listOf(
            "Работает отлично, спасибо разработчику!",
            "Очень удобное приложение, окупило себя.",
            "Пульт потерян, а приложение выручило.",
            "Всё работает как надо, рекомендую.",
            "Не думал, что так легко найдётся замена пульту.",
            "Спасибо, колонки снова управляются с телефона.",
            "Хорошее приложение, но кнопки мелковаты.",
            "Идеально для моей акустики, всё летает.",
            "Быстро, просто, без рекламы — то что нужно.",
            "Работает на моём телефоне без нареканий."
        )

        val models = listOf(
            "2312DRA50G", "M2102K1G", "M2007J20CG",
            "SM-S918B", "RMX3851", "M2101K6G", "V2329A", "CPH2581"
        )
        val manufacturers = listOf("Xiaomi", "samsung", "realme", "vivo", "OnePlus", "HONOR")

        val elements = JSONArray()
        val feedbackCount = 15 + rnd.nextInt(10)

        for (i in 0 until feedbackCount) {
            val rating = if (rnd.nextInt(10) < 8) 5 else (3 + rnd.nextInt(3))
            val date = daysAgo(rnd.nextInt(90))
            elements.put(JSONObject().apply {
                put("commentId", 2_400_000_000L + rnd.nextInt(10_000_000))
                put("appRating", rating)
                put("firstName", names[rnd.nextInt(names.size)])
                put("userAvatarUrl", "")
                put("commentDate", isoFormat.format(date))
                put("editedAt", JSONObject.NULL)
                put("commentText", texts[rnd.nextInt(texts.size)])
                put("userVerCode", 130 + rnd.nextInt(50))
                put("likeCounter", rnd.nextInt(5))
                put("dislikeCounter", rnd.nextInt(3))
                put("feedbackType", "STABLE")
                put("deviceInfo", JSONObject().apply {
                    put("id", "demo-${rnd.nextInt(Int.MAX_VALUE)}")
                    put("firmwareVersion", (10 + rnd.nextInt(6)).toString())
                    put("model", models[rnd.nextInt(models.size)])
                    put("manufacturer", manufacturers[rnd.nextInt(manufacturers.size)])
                })
                put("complaintStatus", JSONObject.NULL)
                put("devResponse", JSONObject.NULL)
                put("totalRepliesCount", 0)
            })
        }

        return JSONObject().apply {
            put("elements", elements)
            put("continuation", "0")
            put("elementsCount", feedbackCount)
        }.toString()
    }

    // ---------- Хелперы ----------

    private fun buildInvoice(
        profile: AppProfile, date: Date, rnd: Random,
        dayOffset: Int, index: Int
    ): JSONObject {
        val ways = listOf(
            Triple("SBP", null, 0.45),
            Triple("CARD", "XX%04d".format(rnd.nextInt(10000)), 0.15),
            Triple("SBERPAY", "XX%04d".format(rnd.nextInt(10000)), 0.15),
            Triple("CARD_BINDING", "XX%04d".format(rnd.nextInt(10000)), 0.15),
            Triple("mobile_dmr", "+7 9XX XXX XX %02d".format(rnd.nextInt(100)), 0.10)
        )
        var r = rnd.nextDouble()
        var chosen = ways.first()
        for (w in ways) {
            if (r < w.third) {
                chosen = w; break
            }
            r -= w.third
        }

        // Смещаем время внутри дня
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow")).apply {
            time = date
            set(Calendar.HOUR_OF_DAY, rnd.nextInt(24))
            set(Calendar.MINUTE, rnd.nextInt(60))
            set(Calendar.SECOND, rnd.nextInt(60))
        }

        return JSONObject().apply {
            put("invoice_id", "9%09d".format(rnd.nextInt(1_000_000_000)))
            put("invoice_date", isoFormat.format(cal.time))
            put("invoice_status", "confirmed")
            put("purchase_id", java.util.UUID.randomUUID().toString())
            put("order_number", JSONObject.NULL)
            put("description", "Покупка приложения ${profile.appName}")
            put("visual_name", "Покупка приложения")
            put("product_name", profile.appName)
            put("visual_amount", "149 ₽")
            put("amount_create", profile.basePriceKopecks)
            put("payment_info", JSONObject().apply {
                put("payment_way_code", chosen.first)
                put("masked_pan", chosen.second ?: JSONObject.NULL)
            })
            put("app_user_id", JSONObject.NULL)
            put("app_user_email", JSONObject.NULL)
            put("auth_type", "VK_AUTH")
            put("developer_payload", JSONObject.NULL)
            put("base_price", JSONObject.NULL)
            put("promotion_code", JSONObject.NULL)
        }
    }

    private fun counters(d: Int, w: Int, m: Int, t: Int) = JSONObject().apply {
        put("dailyStats", d); put("weeklyStats", w)
        put("monthlyStats", m); put("totalStats", t)
    }

    private fun percents(v: Int) = JSONObject().apply {
        put("dailyStats", v); put("weeklyStats", v)
        put("monthlyStats", v); put("totalStats", v)
    }

    private fun sums(d: Double, w: Double, m: Double, t: Double) = JSONObject().apply {
        put("dailyStats", d); put("weeklyStats", w)
        put("monthlyStats", m); put("totalStats", t)
    }

    /** Простой сэмплер Пуассона через метод Кнута — даёт правдоподобное распределение. */
    private fun poissonSample(lambda: Double, rnd: Random): Int {
        val l = Math.exp(-lambda)
        var k = 0
        var p = 1.0
        do {
            k++
            p *= rnd.nextDouble()
        } while (p > l && k < 50)
        return k - 1
    }

    private fun daysAgo(days: Int): Date {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return cal.time
    }

    /** Индекс дня — для сида, чтобы данные не менялись в течение суток. */
    private fun dayIndex(): Long = System.currentTimeMillis() / 86_400_000L

    private fun emptyInvoices() =
        """{"code":"OK","message":null,"body":{"invoices":[],"pageNumber":0,"pageSize":250,"totalElements":0,"totalPages":0},"timestamp":"${
            isoFormat.format(Date())
        }"}"""

    private fun emptyStats() =
        """{"code":"OK","message":null,"body":{"income":null,"returned":null},"timestamp":"${isoFormat.format(Date())}"}"""
}