package com.example.socialme_interfazgrafica.data

import com.example.socialme_interfazgrafica.BuildConfig
import com.example.socialme_interfazgrafica.model.ActividadCreateDTO
import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.ActividadUpdateDTO
import com.example.socialme_interfazgrafica.model.BloqueoDTO
import com.example.socialme_interfazgrafica.model.CambiarContrasenaDTO
import com.example.socialme_interfazgrafica.model.ComunidadCreateDTO
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.ComunidadUpdateDTO
import com.example.socialme_interfazgrafica.model.DenunciaCreateDTO
import com.example.socialme_interfazgrafica.model.DenunciaDTO
import com.example.socialme_interfazgrafica.model.LoginResponse
import com.example.socialme_interfazgrafica.model.MensajeCreateDTO
import com.example.socialme_interfazgrafica.model.MensajeDTO
import com.example.socialme_interfazgrafica.model.ParticipantesActividadDTO
import com.example.socialme_interfazgrafica.model.ParticipantesComunidadDTO
import com.example.socialme_interfazgrafica.model.PaymentRequestDTO
import com.example.socialme_interfazgrafica.model.PaymentResponseDTO
import com.example.socialme_interfazgrafica.model.PaymentStatusDTO
import com.example.socialme_interfazgrafica.model.PaymentVerificationDTO
import com.example.socialme_interfazgrafica.model.SolicitudAmistadDTO
import com.example.socialme_interfazgrafica.model.UsuarioDTO
import com.example.socialme_interfazgrafica.model.UsuarioLoginDTO
import com.example.socialme_interfazgrafica.model.UsuarioRegisterDTO
import com.example.socialme_interfazgrafica.model.UsuarioUpdateDTO
import com.example.socialme_interfazgrafica.model.VerificacionDTO
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
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
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

//SERVICIO DE RETROFIT QUE MANEJA TODOS LOS ENDPOINTS DE LA APLICACION

interface RetrofitService {

    @POST("/Usuario/login")
    suspend fun loginUser(
        @Body usuario: UsuarioLoginDTO
    ): Response<LoginResponse>

    @POST("/Usuario/iniciarRegistro")
    suspend fun iniciarRegistro(@Body usuarioRegisterDTO: UsuarioRegisterDTO): Response<Map<String, String>>

    @POST("/Usuario/completarRegistro")
    suspend fun completarRegistro(@Body verificacionDTO: VerificacionDTO): Response<UsuarioDTO>

