package com.islamshikhi.app.ui.ibadah

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.MenuCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.navigation.Routes
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.launch

private data class Step(val id: String, val textBn: String)
private data class StepGroup(val titleBn: String, val steps: List<Step>)

private const val DONE_SET = "hajj_done"

private val UMRAH = StepGroup(
    "উমরার চেকলিস্ট",
    listOf(
        Step("u1", "ইহরামের নিয়ত ও তালবিয়া"),
        Step("u2", "কাবাঘরের সাত চক্কর তাওয়াফ"),
        Step("u3", "মাকামে ইবরাহীমে দুই রাকাত নামাজ"),
        Step("u4", "সাফা-মারওয়ায় সাত চক্কর সাঈ"),
        Step("u5", "মাথা মুণ্ডন বা চুল ছোট করা")
    )
)

private val HAJJ = listOf(
    StepGroup("হজ্জের প্রস্তুতি", listOf(
        Step("p1", "পাসপোর্ট, ভিসা ও টিকার কাগজ গুছিয়ে নেওয়া"),
        Step("p2", "হজ্জের নিয়ম-কানুন আলেমের কাছে শেখা"),
        Step("p3", "ঋণ পরিশোধ, পরিবারের হক বুঝিয়ে দেওয়া ও ওসিয়ত")
    )),
    StepGroup("৮ জিলহজ্জ", listOf(
        Step("d8a", "ইহরাম ও তালবিয়া"),
        Step("d8b", "মিনায় গমন, পাঁচ ওয়াক্ত নামাজ ও রাত্রিযাপন")
    )),
    StepGroup("৯ জিলহজ্জ", listOf(
        Step("d9a", "আরাফার ময়দানে অবস্থান (সূর্য ঢলা থেকে সূর্যাস্ত)"),
        Step("d9b", "মুযদালিফায় মাগরিব-এশা ও রাত্রিযাপন")
    )),
    StepGroup("১০ জিলহজ্জ", listOf(
        Step("d10a", "বড় জামারায় সাতটি কংকর"),
        Step("d10b", "কুরবানি (তামাত্তু ও কিরানে)"),
        Step("d10c", "মাথা মুণ্ডন বা চুল ছোট করা"),
        Step("d10d", "তাওয়াফে ইফাযা (ফরয তাওয়াফ)"),
        Step("d10e", "হজ্জের সাঈ (তামাত্তুকারী ও যার সাঈ বাকি)")
    )),
    StepGroup("১১ থেকে ১৩ জিলহজ্জ", listOf(
        Step("d11", "১১ তারিখ: তিন জামারায় কংকর"),
        Step("d12", "১২ তারিখ: তিন জামারায় কংকর"),
        Step("d13", "১৩ তারিখ (যদি থাকেন): তিন জামারায় কংকর")
    )),
    StepGroup("বিদায়", listOf(Step("w1", "বিদায়ী তাওয়াফ")))
)

/** হজ্জ-উমরার ধাপে ধাপে চেকলিস্ট; বিস্তারিত ও আয়াত-হাদীসসহ আলোচনা জ্ঞান-প্রবন্ধে। টিক শুধু ফোনে সংরক্ষিত। */
@Composable
fun HajjScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val done by container.userData.set(DONE_SET).collectAsState(initial = emptySet())

    @Composable
    fun Group(g: StepGroup) {
        val count = g.steps.count { it.id in done }
        SectionTitle("${g.titleBn} (${DateFormatBn.digits(count)}/${DateFormatBn.digits(g.steps.size)})")
        g.steps.forEach { step ->
            val checked = step.id in done
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = checked, role = Role.Checkbox, onValueChange = {
                        scope.launch { container.userData.toggleInSet(DONE_SET, step.id) }
                    })
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = checked, onCheckedChange = null)
                Text(step.textBn, modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    ScreenColumn("হজ্জ-উমরা গাইড", onBack) {
        MenuCard("ধাপে ধাপে নির্দেশনা", "আয়াত ও হাদীসসহ বিস্তারিত আলোচনা") { onNavigate(Routes.ilm("hajj_steps")) }
        MenuCard("হজ্জ ও উমরা: গুরুত্ব ও শিক্ষা", "কার ওপর ফরয, ফযীলত ও মূল শিক্ষা") { onNavigate(Routes.ilm("hajj")) }
        MenuCard("দোয়া ও যিকির", "অর্থ ও সূত্রসহ") { onNavigate(Routes.DUA) }
        InfoCard(
            title = "চেকলিস্ট",
            body = "যে ধাপ শেষ করলেন তাতে টিক দিন। সংক্ষিপ্ত তালিকা; নিয়ম ও মাযহাবভেদের বিস্তারিত জানতে নির্দেশনা-প্রবন্ধ ও আপনার আলেমের সাহায্য নিন।"
        )
        Group(UMRAH)
        HAJJ.forEach { Group(it) }
        TextButton(onClick = {
            scope.launch { container.userData.putSet(DONE_SET, emptySet()) }
        }) { Text("চেকলিস্ট নতুন করে শুরু করুন") }
    }
}
