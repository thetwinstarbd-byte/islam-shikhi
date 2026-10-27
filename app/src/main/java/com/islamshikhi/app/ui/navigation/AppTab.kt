package com.islamshikhi.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.QuestionAnswer
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/** নিচের পাঁচটি প্রধান ট্যাব। `route` হলো Navigation Compose-এর গন্তব্যের নাম। */
enum class AppTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "হোম", Icons.Filled.Home),
    IBADAH("ibadah", "ইবাদত", Icons.Filled.AccessTime),
    QURAN("quran", "কুরআন", Icons.AutoMirrored.Filled.MenuBook),
    ASSISTANT("assistant", "সহকারী", Icons.AutoMirrored.Filled.QuestionAnswer),
    PROFILE("profile", "প্রোফাইল", Icons.Filled.Person)
}
