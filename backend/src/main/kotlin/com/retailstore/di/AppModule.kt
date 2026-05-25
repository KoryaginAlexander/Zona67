package com.retailstore.di

import com.retailstore.data.repository.*
import com.retailstore.domain.repository.*
import com.retailstore.utils.JwtUtils
import org.koin.dsl.module

val appModule = module {
    single {
        JwtUtils(
            secret = System.getenv("JWT_SECRET") ?: "dev-secret-please-change",
            issuer = "retail-store",
            audience = "retail-store-users",
            accessTokenTtlMinutes = 15L,
            refreshTokenTtlDays = 30L
        )
    }

    single<UserRepository> { UserRepositoryImpl() }
    single<CategoryRepository> { CategoryRepositoryImpl() }
    single<ProductRepository> { ProductRepositoryImpl() }
    single<CartRepository> { CartRepositoryImpl() }
    single<WishlistRepository> { WishlistRepositoryImpl() }
    single<OrderRepository> { OrderRepositoryImpl() }
}
