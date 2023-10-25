package ru.wasiliysoft.rustoreconsole

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.wasiliysoft.rustoreconsole.data.Purchase
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient

class PurchaseViewModel(token: String) : ViewModel() {
    private val api = RetrofitClient.getPurchasesAPI(token)
    private val mutex = Mutex()

    fun getPurchases(appId: List<Int>) = liveData(Dispatchers.IO) {
        val list = mutableListOf<Purchase>()
        appId.map {
            viewModelScope.launch {
                val purchases = api.getPurchases(it).body.list
                mutex.withLock {
                    list.addAll(purchases)
                }
            }
        }.joinAll()
        list.sortByDescending { it.paymentInfo.paymentId }
        emit(list)
    }
}