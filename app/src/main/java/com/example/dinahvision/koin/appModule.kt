package com.example.dinahvision.koin

import androidx.room.Room
import com.example.dinahvision.repository.AppDatabase
import com.example.dinahvision.repository.SessionManager
import com.example.dinahvision.repository.UserDAO
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    single { get<AppDatabase>().sessionDao() }

    single { SessionManager(get()) }
    single { UserDAO() }
}