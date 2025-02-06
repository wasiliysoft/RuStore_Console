package ru.wasiliysoft.rustoreconsole.screen.paymentstats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.wasiliysoft.rustoreconsole.data.Stats
import ru.wasiliysoft.rustoreconsole.ui.view.ErrorTextView
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult.Loading

data class AppStats(
    val appName: String,
    val appId: Long,
    val overallSum: Stats
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentStatScreen(
    modifier: Modifier = Modifier,
    viewModel: PaymentsViewModel = viewModel(),
) {
    Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val uiSate = viewModel.overallSum.observeAsState(Loading("")).value
            val state = rememberPullToRefreshState()
            PullToRefreshBox(
                state = state,
                isRefreshing = uiSate is Loading,
                onRefresh = viewModel::load
            ) {
                when (uiSate) {
                    is Loading -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = uiSate.description)
                    }

                    is LoadingResult.Success -> {
                        if (uiSate.comment.isNotEmpty()) {
                            Text(text = uiSate.comment)
                        }
                        ListView(data = uiSate.data)
                    }

                    is LoadingResult.Error -> ErrorTextView(exception = uiSate.exception)
                }
            }
        }
    }
}

@Composable
private fun ListView(
    data: List<AppStats>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        items(items = data, key = { it.appId }) {
            PaymentStatCard(it)
        }
    }
}

@Composable
private fun PaymentStatCard(
    appStats: AppStats,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = appStats.appName, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                appStats.overallSum.let {
                    Text(text = "Вчера\n${it.dailyStats} р.")
                    Text(text = "7 дней\n${it.weeklyStats} р.")
                    Text(text = "30 дней\n${it.monthlyStats} р.")
                    Text(text = "Всё время\n${it.totalStats} р.")
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewPaymentStatCard() {
    PaymentStatCard(
        AppStats(
            appName = "Test app name", appId = 4, overallSum = Stats(
                dailyStats = 1,
                weeklyStats = 7,
                monthlyStats = 30,
                totalStats = 365
            )
        )
    )
}