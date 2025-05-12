package com.example.socialme_interfazgrafica.data

import com.example.socialme_interfazgrafica.model.ActividadCreateDTO
import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.ActividadUpdateDTO
import com.example.socialme_interfazgrafica.model.ComunidadCreateDTO
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.ComunidadUpdateDTO
import com.example.socialme_interfazgrafica.model.DenunciaCreateDTO
import com.example.socialme_interfazgrafica.model.LoginResponse
import com.example.socialme_interfazgrafica.model.ParticipantesActividadDTO
import com.example.socialme_interfazgrafica.model.ParticipantesComunidadDTO
import com.example.socialme_interfazgrafica.model.RegistroResponse
import com.example.socialme_interfazgrafica.model.UsuarioDTO
import com.example.socialme_interfazgrafica.model.UsuarioLoginDTO
import com.example.socialme_interfazgrafica.model.UsuarioRegisterDTO
import com.example.socialme_interfazgrafica.model.UsuarioUpdateDTO
import com.example.socialme_interfazgrafica.model.VerificacionDTO
import com.google.gson.GsonBuilder
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
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

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

    @GET("/Actividad/verActividadesPublicasEnZona/{distanciaKm}/{username}")
    suspend fun verActividadesPublicas(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("distanciaKm") distanciaKm: Float
    ): Response<List<ActividadDTO>>

    @GET("/Comunidad/verComunidadesPublicasEnZona/{distanciaKm}/{username}")
    suspend fun verComunidadesPublicas(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("distanciaKm") distanciaKm: Float
    ): Response<List<ComunidadDTO>>

    @GET("/Actividad/verTodasActividadesPublicas")
    suspend fun verTodasActividadesPublicas(
        @Header("Authorization") token: String,
    ): Response<List<ActividadDTO>>

    @GET("/Comunidad/verTodasComunidadesPublicas")
    suspend fun verTodasComunidadesPublicas(
        @Header("Authorization") token: String,
    ): Response<List<ComunidadDTO>>

    @GET("Usuario/verTodosLosUsuarios/{username}")
    suspend fun verTodosLosUsuarios(
        @Header("Authorization") token: String,
        @Path("username") username:String
    ): Response<List<UsuarioDTO>>

    @GET("Actividad/verActividadNoParticipaUsuario/{username}")
    suspend fun verActividadNoParticipaUsuario(
        @Header("Authorization") token: String,
        @Path("username") username:String
    ): Response<List<ActividadDTO>>

    @GET("Actividad/verActividadPorId/{id}")
    suspend fun verActividadPorId(
        @Header("Authorization") token: String,
        @Path("id") username: String,
    ): Response <ActividadDTO>

    @GET("Comunidad/verComunidadPorUrl/{url}")
    suspend fun verComunidadPorUrl(
        @Header("Authorization") token: String,
        @Path("url") url:String
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

    @POST("/Comunidad/unirseComunidadPorCodigo/{codigo}")
    suspend fun unirseComunidadPorCodigo(
        @Body participantesComunidadDTO: ParticipantesComunidadDTO,
        @Path("codigo") codigo: String,
        @Header("Authorization") token: String
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

    @GET("Comunidad/verComunidadesPorUsuarioCreador/{username}")
    suspend fun verComunidadesPorUsuarioCreador(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<ComunidadDTO>>

    @GET("Comunidad/verificarCreadorAdministradorComunidad/{username}/{comunidadUrl}")
    suspend fun verificarCreadorAdministradorComunidad(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("comunidadUrl") comunidadUrl: String
    ): Response<Boolean>


    @GET("Actividad/verificarCreadorAdministradorActividad/{username}/{idActividad}")
    suspend fun verificarCreadorAdministradorActividad(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("idActividad") idActvidad: String
    ): Response<Boolean>

    @PUT("Comunidad/modificarComunidad")
    suspend fun modificarComunidad(
        @Header("Authorization") token: String,
        @Body comunidadUpdateDTO: ComunidadUpdateDTO
    ): Response<ComunidadDTO>

    @PUT("Actividad/modificarActividad")
    suspend fun modificarActividad(
        @Header("Authorization") token: String,
        @Body actividadUpdateDTO: ActividadUpdateDTO
    ): Response<ActividadDTO>


    @DELETE("Actividad/eliminarActividad/{id}")
    suspend fun eliminarActividad(
        @Header("Authorization") token: String,
        @Path("id") id : String
    ): Response<ActividadDTO>


    @POST("/Comunidad/crearComunidad")
    suspend fun crearComunidad(
        @Header("Authorization") token: String,
        @Body comunidadCreateDTO: ComunidadCreateDTO
    ): Response<ComunidadDTO>

    @POST("/Actividad/crearActividad")
    suspend fun crearActividad(
        @Header("Authorization") token: String,
        @Body actividadCreateDTO: ActividadCreateDTO
    ): Response<ActividadDTO>

    @DELETE("/Comunidad/eliminarComunidad/{url}")
    suspend fun eliminarComunidad(
        @Header("Authorization") token: String,
        @Path("url") url:String
    ): Response<ActividadCreateDTO>

    @DELETE("/Usuario/eliminarUsuario/{username}")
    suspend fun eliminarUsuario(
        @Header("Authorization") token: String,
        @Path("username") username : String
    ): Response<UsuarioDTO>

    @PUT("/Usuario/modificarUsuario")
    suspend fun modificarUsuario(
        @Header("Authorization") token: String,
        @Body usuarioUpdateDTO: UsuarioUpdateDTO
    ): Response<ActividadCreateDTO>

    // Añadir a tu RetrofitService.kt
    @POST("/Usuario/verificarCodigo")
    suspend fun verificarCodigo(
        @Body verificacionDTO: VerificacionDTO
    ): Response<Boolean>

    @GET("/Usuario/reenviarCodigo/{email}")
    suspend fun reenviarCodigo(
        @Path("email") email: String
    ): Response<Boolean>

    @POST("/Denuncia/crearDenuncia")
    suspend fun crearDenuncia(
        @Header("Authorization") token: String,
        @Body denunciaCreateDTO: DenunciaCreateDTO
    ): Response<DenunciaCreateDTO>

    object RetrofitServiceFactory {
        fun makeRetrofitService(): RetrofitService {
            // Configurar Gson para formatear correctamente las fechas
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")  // Formato ISO 8601 compatible con Java
                .create()

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)  // Añadido para evitar timeouts
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://social-me-tfg.onrender.com")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))  // Usar el Gson personalizado
                .build()
                .create(RetrofitService::class.java)
        }
    }
}