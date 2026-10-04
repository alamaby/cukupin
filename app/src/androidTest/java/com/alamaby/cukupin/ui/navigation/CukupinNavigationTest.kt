package com.alamaby.cukupin.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.alamaby.cukupin.data.prefs.OnboardingPreferences
import com.alamaby.cukupin.di.AppContainer
import com.alamaby.cukupin.domain.model.BudgetTarget
import com.alamaby.cukupin.domain.model.ExpenseCategory
import com.alamaby.cukupin.domain.model.ExpenseTransaction
import com.alamaby.cukupin.domain.model.FundAdjustment
import com.alamaby.cukupin.domain.model.TargetLifecycleStatus
import com.alamaby.cukupin.domain.repository.BudgetTargetRepository
import com.alamaby.cukupin.domain.repository.CategoryRepository
import com.alamaby.cukupin.domain.repository.ExpenseRepository
import com.alamaby.cukupin.domain.repository.FundAdjustmentRepository
import com.alamaby.cukupin.domain.time.DateProvider
import com.alamaby.cukupin.ui.theme.CukupinTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Perilaku navigasi yang tidak bisa dibuktikan unit test.
 *
 * Test ini merender [CukupinApp] apa adanya lewat NavController milik Compose,
 * lalu memeriksa apa yang benar-benar dilihat user: apakah tombol "Batal" ada,
 * dan ke mana tombol itu membawa.
 *
 * Kenapa harus instrumentation test: aturan di sini bergantung pada keadaan
 * back stack milik NavController sungguhan. Fungsi murni yang menerima
 * `previousBackStackEntry` hanya menguji dirinya sendiri — hasilnya selalu benar
 * tanpa pernah menyentuh perilaku nyata.
 *
 * Kenapa bukan `TestNavHostController`: di navigation 2.7.7 controller itu memasang
 * `TestNavigatorProvider` yang mengembalikan `TestNavigator` untuk semua nama,
 * sedangkan `composable()` mengharapkan `ComposeNavigator`. Hasilnya
 * ClassCastException sebelum tes pertama sempat jalan. Kelasnya `final` juga, jadi
 * tidak bisa ditimpa.
 *
 * Konsekuensi yang diterima: test ini hanya bisa memeriksa apa yang terlihat di
 * layar. Invariant back stack (setelah menyimpan target, back stack tidak boleh
 * memuat dua entri Dashboard) tidak bisa dibuktikan dari sini dan dijaga lewat
 * kode, bukan lewat test.
 *
 * Repository di bawah memakai data in-memory, jadi test tidak menyentuh Room
 * dan tidak bergantung pada isi database device.
 */
