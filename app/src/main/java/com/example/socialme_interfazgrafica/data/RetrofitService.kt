package com.example.socialme_interfazgrafica.data

import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.LoginResponse
import com.example.socialme_interfazgrafica.model.ParticipantesActividadDTO
import com.example.socialme_interfazgrafica.model.ParticipantesComunidadDTO
import com.example.socialme_interfazgrafica.model.RegistroResponse
import com.example.socialme_interfazgrafica.model.UsuarioDTO
import com.example.socialme_interfazgrafica.model.UsuarioLoginDTO
import com.example.socialme_interfazgrafica.model.UsuarioRegisterDTO
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
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

    @GET("Actividad/verActividadNoParticipaUsuario/{username}")
    suspend fun verActividadNoParticipaUsuario(
        @Header("Authorization") token: String,
        @Path("username") username:String
    ): Response<List<ActividadDTO>>

    @GET("Actividad/verActividadPorId/{id}")
    suspend fun verActividadPorId(
        @Header("Authorization") token: String,
        @Path("id") username:String
    ): Response <ActividadDTO>

    @GET("Comunidad/verComunidadPorUrl/{url}")
    suspend fun verComunidadPorUrl(
        @Header("Authorization") token: String,
        @Path("url") username:String
    ): Response <ComunidadDTO>

    @POST("/Actividad/unirseActividad")
    suspend fun unirseActividad(
        @Body participantesActividadDTO: ParticipantesActividadDTO,
        @Header("Authorization") token: String,
    ): Response<RegistroResponse>

    @HTTP(method = "DELETE", path = "/Actividad/salirActividad", hasBody = true)
    suspend fun salirActividad(
        @Body participantesActividadDTO: ParticipantesActividadDTO,
        @Header("Authorization") token: String,
    ): Response<RegistroResponse>

    @POST("/Comunidad/unirseComunidad")
    suspend fun unirseComunidad(
        @Body participantesComunidadDTO: ParticipantesComunidadDTO,
        @Header("Authorization") token: String,
    ): Response<RegistroResponse>

    @POST("/Comunidad/booleanUsuarioApuntadoComunidad")
    suspend fun booleanUsuarioApuntadoComunidad(
        @Body participantesComunidadDTO: ParticipantesComunidadDTO,
        @Header("Authorization") token: String,
    ): Response<Boolean>


    @HTTP(method = "DELETE", path = "/Comunidad/salirComunidad", hasBody = true)
    suspend fun salirComunidad(
        @Body participantesComunidadDTO: ParticipantesComunidadDTO,
        @Header("Authorization") token: String,
    ): Response<RegistroResponse>

    @POST("/Actividad/booleanUsuarioApuntadoActividad")
    suspend fun booleanUsuarioApuntadoActividad(
        @Body participantesActividadDTO: ParticipantesActividadDTO,
        @Header("Authorization") token: String,
    ): Response<Boolean>

    @GET("Usuario/verUsuarioPorUsername/{username}")
    suspend fun verUsuarioPorUsername(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<UsuarioDTO>

    @GET("Actividad/verActividadesPorComunidad/{comunidad}")
    suspend fun verActividadesPorComunidad(
        @Header("Authorization") token: String,
        @Path("comunidad") username: String
    ): Response<List<ActividadDTO>>

    @GET("Actividad/contarUsuariosEnUnaActividad/{actividadId}")
    suspend fun contarUsuariosEnUnaActividad(
        @Header("Authorization") token: String,
        @Path("actividadId") actividadId: String
    ): Response<Int>

    @GET("Comunidad/contarUsuariosEnUnaComunidad/{comunidad}")
    suspend fun contarUsuariosEnUnaComunidad(
        @Header("Authorization") token: String,
        @Path("comunidad") comunidad: String
    ): Response<Int>

    @GET("Usuario/verUsuariosPorComunidad/{comunidad}")
    suspend fun verUsuariosPorComunidad(
        @Header("Authorization") token: String,
        @Path("comunidad") comunidad: String
    ): Response<List<UsuarioDTO>>

    @GET("Usuario/verUsuariosPorActividad/{actividadId}")
    suspend fun verUsuariosPorActividad(
        @Header("Authorization") token: String,
        @Path("actividadId") actividadId: String
    ): Response<List<UsuarioDTO>>

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