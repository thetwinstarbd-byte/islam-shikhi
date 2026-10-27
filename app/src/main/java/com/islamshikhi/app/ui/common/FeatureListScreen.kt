package com.islamshikhi.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.util.DateFormatBn

/** একটি ট্যাবের ভেতরের সাবমডিউল। `phase` হলো যে পর্যায়ে এটি তৈরি হবে। */
data class FeatureItem(val title: String, val description: String, val phase: Int)

/**
 * যে ট্যাবের কনটেন্ট এখনও তৈরি হয়নি, সেখানে কী কী আসছে এবং কোন পর্যায়ে — তা সৎভাবে দেখায়।
 * কোনো ফিচারকে তৈরি হয়ে গেছে বলে দেখানো হয় না।
 */
@Composable
fun FeatureListScreen(title: String, intro: String, items: List<FeatureItem>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(intro, style = MaterialTheme.typography.bodyMedium)
        items.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(item.description, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "শীঘ্রই আসছে (পর্যায় ${DateFormatBn.digits(item.phase)})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
