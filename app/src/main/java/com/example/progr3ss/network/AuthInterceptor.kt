package com.example.progr3ss.network

import android.content.Context
import com.example.progr3ss.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
class AuthInterceptor(context: Context) : Interceptor {
    private val sessionManager =
        SessionManager(context.applicationContext)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
        if (original.header("Authorization") == null) {
            sessionManager.fetchAuthToken()?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
        }
        return chain.proceed(requestBuilder.build())
    }
}