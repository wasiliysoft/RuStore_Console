package ru.wasiliysoft.rustoreconsole.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ErrorTextView(
    exception: Exception,
    modifier: Modifier = Modifier
) {
    //    LazyColumn нужен для работы PullToRefreshBox
    LazyColumn(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        item {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = exception.message ?: "Неизвестная ошибка: $exception")
            }
        }
    }
}