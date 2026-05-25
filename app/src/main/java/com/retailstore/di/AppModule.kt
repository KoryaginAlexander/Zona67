package com.retailstore.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.retailstore.BuildConfig
import com.retailstore.data.local.TokenDataStore
import com.retailstore.data.local.cart.AppDatabase
import com.retailstore.data.local.cart.GuestCartDao
import com.retailstore.data.remote.api.*
import com.retailstore.data.remote.interceptor.AuthInterceptor
import com.retailstore.data.repository.*
import com.retailstore.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton fun provideAuthApi(r: Retrofit): AuthApi = r.create(AuthApi::class.java)
    @Provides @Singleton fun provideUserApi(r: Retrofit): UserApi = r.create(UserApi::class.java)
    @Provides @Singleton fun provideProductApi(r: Retrofit): ProductApi = r.create(ProductApi::class.java)
    @Provides @Singleton fun provideCategoryApi(r: Retrofit): CategoryApi = r.create(CategoryApi::class.java)
    @Provides @Singleton fun provideCartApi(r: Retrofit): CartApi = r.create(CartApi::class.java)
    @Provides @Singleton fun provideWishlistApi(r: Retrofit): WishlistApi = r.create(WishlistApi::class.java)
    @Provides @Singleton fun provideOrderApi(r: Retrofit): OrderApi = r.create(OrderApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "retailstore.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideGuestCartDao(db: AppDatabase): GuestCartDao = db.guestCartDao()
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository
    @Binds @Singleton abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository
    @Binds @Singleton abstract fun bindWishlistRepository(impl: WishlistRepositoryImpl): WishlistRepository
    @Binds @Singleton abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository
    @Binds @Singleton abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
