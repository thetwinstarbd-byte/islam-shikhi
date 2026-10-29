package com.islamshikhi.app.ui.zakat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.feature.zakat.NisabBasis
import com.islamshikhi.app.feature.zakat.ZakatCalculator
import com.islamshikhi.app.feature.zakat.ZakatInput
import com.islamshikhi.app.feature.zakat.ZakatResult
import com.islamshikhi.app.ui.common.MenuCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.WarningCard
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.navigation.Routes
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

private val FIELDS = listOf(
    "cash" to "নগদ ও ব্যাংকে জমা টাকা",
    "gold" to "স্বর্ণ (গ্রাম)",
    "silver" to "রূপা (গ্রাম)",
    "goldPrice" to "স্বর্ণের বর্তমান দর (প্রতি গ্রাম)",
    "silverPrice" to "রূপার বর্তমান দর (প্রতি গ্রাম)",
    "business" to "ব্যবসার পণ্যের বর্তমান মূল্য",
    "receivables" to "অন্যের কাছে আদায়যোগ্য পাওনা",
    "liabilities" to "এখনই পরিশোধযোগ্য ঋণ"
)

private fun key(name: String) = "zakat:$name"

/**
 * যাকাতের আনুমানিক হিসাব। ইনপুট ফোনে সংরক্ষিত থাকে, তাই পরে আবার খুলে বদলানো যায়।
 * এটি ফতোয়া নয়; নিসাব, সম্পদের ধরন ও বছর পূর্ণ হওয়া (হাওল) নিয়ে মতভেদ ও ব্যক্তিগত অবস্থা আছে।
 */
@Composable
fun ZakatScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    var values by remember { mutableStateOf(FIELDS.associate { it.first to "" }) }
    var basis by remember { mutableStateOf(NisabBasis.SILVER) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val saved = FIELDS.associate { (name, _) -> name to (container.userData.string(key(name)).first() ?: "") }
        values = saved
        basis = if (container.userData.string(key("basis")).first() == "GOLD") NisabBasis.GOLD else NisabBasis.SILVER
        loaded = true
    }

    fun updateField(name: String, text: String) {
        values = values + (name to text)
        scope.launch { container.userData.putString(key(name), text.ifBlank { null }) }
    }

    fun num(name: String) = ZakatCalculator.parse(values[name].orEmpty())

    val input = ZakatInput(
        cash = num("cash"), goldGrams = num("gold"), silverGrams = num("silver"),
        goldPricePerGram = num("goldPrice"), silverPricePerGram = num("silverPrice"),
        businessAssets = num("business"), receivables = num("receivables"),
        liabilities = num("liabilities"), basis = basis
    )
    val result = ZakatCalculator.calculate(input)

    ScreenColumn("যাকাত ক্যালকুলেটর", onBack) {
        WarningCard(
            "এটি আনুমানিক হিসাব, ফতোয়া নয়। যাকাত ফরজ হতে সম্পদ নিসাব পরিমাণ হওয়ার পর এক বছর (হাওল) পূর্ণ হতে হয় বলে " +
                "সাধারণভাবে বলা হয়; আপনার অবস্থায় প্রযোজ্য কি না এবং কোন সম্পদ ধরা হবে, তা যোগ্য আলেমের কাছে জেনে নিন।"
        )
        Text(
            "সব অঙ্ক এই ফোনে সংরক্ষিত থাকে, কোথাও পাঠানো হয় না। সংখ্যা বাংলা বা ইংরেজি—যেকোনো অঙ্কে লিখতে পারেন। " +
                "স্বর্ণ-রূপার দর অ্যাপ নিজে আনে না; আপনার এলাকার বর্তমান বাজারদর নিজে লিখুন।",
            style = MaterialTheme.typography.bodySmall
        )

        SectionTitle("নিসাব কীসের হিসাবে ধরবেন")
        BasisRow("রূপার নিসাব (≈ ${DateFormatBn.digits("612.36")} গ্রাম)", basis == NisabBasis.SILVER) {
            basis = NisabBasis.SILVER
            scope.launch { container.userData.putString(key("basis"), "SILVER") }
        }
        BasisRow("স্বর্ণের নিসাব (≈ ${DateFormatBn.digits("87.48")} গ্রাম)", basis == NisabBasis.GOLD) {
            basis = NisabBasis.GOLD
            scope.launch { container.userData.putString(key("basis"), "GOLD") }
        }
        Text(
            "কোন ধাতুর নিসাব ধরা হবে তা নিয়ে আলেমদের মধ্যে মতভেদ আছে। রূপার নিসাব কম মূল্যের, তাই বেশি মানুষ নিসাবের আওতায় আসেন। " +
                "মানগুলো ৭.৫ ভরি স্বর্ণ ও ৫২.৫ ভরি রূপার প্রচলিত হিসাব; কেউ কেউ স্বর্ণের নিসাব ৮৫ গ্রাম ধরেন। আপনি যে মত অনুসরণ করেন, সেটি জেনে নিন।",
            style = MaterialTheme.typography.bodySmall
        )

        SectionTitle("আপনার সম্পদ ও ঋণ")
        if (loaded) {
            FIELDS.forEach { (name, label) -> NumberField(label, values[name].orEmpty()) { updateField(name, it) } }
        }

        SectionTitle("হিসাব")
        ResultCard(result, basis)

        OutlinedButton(onClick = {
            FIELDS.forEach { (name, _) -> updateField(name, "") }
        }) { Text("সব ঘর খালি করুন") }

        SectionTitle("আরও জানতে")
        MenuCard("মাসআলা দেখুন", "যাকাত, নিসাব ও দান সম্পর্কিত প্রশ্নোত্তর") { onNavigate(Routes.MASAIL) }
    }
}

@Composable
private fun BasisRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

private fun fmt(value: Double): String = DateFormatBn.digits(String.format(Locale.US, "%,.2f", value))

@Composable
private fun ResultCard(r: ZakatResult, basis: NisabBasis) {
    val metal = if (basis == NisabBasis.SILVER) "রূপা" else "স্বর্ণ"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Line("স্বর্ণের মূল্য", fmt(r.goldValue))
            Line("রূপার মূল্য", fmt(r.silverValue))
            Line("মোট সম্পদ", fmt(r.totalAssets))
            Line("বাদ: ঋণ", fmt(r.liabilities))
            Line("নিট সম্পদ", fmt(r.netWealth), bold = true)
            Line("নিসাব ($metal, ${DateFormatBn.digits(String.format(Locale.US, "%.2f", r.nisabGrams))} গ্রাম)", fmt(r.nisabValue))

            when {
                r.priceMissing -> Text(
                    "নিসাব বের করতে ${metal}র বর্তমান দর (প্রতি গ্রাম) লিখুন।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                r.reachesNisab -> {
                    Text(
                        "নিট সম্পদ নিসাব পরিমাণ বা তার বেশি। বছর পূর্ণ হয়ে থাকলে আনুমানিক যাকাত (২.৫%):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(fmt(r.zakatDue), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                else -> Text(
                    "নিট সম্পদ নিসাবের চেয়ে কম, তাই এই হিসাবে যাকাত আসছে না।",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun Line(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}
