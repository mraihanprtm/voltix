package com.example.voltix.ui.component

import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimePickerDialogButton(
    label: String,
    time: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    val context = LocalContext.current

    Button(onClick = {
        showTimePicker(context, time, onTimeSelected)
    }) {
        Text("$label: ${time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}")
    }
}

fun showTimePicker(
    context: Context,
    currentTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    val dialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            onTimeSelected(LocalTime.of(hour, minute))
        },
        currentTime.hour,
        currentTime.minute,
        true
    )
    dialog.show()
}

