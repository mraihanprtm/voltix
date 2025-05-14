package com.example.voltix.ui.screen

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.voltix.R
import com.example.voltix.data.repository.DashboardData
import com.example.voltix.viewmodel.dashboard.DashboardViewModel
import com.example.voltix.viewmodel.dashboard.TimeRange
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val dashboardData by viewModel.dashboardData.collectAsState()
    val timeRange by viewModel.timeRange.collectAsState()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Icon Watermark
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fa_calendar),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .aspectRatio(0.2f)
                            .align(Alignment.TopEnd)
                            .offset(x = 50.dp, y = (-5).dp)
                            .alpha(0.07f),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Column {
                        // Judul Rentang Waktu
                        Text(
                            text = "Atur Rentang Waktu",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dropdown atau Radio Time Range
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TimeRange.values().forEach { range ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { viewModel.setTimeRange(range) }
                                        .padding(horizontal = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = timeRange == range,
                                        onClick = { viewModel.setTimeRange(range) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFF3F51B5),
                                            unselectedColor = Color.Gray
                                        )
                                    )
                                    Text(
                                        text = when (range) {
                                            TimeRange.DAILY -> "Harian"
                                            TimeRange.MONTHLY -> "Bulanan"
                                            TimeRange.YEARLY -> "Tahunan"
                                        },
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                                        color = if (timeRange == range) Color(0xFF1A237E) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        dashboardData?.let { data ->
            item {
                GeneralInfoCard(data, numberFormat, timeRange)
            }

            item {
                PowerUsageGraphCard(data.hourlyPower, timeRange)
            }
        } ?: item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun GeneralInfoCard(data: DashboardData, numberFormat: NumberFormat, timeRange: TimeRange) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Informasi Umum",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        // Jumlah Perangkat Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(20.dp))
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp)
            ) {
                // Background Icon Watermark
                Icon(
                    painter = painterResource(id = R.drawable.ic_fa_tag), // Replace with devices icon
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .aspectRatio(0.9f)
                        .align(Alignment.TopEnd)
                        .offset(x = 5.dp, y = (-5).dp)
                        .alpha(0.07f),
                    tint = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoItem(
                        label = "Jumlah Perangkat",
                        value = data.totalDevices.toString()
                    )
                }
            }
        }

        // Informasi Umum Section
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Daya Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fa_bolt),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .aspectRatio(1f)
                                .align(Alignment.TopEnd)
                                .offset(x = 20.dp, y = (-20).dp)
                                .alpha(0.07f),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        InfoItem(
                            label = "Total Daya",
                            value = when (timeRange) {
                                TimeRange.DAILY -> String.format("%.2f kWh/hari", data.totalPower)
                                TimeRange.MONTHLY -> String.format("%.2f kWh/bulan", data.totalPower)
                                TimeRange.YEARLY -> String.format("%.2f kWh/tahun", data.totalPower)
                            }
                        )
                    }
                }

                // Total Biaya Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fa_money),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .aspectRatio(1f)
                                .align(Alignment.TopEnd)
                                .offset(x = 20.dp, y = (-20).dp)
                                .alpha(0.07f),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        InfoItem(
                            label = "Total Biaya",
                            value = numberFormat.format(data.totalCost)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PowerUsageGraphCard(hourlyPower: List<Float>, timeRange: TimeRange) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Grafik Pemakaian Daya Per Jam",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (hourlyPower.all { it == 0f }) {
                Text(
                    text = "No data available",
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                PowerUsageLineChart(hourlyPower, timeRange)
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PowerUsageLineChart(hourlyPower: List<Float>, timeRange: TimeRange) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    600
                )
                setTouchEnabled(true)
                setScaleEnabled(false)
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    labelRotationAngle = -45f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${value.toInt()}:00"
                        }
                    }
                }

                axisLeft.apply {
                    axisMinimum = 0f
                    granularity = 10f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return when (timeRange) {
                                TimeRange.DAILY -> "${value.toInt()} W"
                                TimeRange.MONTHLY -> "${value.toInt()} kW"
                                TimeRange.YEARLY -> "${value.toInt()} kW"
                            }
                        }
                    }
                }

                val entries = hourlyPower.mapIndexed { index, value ->
                    Entry(index.toFloat(), value)
                }

                val dataSet = LineDataSet(entries, "Power Usage").apply {
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    color = Color(0xFF2196F3).toArgb()
                    valueTextColor = android.graphics.Color.BLACK
                    lineWidth = 2f
                    setDrawCircles(false)
                    setDrawFilled(true)
                    fillColor = Color(0xFF2196F3).copy(alpha = 0.3f).toArgb()
                }

                data = LineData(dataSet)
                invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}