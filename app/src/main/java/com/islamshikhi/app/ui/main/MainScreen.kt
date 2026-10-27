package com.islamshikhi.app.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.islamshikhi.app.domain.model.UserProfile
import com.islamshikhi.app.ui.assistant.AssistantScreen
import com.islamshikhi.app.ui.home.HomeScreen
import com.islamshikhi.app.ui.ibadah.IbadahScreen
import com.islamshikhi.app.ui.navigation.AppTab
import com.islamshikhi.app.ui.profile.ProfileScreen
import com.islamshikhi.app.ui.quran.QuranScreen

/** সেটআপ শেষ হওয়ার পর মূল স্ক্রিন: নিচে পাঁচ ট্যাব, ভেতরে Navigation Compose। */
@Composable
fun MainScreen(profile: UserProfile, onResetData: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val openTab: (AppTab) -> Unit = { tab ->
        navController.navigate(tab.route) {
            // ট্যাব বদলালে ব্যাক-স্ট্যাক জমা না করে প্রতিটি ট্যাবের অবস্থা মনে রাখে।
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = { openTab(tab) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppTab.HOME.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppTab.HOME.route) { HomeScreen(profile = profile, onOpenTab = openTab) }
            composable(AppTab.IBADAH.route) { IbadahScreen() }
            composable(AppTab.QURAN.route) { QuranScreen() }
            composable(AppTab.ASSISTANT.route) { AssistantScreen() }
            composable(AppTab.PROFILE.route) { ProfileScreen(profile = profile, onResetData = onResetData) }
        }
    }
}
