package com.app.shoppy.di

import com.app.shoppy.data.remote.ShoppyApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // IMPORTANT: For Android emulator to access localhost, use 10.0.2.2
    // For physical devices, use the computer's actual local IP address (e.g. 192.168.x.x)
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideShoppyApiService(retrofit: Retrofit): ShoppyApiService {
        return retrofit.create(ShoppyApiService::class.java)
    }
}
