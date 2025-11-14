package com.atom.unimarket.presentation.di

import com.atom.unimarket.presentation.auth.AuthViewModel
import com.atom.unimarket.presentation.chat.ChatViewModel
import com.atom.unimarket.presentation.chatbot.ChatbotViewModel
import com.atom.unimarket.presentation.dashboard.DashboardViewModel
import com.atom.unimarket.presentation.products.ProductViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Este es tu módulo principal
val appModule = module {

    // --- Definición de Singletons (Una sola instancia para toda la app) ---
    // Así es como le decimos a Koin que provea las instancias de Firebase
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }

    // --- Definición de ViewModels ---
    // Koin creará una nueva instancia de ViewModel para cada pantalla que lo necesite

    // AuthViewModel necesita Auth, Firestore, y Storage
    viewModel { AuthViewModel(get(), get(), get()) }

    // ProductViewModel necesita las 3 instancias de Firebase
    viewModel { ProductViewModel(get(), get(), get()) }

    // ChatViewModel necesita Firestore y Auth
    viewModel { ChatViewModel(get(), get()) }

    // Estos no necesitan dependencias de Firebase (por ahora)
    viewModel { ChatbotViewModel() }
    viewModel { DashboardViewModel() }
}