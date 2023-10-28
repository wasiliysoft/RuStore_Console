package ru.wasiliysoft.rustoreconsole

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.wasiliysoft.rustoreconsole.data.Purchase
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

class PurchaseViewModel(
    token: String,
    val appId: List<Long>
) : ViewModel() {
    private val api = RetrofitClient.getPurchasesAPI(token)
    private val mutex = Mutex()

    private val _purchases = MutableLiveData<LoadingResult<List<Purchase>>>()
    val purchases: LiveData<LoadingResult<List<Purchase>>> = _purchases

    fun loadPurchases() {
        viewModelScope.launch(Dispatchers.IO) {
            _purchases.postValue(LoadingResult.Loading("Загружаем"))
            val list = mutableListOf<Purchase>()
            appId.map {
                viewModelScope.launch {
                    try {
                        val purchases = api.getPurchases(it).body.list
                        mutex.withLock {
                            list.addAll(purchases)
                        }
                    } catch (e: Exception) {
                        _purchases.postValue(LoadingResult.Error(e))
                        e.printStackTrace()
                    }
                }
            }.joinAll()
            list.sortByDescending { it.paymentInfo.paymentId }
            _purchases.postValue(LoadingResult.Success(list))
        }
    }
}