package ru.wasiliysoft.rustoreconsole.fragment.apps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

class ApplicationListViewModel : ViewModel() {
    private val api = RetrofitClient.api

    private val _list = MutableLiveData<LoadingResult<List<AppInfo>>>()
    val list: LiveData<LoadingResult<List<AppInfo>>> = _list

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            _list.postValue(LoadingResult.Loading("Загружаем..."))
            val list = mutableListOf<AppInfo>()
            try {
                val url = "https://backapi.rustore.ru/applicationData/retrieveUserApps"
                val data = api.getRetrieveUserApps(url).body.list
                list.addAll(data)
            } catch (e: Exception) {
                _list.postValue(LoadingResult.Error(e))
                e.printStackTrace()
                return@launch
            }
            list.sortByDescending { it.appName }
            _list.postValue(LoadingResult.Success(list))
        }
    }
}