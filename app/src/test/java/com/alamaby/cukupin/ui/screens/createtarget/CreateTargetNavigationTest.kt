package com.alamaby.cukupin.ui.screens.createtarget

import com.alamaby.cukupin.ui.navigation.hasPreviousDestination
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Aturan tampilan tombol batal di layar Buat Target.
 *
 * Layar ini punya dua pemasuk yang berbeda:
 *  - sebagai startDestination (belum ada target aktif), dan
 *  - dibuka dari Dashboard lewat onCreateTarget.
 *
 * Hanya pemasuk kedua yang menyisakan halaman sebelumnya di back stack, jadi
 * hanya pemasuk itu yang boleh menampilkan tombol batal. Test ini mengunci
 * aturan tersebut supaya layar tidak pernah kembali menampilkan tombol yang
 * tidak punya tujuan.
 */
class CreateTargetNavigationTest {

    @Test
    fun `layar awal tidak menampilkan tombol batal`() {
        // startDestination: back stack hanya berisi form itu sendiri, jadi
        // NavController.previousBackStackEntry bernilai null.
        assertFalse(hasPreviousDestination(hasPreviousBackStackEntry = false))
    }

    @Test
    fun `setelah onboarding juga tidak menampilkan tombol batal`() {
        // Lewati/Lanjut memakai popUpTo(ONBOARDING, inclusive), jadi onboarding
        // hilang dari back stack dan form ini menjadi satu-satunya entri.
        assertFalse(hasPreviousDestination(hasPreviousBackStackEntry = false))
    }

    @Test
    fun `dibuka dari dashboard menampilkan tombol batal`() {
        // Dashboard masih ada di back stack, sehingga user bisa kembali ke sana.
        assertTrue(hasPreviousDestination(hasPreviousBackStackEntry = true))
    }
}