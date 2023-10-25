package ru.wasiliysoft.rustoreconsole

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.liveData
import androidx.lifecycle.viewmodel.CreationExtras
import ru.wasiliysoft.rustoreconsole.data.Purchase
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient

class PurchaseViewModel(token: String) : ViewModel() {
    private val api = RetrofitClient.getPurchasesAPI(token)
    private val _list = MutableLiveData<List<Purchase>>(emptyList())
    val list: LiveData<List<Purchase>> = _list

    init {
//        viewModelScope.launch(Dispatchers.IO) {
//            val list = getPurchases(2063488048)
//            _list.postValue(list)
//        }
    }

    fun getPurchases(appId: Int) = liveData {
        val resp = api.getPurchases(appId)
        Log.d("PurchaseViewModel", resp.code)
        Log.d("PurchaseViewModel", resp.message)
        emit(resp.body.list)
    }
//    private suspend fun getPurchases(appId: Int): List<Purchase> {
//        val resp = api.getPurchases(appId)
//        Log.d("PurchaseViewModel", resp.code)
//        Log.d("PurchaseViewModel", resp.message)
//        return resp.body.list
//    }

    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                return PurchaseViewModel(
                    PrefHelper.get(application.applicationContext).token
                ) as T
            }
        }
    }

}