    @GET("/Usuario/reenviarCodigo/{email}")
    suspend fun reenviarCodigo(
        @Path("email") email: String
    ): Response<Boolean>
    @GET("/Usuario/verUsuarioPorUsername/{username}")
    suspend fun verUsuarioPorUsername(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<UsuarioDTO>

    @GET("/Usuario/verTodosLosUsuarios/{username}")
    suspend fun verTodosLosUsuarios(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<UsuarioDTO>>

    @GET("/Usuario/verUsuariosPorComunidad/{comunidad}")
    suspend fun verUsuariosPorComunidad(
        @Header("Authorization") token: String,
        @Path("comunidad") comunidad: String,
        @Query("usuarioActual") usuarioActual: String
    ): Response<List<UsuarioDTO>>

    @GET("/Usuario/verUsuariosPorActividad/{actividadId}")
    suspend fun verUsuariosPorActividad(
        @Header("Authorization") token: String,
        @Path("actividadId") actividadId: String,
        @Query("usuarioActual") usuarioActual: String
    ): Response<List<UsuarioDTO>>

    @PUT("/Usuario/iniciarModificacionUsuario")
    suspend fun iniciarModificacionUsuario(
        @Header("Authorization") token: String,
        @Body usuarioUpdateDTO: UsuarioUpdateDTO
    ): Response<Map<String, String>>

    @POST("/Usuario/completarModificacionUsuario")
    suspend fun completarModificacionUsuario(
        @Header("Authorization") token: String,
        @Body verificacionDTO: VerificacionDTO
    ): Response<UsuarioDTO>

    @GET("/Usuario/usuarioEsAdmin/{username}")
    suspend fun usuarioEsAdmin(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<Boolean>

    @DELETE("/Usuario/eliminarUsuario/{username}")
    suspend fun eliminarUsuario(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<UsuarioDTO>

    @PUT("/Usuario/cambiarPrivacidadComunidad/{username}/{privacidad}")
    suspend fun cambiarPrivacidadComunidad(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("privacidad") privacidad: String
    ): Response<UsuarioDTO>

    @PUT("/Usuario/cambiarPrivacidadActividad/{username}/{privacidad}")
    suspend fun cambiarPrivacidadActividad(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("privacidad") privacidad: String
    ): Response<UsuarioDTO>

    @PUT("/Usuario/cambiarRadarDistancia/{username}/{radar}")
    suspend fun cambiarRadarDistancia(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("radar") radar: String
    ): Response<UsuarioDTO>

    @PUT("/Usuario/cambiarContrasena")
    suspend fun cambiarContrasena(
        @Header("Authorization") token: String,
        @Body cambiarContrasenaDTO: CambiarContrasenaDTO
    ): Response<UsuarioDTO>

    @GET("/Usuario/verPrivacidadActividad/{username}")
    suspend fun verPrivacidadActividad(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<ResponseBody>

    @GET("/Usuario/verPrivacidadComunidad/{username}")
    suspend fun verPrivacidadComunidad(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<ResponseBody>

    @GET("/Usuario/verRadarDistancia/{username}")
    suspend fun verRadarDistancia(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<ResponseBody>

    @GET("/Usuario/verSolicitudesAmistad/{username}")
    suspend fun verSolicitudesAmistad(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<SolicitudAmistadDTO>>

    @GET("/Usuario/verAmigos/{username}")
    suspend fun verAmigos(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<UsuarioDTO>>

    @POST("/Usuario/enviarSolicitudAmistad")
    suspend fun enviarSolicitudAmistad(
        @Header("Authorization") token: String,
        @Body solicitudAmistadDTO: SolicitudAmistadDTO
    ): Response<SolicitudAmistadDTO>

    @PUT("/Usuario/aceptarSolicitud/{id}")
    suspend fun aceptarSolicitud(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Boolean>

    @DELETE("/Usuario/cancelarSolicitudAmistad/{id}")
    suspend fun cancelarSolicitudAmistad(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Boolean>

    @DELETE("/Usuario/rechazarSolicitud/{id}")
    suspend fun rechazarSolicitud(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Boolean>

    @GET("/Usuario/verificarSolicitudPendiente/{remitente}/{destinatario}")
    suspend fun verificarSolicitudPendiente(
        @Header("Authorization") token: String,
        @Path("remitente") remitente: String,
        @Path("destinatario") destinatario: String
    ): Response<Boolean>

    @POST("/Usuario/bloquearUsuario")
    suspend fun bloquearUsuario(
        @Header("Authorization") token: String,
        @Body bloqueoDTO: BloqueoDTO
    ): Response<BloqueoDTO>

    @HTTP(method = "DELETE", path = "/Usuario/desbloquearUsuario", hasBody = true)
    suspend fun desbloquearUsuario(
        @Header("Authorization") token: String,
        @Body bloqueoDTO: BloqueoDTO
    ): Response<Boolean>

    @GET("/Usuario/verUsuariosBloqueados/{username}")
    suspend fun verUsuariosBloqueados(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<UsuarioDTO>>

    @GET("api/paypal/health-check")
    suspend fun healthCheck(): Response<Map<String, String>>

    @POST("api/paypal/simulate-premium-purchase")
    suspend fun simulatePremiumPurchase(
        @Header("Authorization") token: String,
        @Query("username") username: String
    ): Response<PaymentResponseDTO>

    @POST("/Comunidad/crearComunidad")
    suspend fun crearComunidad(
        @Header("Authorization") token: String,
        @Body comunidadCreateDTO: ComunidadCreateDTO
    ): Response<ComunidadDTO>

    @GET("/Comunidad/verComunidadPorUrl/{url}")
    suspend fun verComunidadPorUrl(
        @Header("Authorization") token: String,
        @Path("url") url: String
    ): Response<ComunidadDTO>

    @GET("/Comunidad/verComunidadesPorUsuarioCreador/{username}")
    suspend fun verComunidadesPorUsuarioCreador(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<ComunidadDTO>>

    @GET("/Comunidad/verTodasComunidadesPublicas")
    suspend fun verTodasComunidadesPublicas(
        @Header("Authorization") token: String
    ): Response<List<ComunidadDTO>>

    @GET("/Comunidad/verComunidadesPublicasEnZona/{username}")
    suspend fun verComunidadesPublicas(
        @Header("Authorization") token: String,
        @Path("username") username: String,
    ): Response<List<ComunidadDTO>>

    @GET("/Comunidad/verComunidadPorUsuario/{username}/{usuarioSolicitante}")
    suspend fun verComunidadPorUsuario(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("usuarioSolicitante") usuarioSolicitante: String
    ): Response<List<ComunidadDTO>>

    @POST("/Comunidad/unirseComunidad")
    suspend fun unirseComunidad(
        @Body participantesComunidadDTO: ParticipantesComunidadDTO,
        @Header("Authorization") token: String
    ): Response<ParticipantesComunidadDTO>

    @POST("/Comunidad/unirseComunidadPorCodigo/{codigo}")
    suspend fun unirseComunidadPorCodigo(
        @Body participantesComunidadDTO: ParticipantesComunidadDTO,
        @Path("codigo") codigo: String,
        @Header("Authorization") token: String
    ): Response<ParticipantesComunidadDTO>

    @HTTP(method = "DELETE", path = "/Comunidad/salirComunidad", hasBody = true)
    suspend fun salirComunidad(
        @Body participantesComunidadDTO: ParticipantesComunidadDTO,
        @Header("Authorization") token: String
    ): Response<ParticipantesComunidadDTO>

    @PUT("/Comunidad/modificarComunidad")
    suspend fun modificarComunidad(
        @Header("Authorization") token: String,
        @Body comunidadUpdateDTO: ComunidadUpdateDTO
    ): Response<ComunidadDTO>

    @DELETE("/Comunidad/eliminarComunidad/{url}")
    suspend fun eliminarComunidad(
        @Header("Authorization") token: String,
        @Path("url") url: String
    ): Response<ComunidadDTO>

    @POST("/Comunidad/booleanUsuarioApuntadoComunidad")
    suspend fun booleanUsuarioApuntadoComunidad(
        @Body participantesComunidadDTO: ParticipantesComunidadDTO,
        @Header("Authorization") token: String
    ): Response<Boolean>

    @GET("/Comunidad/contarUsuariosEnUnaComunidad/{comunidad}")
    suspend fun contarUsuariosEnUnaComunidad(
        @Header("Authorization") token: String,
        @Path("comunidad") comunidad: String
    ): Response<Int>

    @GET("/Comunidad/verificarCreadorAdministradorComunidad/{username}/{comunidadUrl}")
    suspend fun verificarCreadorAdministradorComunidad(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("comunidadUrl") comunidadUrl: String
    ): Response<Boolean>

    @PUT("/Comunidad/cambiarCreadorComunidad/{comunidadUrl}/{creadorActual}/{nuevoCreador}")
    suspend fun cambiarCreadorComunidad(
        @Header("Authorization") token: String,
        @Path("comunidadUrl") comunidadUrl: String,
        @Path("creadorActual") creadorActual: String,
        @Path("nuevoCreador") nuevoCreador: String
    ): Response<ComunidadDTO>

    @PUT("/Comunidad/expulsarUsuario/{username}/{url}/{usuarioSolicitante}")
    suspend fun expulsarUsuario(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("url") url:String,
        @Path("usuarioSolicitante") usuarioSolicitante:String
    ): Response<UsuarioDTO>

    @POST("/Actividad/crearActividad")
    suspend fun crearActividad(
        @Header("Authorization") token: String,
        @Body actividadCreateDTO: ActividadCreateDTO
    ): Response<ActividadDTO>

    @GET("/Actividad/verActividadPorId/{id}")
    suspend fun verActividadPorId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ActividadDTO>

    @GET("/Actividad/verActividadNoParticipaUsuarioFechaSuperior/{username}")
    suspend fun verActividadNoParticipaUsuarioFechaSuperior(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<ActividadDTO>>

    @GET("/Actividad/verTodasActividadesPublicasFechaSuperior")
    suspend fun verTodasActividadesPublicasFechaSuperior(
        @Header("Authorization") token: String
    ): Response<List<ActividadDTO>>

    @GET("/Actividad/verTodasActividadesPublicasCualquierFecha")
    suspend fun verTodasActividadesPublicasCualquierFecha(
        @Header("Authorization") token: String
    ): Response<List<ActividadDTO>>

    @GET("/Actividad/verActividadesPublicasEnZonaFechaSuperior/{username}")
    suspend fun verActividadesPublicasFechaSuperior(
        @Header("Authorization") token: String,
        @Path("username") username: String,
    ): Response<List<ActividadDTO>>

    @GET("/Actividad/verActividadesPorComunidadFechaSuperior/{comunidad}")
    suspend fun verActividadesPorComunidadFechaSuperior(
        @Header("Authorization") token: String,
        @Path("comunidad") comunidad: String
    ): Response<List<ActividadDTO>>

    @GET("/Actividad/verActividadesPorComunidadCualquierFecha/{comunidad}")
    suspend fun verActividadesPorComunidadCualquierFecha(
        @Header("Authorization") token: String,
        @Path("comunidad") comunidad: String
    ): Response<List<ActividadDTO>>

    @GET("/Actividad/verActividadPorUsernameFechaSuperior/{username}/{usuarioSolicitante}")
    suspend fun verActividadPorUsernameFechaSuperior(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("usuarioSolicitante") usuarioSolicitante: String
    ): Response<List<ActividadDTO>>

    @GET("/Actividad/verComunidadPorActividad/{idActividad}")
    suspend fun verComunidadPorActividad(
        @Header("Authorization") token: String,
        @Path("idActividad") idActividad: String
    ): Response<ComunidadDTO>

    @POST("/Actividad/unirseActividad")
    suspend fun unirseActividad(
        @Body participantesActividadDTO: ParticipantesActividadDTO,
        @Header("Authorization") token: String
    ): Response<ParticipantesActividadDTO>

    @HTTP(method = "DELETE", path = "/Actividad/salirActividad", hasBody = true)
    suspend fun salirActividad(
        @Body participantesActividadDTO: ParticipantesActividadDTO,
        @Header("Authorization") token: String
    ): Response<ParticipantesActividadDTO>

    @PUT("/Actividad/modificarActividad")
    suspend fun modificarActividad(
        @Header("Authorization") token: String,
        @Body actividadUpdateDTO: ActividadUpdateDTO
    ): Response<ActividadDTO>

    @DELETE("/Actividad/eliminarActividad/{id}")
    suspend fun eliminarActividad(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ActividadDTO>

    @POST("/Actividad/booleanUsuarioApuntadoActividad")
    suspend fun booleanUsuarioApuntadoActividad(
        @Body participantesActividadDTO: ParticipantesActividadDTO,
        @Header("Authorization") token: String
    ): Response<Boolean>

    @GET("/Actividad/contarUsuariosEnUnaActividad/{actividadId}")
    suspend fun contarUsuariosEnUnaActividad(
        @Header("Authorization") token: String,
        @Path("actividadId") actividadId: String
    ): Response<Int>

    @POST("/Denuncia/crearDenuncia")
    suspend fun crearDenuncia(
        @Header("Authorization") token: String,
        @Body denunciaCreateDTO: DenunciaCreateDTO
    ): Response<DenunciaDTO>

    @GET("/Denuncia/verTodasLasDenuncias")
    suspend fun verTodasLasDenuncias(
        @Header("Authorization") token: String
    ): Response<List<DenunciaDTO>>

    @GET("/Denuncia/verDenunciasNoCompletadas")
    suspend fun verDenunciasNoCompletadas(
        @Header("Authorization") token: String
    ): Response<List<DenunciaDTO>>

    @PUT("/Denuncia/completarDenuncia/{denunciaId}/{completado}")
    suspend fun completarDenuncia(
        @Header("Authorization") token: String,
        @Path("denunciaId") denunciaId: String,
        @Path("completado") completado: Boolean
    ): Response<DenunciaDTO>

    @POST("/Chat/enviarMensaje")
    suspend fun enviarMensaje(
        @Header("Authorization") token: String,
        @Body mensajeCreateDTO: MensajeCreateDTO
    ): Response<MensajeDTO>

    @GET("/Chat/obtenerMensajes/{comunidadUrl}")
    suspend fun obtenerMensajesComunidad(
        @Header("Authorization") token: String,
        @Path("comunidadUrl") comunidadUrl: String
    ): Response<List<MensajeDTO>>

    object RetrofitServiceFactory {

        val retrofit: Retrofit by lazy {
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                .create()

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            Retrofit.Builder()
                .baseUrl(BuildConfig.URL_API)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        fun makeRetrofitService(): RetrofitService {
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                .create()

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.URL_API)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(RetrofitService::class.java)
        }
    }
}