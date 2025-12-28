package org.retrofun.project.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.retrofun.project.data.repository.GameRepositoryImpl
import org.retrofun.project.domain.repository.GameRepository
import org.retrofun.project.presentation.gamelist.GameListScreenModel
import org.retrofun.project.presentation.gamedetail.GameDetailScreenModel
import org.retrofun.project.presentation.gameplayer.GamePlayerScreenModel

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, platformModule)
    }
}

val appModule = module {
    single<GameRepository> { GameRepositoryImpl() }
    
    // ScreenModels
    factory { GameListScreenModel(get()) }
    factory { GameDetailScreenModel(get()) }
    factory { GamePlayerScreenModel(get(), get(), get()) }
}

expect val platformModule: Module
