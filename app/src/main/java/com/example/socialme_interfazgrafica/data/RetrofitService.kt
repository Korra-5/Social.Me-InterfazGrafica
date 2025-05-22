package com.example.socialme_interfazgrafica.data

import com.example.socialme_interfazgrafica.model.ActividadCreateDTO
import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.ActividadUpdateDTO
import com.example.socialme_interfazgrafica.model.BloqueoDTO
import com.example.socialme_interfazgrafica.model.ComunidadCreateDTO
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.ComunidadUpdateDTO
import com.example.socialme_interfazgrafica.model.ConfirmacionRegistroDTO
import com.example.socialme_interfazgrafica.model.ConfirmarCambioEmailDTO
import com.example.socialme_interfazgrafica.model.DenunciaCreateDTO
import com.example.socialme_interfazgrafica.model.LoginResponse
import com.example.socialme_interfazgrafica.model.MensajeCreateDTO
import com.example.socialme_interfazgrafica.model.MensajeDTO
import com.example.socialme_interfazgrafica.model.NotificacionDTO
import com.example.socialme_interfazgrafica.model.ParticipantesActividadDTO
import com.example.socialme_interfazgrafica.model.ParticipantesComunidadDTO
import com.example.socialme_interfazgrafica.model.PaymentVerificationRequest
import com.example.socialme_interfazgrafica.model.RegistroResponse
import com.example.socialme_interfazgrafica.model.SolicitudAmistadDTO
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
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface RetrofitService {

    // ==================== USUARIO ====================
    @POST("/Usuario/register")
    suspend fun insertUser(
        @Body usuario: UsuarioRegisterDTO
    ): Response<RegistroResponse>

    @POST("/Usuario/login")
    suspend fun loginUser(
        @Body usuario: UsuarioLoginDTO
    ): Response<LoginResponse>

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

    @PUT("/Usuario/modificarUsuario")
    suspend fun modificarUsuario(
        @Header("Authorization") token: String,
        @Body usuarioUpdateDTO: UsuarioUpdateDTO
    ): Response<UsuarioDTO> // CORREGIDO: Devuelve UsuarioDTO, no ActividadCreateDTO

    @DELETE("/Usuario/eliminarUsuario/{username}")
    suspend fun eliminarUsuario(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<UsuarioDTO>

    @PUT("/Usuario/actualizarPremium/{username}")
    suspend fun actualizarPremium(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<UsuarioDTO>

    @POST("/Usuario/verificarPremium")
    suspend fun verificarPremium(
        @Header("Authorization") token: String,
        @Body paymentData: PaymentVerificationRequest
    ): Response<Map<String, Any>>

    @POST("/Usuario/verificarCodigo")
    suspend fun verificarCodigo(
        @Body verificacionDTO: VerificacionDTO
    ): Response<Boolean>

    @GET("/Usuario/reenviarCodigo/{email}")
    suspend fun reenviarCodigo(
        @Path("email") email: String
    ): Response<Boolean>

    // AMISTADES
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

    @DELETE("/Usuario/rechazarSolicitud/{id}") // AÑADIDO: Faltaba este endpoint
    suspend fun rechazarSolicitud(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Boolean>

    @GET("/Usuario/verificarSolicitudPendiente/{remitente}/{destinatario}") // AÑADIDO: Faltaba este endpoint
    suspend fun verificarSolicitudPendiente(
        @Header("Authorization") token: String,
        @Path("remitente") remitente: String,
        @Path("destinatario") destinatario: String
    ): Response<Boolean>

    // BLOQUEOS
    @POST("/Usuario/bloquearUsuario")
    suspend fun bloquearUsuario(
        @Header("Authorization") token: String,
        @Body bloqueoDTO: BloqueoDTO
    ): Response<BloqueoDTO>

    @HTTP(method = "DELETE", path = "/Usuario/desbloquearUsuario", hasBody = true) // CORREGIDO: Faltaba el "/"
    suspend fun desbloquearUsuario(
        @Header("Authorization") token: String,
        @Body bloqueoDTO: BloqueoDTO
    ): Response<Boolean>

    @GET("/Usuario/verUsuariosBloqueados/{username}")
    suspend fun verUsuariosBloqueados(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<UsuarioDTO>>

    @GET("/Usuario/existeBloqueo/{usuario1}/{usuario2}") // AÑADIDO: Faltaba este endpoint
    suspend fun existeBloqueo(
        @Header("Authorization") token: String,
        @Path("usuario1") usuario1: String,
        @Path("usuario2") usuario2: String
    ): Response<Boolean>

    // ==================== COMUNIDAD ====================
    @POST("/Comunidad/crearComunidad")
    suspend fun crearComunidad(
        @Header("Authorization") token: String,
        @Body comunidadCreateDTO: ComunidadCreateDTO
    ): Response<ComunidadDTO>

    @GET("/Comunidad/verComunidadPorUsuario/{username}")
    suspend fun verComunidadPorUsuario(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<ComunidadDTO>>

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

    @GET("/Comunidad/verComunidadesPublicasEnZona/{distanciaKm}/{username}")
    suspend fun verComunidadesPublicas(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("distanciaKm") distanciaKm: Float
    ): Response<List<ComunidadDTO>>

    @POST("/Comunidad/unirseComunidad")
    suspend fun unirseComunidad(
        @Body participantesComunidadDTO: ParticipantesComunidadDTO,
        @Header("Authorization") token: String
    ): Response<RegistroResponse>

    @POST("/Comunidad/unirseComunidadPorCodigo/{codigo}")
    suspend fun unirseComunidadPorCodigo(
        @Body participantesComunidadDTO: ParticipantesComunidadDTO,
        @Path("codigo") codigo: String,
        @Header("Authorization") token: String
    ): Response<RegistroResponse>

    @HTTP(method = "DELETE", path = "/Comunidad/salirComunidad", hasBody = true)
    suspend fun salirComunidad(
        @Body participantesComunidadDTO: ParticipantesComunidadDTO,
        @Header("Authorization") token: String
    ): Response<RegistroResponse>

    @PUT("/Comunidad/modificarComunidad")
    suspend fun modificarComunidad(
        @Header("Authorization") token: String,
        @Body comunidadUpdateDTO: ComunidadUpdateDTO
    ): Response<ComunidadDTO>

    @DELETE("/Comunidad/eliminarComunidad/{url}")
    suspend fun eliminarComunidad(
        @Header("Authorization") token: String,
        @Path("url") url: String
    ): Response<ComunidadDTO> // CORREGIDO: Devuelve ComunidadDTO, no ActividadCreateDTO

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

    @DELETE("/Comunidad/eliminarUsuarioDeComunidad/{usuarioSolicitante}")
    suspend fun eliminarUsuarioDeComunidad(
        @Header("Authorization") token: String,
        @Body participantesComunidadDTO: ParticipantesComunidadDTO,
        @Path("usuarioSolicitante") usuarioSolicitante: String
    ): Response<ParticipantesComunidadDTO>

    // NUEVO ENDPOINT CORREGIDO
    @PUT("/Comunidad/cambiarCreadorComunidad/{comunidadUrl}/{creadorActual}/{nuevoCreador}")
    suspend fun cambiarCreadorComunidad(
        @Header("Authorization") token: String,
        @Path("comunidadUrl") comunidadUrl: String,
        @Path("creadorActual") creadorActual: String,
        @Path("nuevoCreador") nuevoCreador: String
    ): Response<ComunidadDTO>

    // ==================== ACTIVIDAD ====================
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

    @GET("/Actividad/verActividadPorUsername/{username}")
    suspend fun verActividadPorUsername(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<ActividadDTO>>

    @GET("/Actividad/verActividadNoParticipaUsuario/{username}")
    suspend fun verActividadNoParticipaUsuario(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<ActividadDTO>>

    @GET("/Actividad/verTodasActividadesPublicas")
    suspend fun verTodasActividadesPublicas(
        @Header("Authorization") token: String
    ): Response<List<ActividadDTO>>

    @GET("/Actividad/verActividadesPublicasEnZona/{distanciaKm}/{username}")
    suspend fun verActividadesPublicas(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("distanciaKm") distanciaKm: Float
    ): Response<List<ActividadDTO>>

    @GET("/Actividad/verActividadesPorComunidad/{comunidad}")
    suspend fun verActividadesPorComunidad(
        @Header("Authorization") token: String,
        @Path("comunidad") comunidad: String
    ): Response<List<ActividadDTO>>

    @POST("/Actividad/unirseActividad")
    suspend fun unirseActividad(
        @Body participantesActividadDTO: ParticipantesActividadDTO,
        @Header("Authorization") token: String
    ): Response<RegistroResponse>

    @HTTP(method = "DELETE", path = "/Actividad/salirActividad", hasBody = true)
    suspend fun salirActividad(
        @Body participantesActividadDTO: ParticipantesActividadDTO,
        @Header("Authorization") token: String
    ): Response<RegistroResponse>

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

    @GET("/Actividad/verificarCreadorAdministradorActividad/{username}/{idActividad}")
    suspend fun verificarCreadorAdministradorActividad(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("idActividad") idActividad: String // CORREGIDO: era "idActvidad" (typo)
    ): Response<Boolean>

    // ==================== DENUNCIA ====================
    @POST("/Denuncia/crearDenuncia")
    suspend fun crearDenuncia(
        @Header("Authorization") token: String,
        @Body denunciaCreateDTO: DenunciaCreateDTO
    ): Response<DenunciaCreateDTO>

    // ==================== NOTIFICACIONES ====================
    @GET("/Notificacion/obtenerNotificaciones/{username}")
    suspend fun obtenerNotificaciones(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<NotificacionDTO>>

    @GET("/Notificacion/contarNoLeidas/{username}")
    suspend fun contarNoLeidas(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<Long>

    @PUT("/Notificacion/marcarComoLeida/{notificacionId}")
    suspend fun marcarComoLeida(
        @Header("Authorization") token: String,
        @Path("notificacionId") notificacionId: String
    ): Response<NotificacionDTO>

    // ==================== CHAT ====================
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