@RunWith(AndroidJUnit4::class)
class CukupinNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val targetRepository = FakeBudgetTargetRepository()

    /**
     * Tanggal tetap supaya assertion tidak ikut bergeser tiap hari, dan supaya
     * rentang default form (mulai .. selesai +29 hari) selalu valid.
     */
    private val today: LocalDate = LocalDate.of(2026, 10, 4)

    private fun container() = AppContainer(
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        targetRepository = targetRepository,
        expenseRepository = FakeExpenseRepository(),
        categoryRepository = FakeCategoryRepository(),
        adjustmentRepository = FakeFundAdjustmentRepository(),
        onboardingPreferences = OnboardingPreferences(
            InstrumentationRegistry.getInstrumentation().targetContext
        ),
        dateProvider = FixedDateProvider(today)
    )

    private fun setContent(startDestination: String) {
        composeTestRule.setContent {
            CukupinTheme {
                CukupinApp(container = container(), startDestination = startDestination)
            }
        }
    }

    /**
     * Jalur masuk pertama: layar awal saat belum ada target aktif.
     *
     * Di jalur ini form adalah satu-satunya entri back stack, jadi tidak ada
     * halaman sebelumnya. Tombol yang tidak punya tujuan lebih baik tidak
     * ditampilkan sama sekali daripada menggantung.
     */
    @Test
    fun layarAwalTanpaTargetTidakMenampilkanTombolBatal() {
        setContent(Routes.CREATE_TARGET)

        composeTestRule.onNodeWithText("Buat Target Dana").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Batal").assertDoesNotExist()
    }

    /**
     * Jalur masuk kedua: setelah onboarding "Lewati" ditekan.
     *
     * `onFinish` menavigasi dengan `popUpTo(ONBOARDING) { inclusive = true }`,
     * jadi onboarding hilang dari back stack dan form ini berdiri sendiri.
     */
    @Test
    fun setelahOnboardingTetapTidakMenampilkanTombolBatal() {
        setContent(Routes.ONBOARDING)

        composeTestRule.onNodeWithText("Lewati").performClick()

        composeTestRule.onNodeWithText("Buat Target Dana").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Batal").assertDoesNotExist()
    }

    /**
     * Jalur masuk ketiga: dibuka dari Dashboard lewat "Buat Target Baru".
     *
     * Di sini Dashboard sengaja tidak di-pop, jadi form punya halaman sebelumnya
     * dan tombol batal tampil. Inilah kasus yang tidak pernah bisa dibuktikan
     * sebelumnya, karena `popUpTo(DASHBOARD, inclusive)` lama membuat form ini
     * selalu menjadi satu-satunya entri back stack sehingga tombolnya tak pernah
     * muncul.
     */
    @Test
    fun dibukaDariDashboardMenampilkanTombolBatal() {
        setContent(Routes.DASHBOARD)

        composeTestRule.onNodeWithText("Buat Target Baru").performClick()

        composeTestRule.onNodeWithText("Buat Target Dana").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Batal").assertIsDisplayed()
    }

    /**
     * Tombol batal harus benar-benar mengembalikan user ke Dashboard, bukan
     * sekadar hilang dari layar.
     */
    @Test
    fun tombolBatalKembaliKeDashboard() {
        setContent(Routes.DASHBOARD)

        composeTestRule.onNodeWithText("Buat Target Baru").performClick()
        composeTestRule.onNodeWithContentDescription("Batal").performClick()

        composeTestRule.onNodeWithText("Buat Target Baru").assertIsDisplayed()
    }

    /**
     * Menyimpan target dari jalur Dashboard harus mendarat di Dashboard dengan
     * data target baru. Indirect ini juga menangkap regresi kalau navigasi
     * balik ke Dashboard sempat menumpuk dua entri.
     */
    @Test
    fun menyimpanTargetDariDashboardTampilDiDashboardDenganTargetBaru() {
        setContent(Routes.DASHBOARD)

        composeTestRule.onNodeWithText("Buat Target Baru").performClick()
        composeTestRule
            .onNode(hasSetTextAction() and hasText("Nama target", substring = true))
            .performTextInput("Dana Darurat")
        composeTestRule
            .onNode(hasSetTextAction() and hasText("Dana awal", substring = true))
            .performTextInput("1500000")
        composeTestRule.onNodeWithText("Simpan Target").performClick()

        composeTestRule.onNodeWithText("Dana Darurat").assertIsDisplayed()
    }
}

private class FixedDateProvider(private val date: LocalDate) : DateProvider {
    override fun today(): LocalDate = date
}

private class FakeBudgetTargetRepository : BudgetTargetRepository {
    private val active = MutableStateFlow<BudgetTarget?>(null)

    override fun observeActiveTarget(): Flow<BudgetTarget?> = active

    override fun observeTarget(id: String): Flow<BudgetTarget?> =
        active.map { target -> target?.takeIf { it.id == id } }

    override fun observeLatestTarget(): Flow<BudgetTarget?> = active

    override suspend fun getTarget(id: String): BudgetTarget? = active.value?.takeIf { it.id == id }

    override suspend fun create(target: BudgetTarget) {
        active.value = target
    }

    override suspend fun update(target: BudgetTarget) {
        if (active.value?.id == target.id) active.value = target
    }

    override suspend fun complete(id: String) {
        active.value = active.value?.takeIf { it.id == id }?.copy(
            lifecycleStatus = TargetLifecycleStatus.COMPLETED
        )
    }

    override suspend fun archive(id: String) {
        active.value = null
    }
}

private class FakeExpenseRepository : ExpenseRepository {
    override fun observeByTarget(targetId: String): Flow<List<ExpenseTransaction>> = flowOf(emptyList())
    override suspend fun getById(transactionId: String): ExpenseTransaction? = null
    override suspend fun create(transaction: ExpenseTransaction) = Unit
    override suspend fun update(transaction: ExpenseTransaction) = Unit
    override suspend fun delete(transactionId: String) = Unit
}

private class FakeCategoryRepository : CategoryRepository {
    override fun observeActiveCategories(): Flow<List<ExpenseCategory>> = flowOf(emptyList())
    override suspend fun seedDefaults() = Unit
}

private class FakeFundAdjustmentRepository : FundAdjustmentRepository {
    override fun observeByTarget(targetId: String): Flow<List<FundAdjustment>> = flowOf(emptyList())
    override suspend fun add(adjustment: FundAdjustment) = Unit
}
