package com.example.voltix.ui

import androidx.compose.runtime.Composable

@Composable
fun BottomNavigationBar(
    selectedItem: Int = 1, // Devices default aktif
    onItemSelected: (Int) -> Unit = {}
) {
    val items = listOf("HOME", "DEVICES", "RECAP", "PROFILE")
    val icons = listOf(Icons.Default.Home, Icons.Default.Devices, Icons.Default.List, Icons.Default.Person)

    BottomNavigation(
        backgroundColor = Color.White,
        elevation = 10.dp
    ) {
        items.forEachIndexed { index, label ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp
                    )
                },
                selected = selectedItem == index,
                onClick = { onItemSelected(index) },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.Gray
            )
        }
    }
}
