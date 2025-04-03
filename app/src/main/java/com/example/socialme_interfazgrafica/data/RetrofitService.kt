package com.example.socialme_interfazgrafica.data

import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.LoginResponse
import com.example.socialme_interfazgrafica.model.RegistroResponse
import com.example.socialme_interfazgrafica.model.UsuarioLoginDTO
import com.example.socialme_interfazgrafica.model.UsuarioRegisterDTO
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface RetrofitService {
    @POST("/Usuario/register")
    suspend fun insertUser(
        @Body usuario: UsuarioRegisterDTO
    ): Response<RegistroResponse>

    @POST("/Usuario/login")
    suspend fun loginUser(
        @Body usuario: UsuarioLoginDTO
    ): Response<LoginResponse>

    @GET("Comunidad/verComunidadPorUsuario/{username}")
    suspend fun verComunidadPorUsuario(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<ComunidadDTO>>

    @GET("Actividad/verActividadPorUsername/{username}")
    suspend fun verActividadPorUsername(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<ActividadDTO>>

    @GET("Actividad/verActividadesPublicasEnZona")
    suspend fun verActividadesPublicasEnZona(
        @Header("Authorization") token: String
    ): Response<List<ActividadDTO>>

    @GET("Actividad/verActividadPorComunidad/{username}")
    suspend fun verActividadesPorComunidad(
        @Header("Authorization") token: String,
        @Path("username") username:String
    ): Response<List<ActividadDTO>>


    object RetrofitServiceFactory {
        fun makeRetrofitService(): RetrofitService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
            return Retrofit.Builder()
                .baseUrl("https://social-me-tfg.onrender.com")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RetrofitService::class.java)
        }
    }
}