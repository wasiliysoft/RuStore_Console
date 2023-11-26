package ru.wasiliysoft.rustoreconsole.screen.apps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

class ApplicationListViewModel : ViewModel() {
    private val _list = MutableLiveData<LoadingResult<List<AppInfo>>>()
    val list: LiveData<LoadingResult<List<AppInfo>>> = _list
    private val repo = AppListRepository

    init {
        Log.d("ApplicationListViewModel", "onInit")
        load()
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _list.postValue(LoadingResult.Loading("Загружаем..."))
            try {
                val list = repo.getAppsForce().toMutableList()
                list.sortByDescending { it.appName }
                _list.postValue(LoadingResult.Success(list))
            } catch (e: Exception) {
                e.printStackTrace()
                _list.postValue(LoadingResult.Error(e))
            }
        }
    }
}