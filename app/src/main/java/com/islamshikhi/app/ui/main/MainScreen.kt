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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.islamshikhi.app.domain.model.UserProfile
import com.islamshikhi.app.ui.assistant.AssistantScreen
import com.islamshikhi.app.ui.home.HomeScreen
import com.islamshikhi.app.ui.ibadah.IbadahScreen
import com.islamshikhi.app.ui.ibadah.CalendarScreen
import com.islamshikhi.app.ui.ibadah.DuaScreen
import com.islamshikhi.app.ui.ibadah.PrayerScreen
import com.islamshikhi.app.ui.ibadah.QiblaScreen
import com.islamshikhi.app.ui.ibadah.TasbihScreen
import com.islamshikhi.app.ui.ibadah.TrackerScreen
import com.islamshikhi.app.ui.navigation.AppTab
import com.islamshikhi.app.ui.navigation.Routes
import com.islamshikhi.app.ui.profile.ProfileScreen
import com.islamshikhi.app.ui.bookmarks.BookmarksScreen
import com.islamshikhi.app.ui.quran.MemorizeScreen
import com.islamshikhi.app.ui.quran.QuranNoticeScreen
import com.islamshikhi.app.ui.quran.QuranReaderScreen
import com.islamshikhi.app.ui.quran.QuranScreen
import com.islamshikhi.app.ui.quran.QuranSearchScreen

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

    val go: (String) -> Unit = { route -> navController.navigate(route) { launchSingleTop = true } }
    val back: () -> Unit = { navController.popBackStack() }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute?.startsWith(tab.route) == true,
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
            composable(AppTab.HOME.route) {
                HomeScreen(profile = profile, onOpenTab = openTab, onNavigate = go)
            }
            composable(AppTab.IBADAH.route) { IbadahScreen(onNavigate = go) }
            composable(AppTab.QURAN.route) { QuranScreen(onNavigate = go) }
            composable(AppTab.ASSISTANT.route) { AssistantScreen(onNavigate = go) }
            composable(Routes.DUA) { DuaScreen(onBack = back) }
            composable(Routes.PRAYER) { PrayerScreen(onBack = back) }
            composable(Routes.QIBLA) { QiblaScreen(onBack = back) }
            composable(Routes.CALENDAR) { CalendarScreen(onBack = back) }
            composable(Routes.TASBIH) { TasbihScreen(onBack = back) }
            composable(Routes.TRACKER) { TrackerScreen(onBack = back) }
            composable(Routes.QURAN_SEARCH) {
                QuranSearchScreen(onBack = back, onOpen = { chapter, ayah -> go(Routes.quranRead(chapter, ayah)) })
            }
            composable(Routes.QURAN_NOTICE) { QuranNoticeScreen(onBack = back) }
            composable(Routes.QURAN_MEMORIZE) {
                MemorizeScreen(onBack = back, onOpenChapter = { go(Routes.quranRead(it)) })
            }
            composable(
                route = Routes.QURAN_READ,
                arguments = listOf(
                    navArgument("chapter") { type = NavType.IntType },
                    navArgument("ayah") { type = NavType.IntType; defaultValue = 1 }
                )
            ) { entry ->
                QuranReaderScreen(
                    chapterId = entry.arguments?.getInt("chapter") ?: 1,
                    startAyah = entry.arguments?.getInt("ayah") ?: 1,
                    onBack = back
                )
            }
            composable(Routes.BOOKMARKS) { BookmarksScreen(onBack = back, onNavigate = go) }
            composable(AppTab.PROFILE.route) { ProfileScreen(profile = profile, onResetData = onResetData) }
        }
    }
}
