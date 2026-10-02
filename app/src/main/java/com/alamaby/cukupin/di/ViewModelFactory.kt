package com.alamaby.cukupin.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Factory ViewModel sederhana untuk manual DI. */
@Suppress("UNCHECKED_CAST")
fun <T : ViewModel> viewModelFactory(create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <M : ViewModel> create(modelClass: Class<M>): M = create() as M
    }
