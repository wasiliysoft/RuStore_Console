package ru.wasiliysoft.rustoreconsole.screen.main

import androidx.lifecycle.ViewModel
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository

class MainScreenViewModel : ViewModel() {
    private val repo = AppListRepository

    val selectedApp = repo.selectedApp
}