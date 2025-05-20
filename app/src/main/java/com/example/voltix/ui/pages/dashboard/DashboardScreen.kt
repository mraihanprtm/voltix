package com.example.voltix.ui.screen

import android.annotation.SuppressLint
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.voltix.R
import com.example.voltix.data.repository.DashboardData
import com.example.voltix.ui.component.LoadingAnimationSection
import com.example.voltix.viewmodel.dashboard.DashboardViewModel
import com.example.voltix.viewmodel.dashboard.TimeRange
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    navController: NavHostController
) {
    val dashboardData by viewModel.dashboardData.collectAsState()
    val timeRange by viewModel.timeRange.collectAsState()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    // Debug data loading
    LaunchedEffect(dashboardData) {
        dashboardData?.let { data ->
            println("Dashboard: totalDevices = ${data.totalDevices}")
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { _ ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            dashboardData?.let { data ->
                if (data.totalDevices == 0) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            WelcomeSection(navController)
                        }
                    }
                } else {
                    item {
                        TimeRangeCard(viewModel, timeRange)
                    }

                    item {
                        GeneralInfoCard(data, numberFormat, timeRange)
                    }

                    item {
                        PowerUsageGraphCard(data.hourlyPower, timeRange)
                    }
                }
            } ?: item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingAnimationSection(true)
                }
            }
        }
    }
}

@Composable
fun WelcomeSection(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.welcome_animation))
        LottieAnimation(
            composition = composition,
            modifier = Modifier.size(200.dp),
            iterations = LottieConstants.IterateForever
        )
        Text(
            text = "Selamat datang di aplikasi VOLTIX",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Sebelum eksplor lebih jauh, kita tambah ruangan dulu yuk! Terus jangan lupa tambah perangkat yaaa!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = { navController.navigate("daftar_ruangan") },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Tambah Ruangan",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun TimeRangeCard(viewModel: DashboardViewModel, timeRange: TimeRange) {
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
            Icon(
                painter = painterResource(id = R.drawable.ic_fa_calendar),
                contentDescription = "Ikon kalender",
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .aspectRatio(0.2f)
                    .align(Alignment.TopEnd)
                    .offset(x = 50.dp, y = (-5).dp)
                    .alpha(0.07f),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = "Atur Rentang Waktu",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = when (range) {
                                    TimeRange.DAILY -> "Harian"
                                    TimeRange.MONTHLY -> "Bulanan"
                                    TimeRange.YEARLY -> "Tahunan"
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                                color = if (timeRange == range) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(20.dp))
                .shadow(8.dp, RoundedCornerShape(20.dp)),
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
                    painter = painterResource(id = R.drawable.ic_fa_tag),
                    contentDescription = "Ikon perangkat",
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
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
                        contentDescription = "Ikon daya",
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
                            TimeRange.DAILY -> String.format("%.2f kWh", data.totalPower)
                            TimeRange.MONTHLY -> String.format("%.2f kWh", data.totalPower)
                            TimeRange.YEARLY -> String.format("%.2f kWh", data.totalPower)
                        }
                    )
                }
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
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
                        contentDescription = "Ikon biaya",
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

@Composable
fun PowerUsageGraphCard(hourlyPower: List<Float>, timeRange: TimeRange) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Grafik Pemakaian Daya Per Jam",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (hourlyPower.isEmpty() || hourlyPower.all { it == 0f }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada data untuk ditampilkan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
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
    // Capture colors in composable context
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryFadedColor = primaryColor.copy(alpha = 0.2f)
    val onSurfaceFadedColor = onSurfaceColor.copy(alpha = 0.2f)

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                // Set layout params to match Compose modifier
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                // Basic chart configuration
                setTouchEnabled(true)
                setPinchZoom(false)
                setScaleEnabled(false)
                description.isEnabled = false
                setExtraOffsets(8f, 8f, 8f, 16f) // Add padding for labels

                // Legend
                legend.apply {
                    isEnabled = true
                    textColor = onSurfaceColor.toArgb()
                    textSize = 12f
                    form = Legend.LegendForm.LINE
                }

                // X-axis (time)
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    labelRotationAngle = -45f
                    textColor = onSurfaceColor.toArgb()
                    textSize = 10f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${value.toInt()}:00"
                        }
                    }
                }

                // Y-axis (power usage)
                axisLeft.apply {
                    axisMinimum = 0f
                    granularity = 10f
                    textColor = onSurfaceColor.toArgb()
                    textSize = 10f
                    setDrawGridLines(true)
                    gridColor = onSurfaceFadedColor.toArgb()
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
                axisRight.isEnabled = false

                // Data points
                val entries = hourlyPower.mapIndexed { index, value ->
                    Entry(index.toFloat(), value)
                }

                // Line dataset
                val dataSet = LineDataSet(entries, "Pemakaian Daya").apply {
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    color = primaryColor.toArgb()
                    lineWidth = 2.5f
                    setDrawCircles(true)
                    circleRadius = 3f
                    setCircleColor(primaryColor.toArgb())
                    setDrawFilled(true)
                    fillColor = primaryFadedColor.toArgb()
                    setDrawValues(false) // Disable value labels above points
                    valueTextColor = onSurfaceColor.toArgb()
                    valueTextSize = 10f
                }

                // Set data and refresh
                data = LineData(dataSet)
                invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
            .semantics { contentDescription = "Grafik pemakaian daya per jam" }
    )
}