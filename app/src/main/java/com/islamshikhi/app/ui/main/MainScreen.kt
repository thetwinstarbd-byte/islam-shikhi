package com.islamshikhi.app.ui.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.luminance
import com.islamshikhi.app.ui.theme.SystemBarIcons
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.islamshikhi.app.domain.model.UserProfile
import com.islamshikhi.app.ui.assistant.AssistantScreen
import com.islamshikhi.app.ui.about.AboutScreen
import com.islamshikhi.app.ui.assistant.MasailScreen
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.habits.HabitsScreen
import com.islamshikhi.app.ui.info.InfoDetailScreen
import com.islamshikhi.app.ui.info.InfoListScreen
import com.islamshikhi.app.ui.quiz.QuizScreen
import com.islamshikhi.app.ui.zakat.ZakatScreen
import com.islamshikhi.app.data.user.UserKeys
import com.islamshikhi.app.ui.assistant.MasalaDetailScreen
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
    val content = rememberContainer().content
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isHome = currentRoute == null || currentRoute == AppTab.HOME.route
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    // হোমে ওপরে গাঢ় আকাশ, তাই সাদা আইকন; অন্য পাতায় থিম অনুযায়ী
    SystemBarIcons(darkIcons = !isHome && !isDark)

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
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute?.startsWith(tab.route) == true,
                        onClick = { openTab(tab) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppTab.HOME.route,
            modifier = Modifier
                .padding(innerPadding)
                .then(if (isHome) Modifier else Modifier.statusBarsPadding())
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
            composable(Routes.MASAIL) {
                MasailScreen(profile = profile, onBack = back, onOpen = { go(Routes.masala(it)) })
            }
            composable(
                route = Routes.MASALA_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                MasalaDetailScreen(
                    id = entry.arguments?.getString("id").orEmpty(),
                    onBack = back,
                    onOpen = { go(Routes.masala(it)) }
                )
            }
            composable(Routes.BOOKMARKS) { BookmarksScreen(onBack = back, onNavigate = go) }
            composable(Routes.ZAKAT) { ZakatScreen(onBack = back, onNavigate = go) }
            composable(Routes.QUIZ) { QuizScreen(onBack = back) }
            composable(Routes.HABITS) { HabitsScreen(onBack = back, onNavigate = go) }
            composable(Routes.ABOUT) { AboutScreen(onBack = back, onNavigate = go) }

            composable(Routes.KIDS) {
                InfoListScreen(
                    title = "শিশুদের শিক্ষা",
                    intro = "বয়সোপযোগী ইসলামিক শিক্ষা, বাবা-মা শিশুর সঙ্গে বসে পড়তে পারেন। প্রতিটি পাঠে সূত্র দেওয়া আছে।",
                    warning = null,
                    loader = { content.kids() },
                    onBack = back,
                    onOpen = { go(Routes.kid(it)) }
                )
            }
            composable(Routes.KID_DETAIL, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                InfoDetailScreen(
                    id = entry.arguments?.getString("id").orEmpty(),
                    headerTitle = "শিশুদের শিক্ষা",
                    loader = { content.kids() },
                    bookmarkKey = UserKeys::kidKey,
                    onBack = back
                )
            }

            composable(Routes.EMERGENCY) {
                InfoListScreen(
                    title = "জরুরি গাইড",
                    intro = "অসুস্থতা, সফর, মৃত্যু ও বিপদের সময় সাধারণ ইসলামিক নির্দেশনা।",
                    warning = "জীবনের ঝুঁকি বা গুরুতর অসুস্থতা হলে আগে জরুরি সেবায় ফোন করুন (বাংলাদেশে ৯৯৯) বা হাসপাতালে যান। এই অ্যাপ চিকিৎসা বা জরুরি সেবার বিকল্প নয়।",
                    loader = { content.emergency() },
                    onBack = back,
                    onOpen = { go(Routes.emergency(it)) }
                )
            }
            composable(Routes.EMERGENCY_DETAIL, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                InfoDetailScreen(
                    id = entry.arguments?.getString("id").orEmpty(),
                    headerTitle = "জরুরি গাইড",
                    loader = { content.emergency() },
                    bookmarkKey = null,
                    onBack = back
                )
            }

            composable(Routes.LIBRARY) {
                InfoListScreen(
                    title = "ইসলামিক লাইব্রেরি",
                    intro = "সংক্ষিপ্ত নিবন্ধ ও শেখার গাইড। প্রতিটি অংশে সূত্র দেওয়া আছে।",
                    warning = null,
                    loader = { content.library() },
                    onBack = back,
                    onOpen = { go(Routes.library(it)) }
                )
            }
            composable(Routes.LIBRARY_DETAIL, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                InfoDetailScreen(
                    id = entry.arguments?.getString("id").orEmpty(),
                    headerTitle = "লাইব্রেরি",
                    loader = { content.library() },
                    bookmarkKey = UserKeys::libraryKey,
                    onBack = back
                )
            }

            composable(AppTab.PROFILE.route) { ProfileScreen(profile = profile, onResetData = onResetData, onNavigate = go) }
        }
    }
}
