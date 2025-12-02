package com.atom.unimarket.presentation.di

import com.atom.unimarket.presentation.auth.AuthViewModel
import com.atom.unimarket.presentation.chat.ChatViewModel
import com.atom.unimarket.presentation.chatbot.ChatbotViewModel
import com.atom.unimarket.presentation.dashboard.DashboardViewModel
import com.atom.unimarket.presentation.products.ProductViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.messaging.FirebaseMessaging
import com.atom.unimarket.data.local.database.CartDatabase
import com.atom.unimarket.data.local.dao.CartDao
import com.atom.unimarket.data.repository.CartRepositoryImpl
import com.atom.unimarket.domain.repository.CartRepository
import com.atom.unimarket.presentation.viewmodel.CartViewModel
import com.atom.unimarket.presentation.checkout.CheckoutViewModel
import com.atom.unimarket.presentation.seller.orders.SellerOrdersViewModel
import com.atom.unimarket.data.repository.OrderRepositoryImpl
import com.atom.unimarket.domain.repository.OrderRepository
import com.atom.unimarket.data.repository.FCMRepositoryImpl
import com.atom.unimarket.domain.repository.FCMRepository
import com.atom.unimarket.data.repository.UserRepositoryImpl
import com.atom.unimarket.domain.repository.UserRepository
import com.atom.unimarket.data.service.NotificationServiceImpl
import com.atom.unimarket.domain.service.NotificationService
import com.atom.unimarket.data.repository.CheckoutRepositoryImpl
import com.atom.unimarket.domain.repository.CheckoutRepository
import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Este es tu módulo principal
val appModule = module {

    // --- Definición de Singletons (Una sola instancia para toda la app) ---
    // Así es como le decimos a Koin que provea las instancias de Firebase
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }
    single { FirebaseMessaging.getInstance() }

    // --- Room Database ---
    single {
        Room.databaseBuilder(
            androidContext(),
            CartDatabase::class.java,
            "cart_database"
        ).build()
    }

    single { get<CartDatabase>().cartDao() }

    // --- Repositories ---
    single<CartRepository> { CartRepositoryImpl(get()) }
    single<OrderRepository> { OrderRepositoryImpl(get()) }
    single<FCMRepository> { FCMRepositoryImpl(get(), get(), get()) }
    single<CheckoutRepository> { CheckoutRepositoryImpl(get(), get()) }

    // --- Services ---
    single<NotificationService> { NotificationServiceImpl(get()) }

    // --- Definición de ViewModels ---
    // Koin creará una nueva instancia de ViewModel para cada pantalla que lo necesite

    // AuthViewModel necesita Auth, Firestore, Storage, y FCMRepository
    viewModel { AuthViewModel(get(), get(), get(), get()) }

    // ProductViewModel necesita las 3 instancias de Firebase
    viewModel { ProductViewModel(get(), get(), get()) }

    // ChatViewModel necesita Firestore y Auth
    viewModel { ChatViewModel(get(), get()) }

    // Estos no necesitan dependencias de Firebase (por ahora)
    viewModel { ChatbotViewModel() }
    viewModel { DashboardViewModel() }
    single<UserRepository> { UserRepositoryImpl(get()) }

    viewModel { CartViewModel(get()) }
    viewModel { CheckoutViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { SellerOrdersViewModel(get(), get(), get()) }
    viewModel { ProductViewModel(get(), get(), get()) }
    viewModel { com.atom.unimarket.presentation.history.HistoryViewModel(get(), get(), get()) }
}