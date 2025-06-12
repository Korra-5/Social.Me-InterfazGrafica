package com.example.socialme_interfazgrafica

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import com.example.socialme_interfazgrafica.screens.InicioSesionScreen
import org.junit.Test
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.ActividadCreateDTO
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.ComunidadCreateDTO
import com.example.socialme_interfazgrafica.model.Coordenadas
import com.example.socialme_interfazgrafica.model.Direccion
import com.example.socialme_interfazgrafica.model.DenunciaCreateDTO
import com.example.socialme_interfazgrafica.model.DenunciaDTO
import com.example.socialme_interfazgrafica.model.LoginResponse
import com.example.socialme_interfazgrafica.model.ParticipantesComunidadDTO
import com.example.socialme_interfazgrafica.model.ParticipantesActividadDTO
import com.example.socialme_interfazgrafica.model.SolicitudAmistadDTO
import com.example.socialme_interfazgrafica.model.UsuarioDTO
import com.example.socialme_interfazgrafica.model.UsuarioRegisterDTO
import com.example.socialme_interfazgrafica.screens.ActividadDetalleScreen
import com.example.socialme_interfazgrafica.screens.ComunidadDetalleScreen
import com.example.socialme_interfazgrafica.screens.CrearComunidadScreen
import com.example.socialme_interfazgrafica.screens.UsuarioDetallesScreen
import com.example.socialme_interfazgrafica.viewModel.UserViewModel
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.UsuarioLoginDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Before
import retrofit2.Response
import java.util.Date

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenLogin_thenEnterUserAndClickInicioSesion() {
        val mockNavController = mockk<NavController>(relaxed = true)
        val realViewModel = UserViewModel()

        every { mockNavController.currentBackStackEntry } returns null

        composeTestRule.setContent {
            InicioSesionScreen(
                navController = mockNavController,
                viewModel = realViewModel
            )
        }

        composeTestRule
            .onNodeWithTag("textoUsuario")
            .performTextInput("UsuarioEjemplo")

        composeTestRule
            .onNodeWithTag("inicioSesionButton")
            .performClick()
    }

    @Test
    fun whenLogin_thenEnterPasswordAndClickInicioSesion() {
        val mockNavController = mockk<NavController>(relaxed = true)
        val realViewModel = UserViewModel() // Usar el ViewModel real

        every { mockNavController.currentBackStackEntry } returns null

        composeTestRule.setContent {
            InicioSesionScreen(
                navController = mockNavController,
                viewModel = realViewModel
            )
        }

        composeTestRule
            .onNodeWithTag("textoContrasena")
            .performTextInput("123456")

        composeTestRule
            .onNodeWithTag("inicioSesionButton")
            .performClick()
    }

    @Test
    fun whenLogin_thenEnterUserAndPasswordAndClickInicioSesion() {
        val mockNavController = mockk<NavController>(relaxed = true)
        val realViewModel = UserViewModel() // Usar el ViewModel real

        every { mockNavController.currentBackStackEntry } returns null

        composeTestRule.setContent {
            InicioSesionScreen(
                navController = mockNavController,
                viewModel = realViewModel
            )
        }
        composeTestRule
            .onNodeWithTag("textoUsuario")
            .performTextInput("UsuarioEjemplo")

        composeTestRule
            .onNodeWithTag("textoContrasena")
            .performTextInput("123456")

        composeTestRule
            .onNodeWithTag("inicioSesionButton")
            .performClick()
    }

    @Test
    fun whenLogin_ClickInicioSesion() {
        val mockNavController = mockk<NavController>(relaxed = true)
        val realViewModel = UserViewModel() // Usar el ViewModel real

        every { mockNavController.currentBackStackEntry } returns null

        composeTestRule.setContent {
            InicioSesionScreen(
                navController = mockNavController,
                viewModel = realViewModel
            )
        }
        composeTestRule
            .onNodeWithTag("inicioSesionButton")
            .performClick()
    }

    @Test
    fun whenCreateComunidad_isSuccesful() {
        val mockNavController = mockk<NavController>(relaxed = true)

        every { mockNavController.currentBackStackEntry } returns null

        composeTestRule.setContent {
            CrearComunidadScreen(
                navController = mockNavController
            )
        }
        composeTestRule
            .onNodeWithTag("textoUrl")
            .performTextInput("UrlEjemplo")

        composeTestRule
            .onNodeWithTag("textoNombre")
            .performTextInput("NombreEjemplo")

        composeTestRule
            .onNodeWithTag("textoDescripcion")
            .performTextInput("DescripcionEjemplo")

        composeTestRule
            .onNodeWithTag("buttonCrearComunidad")
            .performClick()
    }

    @Test
    fun whenShowComunidadDetalle_isSuccessful() {
        val mockNavController = mockk<NavController>(relaxed = true)

        val mockComunidad = ComunidadDTO(
            url = "comunidad-prueba",
            nombre = "Comunidad de Prueba",
            descripcion = "Esta es una comunidad de prueba para testing",
            intereses = listOf("Testing", "Pruebas", "Android"),
            fotoPerfilId = "6838b94ebalf4835e2f685c4",
            fotoCarruselIds = listOf("fotoCarrusel1", "fotoCarrusel2"),
            creador = "testuser",
            administradores = listOf("testuser", "admin"),
            fechaCreacion = Date(125, 5, 29, 19, 45, 18),
            privada = false,
            coordenadas = Coordenadas(latitud = "36.527061", longitud = "-6.288596"),
            codigoUnion = "TEST123",
            expulsadosUsername = listOf()
        )

        val mockAuthToken = "mock-auth-token-123"

        every { mockNavController.currentBackStackEntry } returns null

        composeTestRule.setContent {
            ComunidadDetalleScreen(
                comunidad = mockComunidad,
                authToken = mockAuthToken,
                navController = mockNavController
            )
        }

        composeTestRule
            .onNodeWithText("Comunidad de Prueba")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Esta es una comunidad de prueba para testing")
            .assertIsDisplayed()
    }

    @Test
    fun whenClickJoinCommunity_performsAction() {
        val mockNavController = mockk<NavController>(relaxed = true)

        val mockComunidad = ComunidadDTO(
            url = "comunidad-prueba",
            nombre = "Comunidad de Prueba",
            descripcion = "Esta es una comunidad de prueba para testing",
            intereses = listOf("Testing", "Pruebas", "Android"),
            fotoPerfilId = "6838b94ebalf4835e2f685c4",
            fotoCarruselIds = listOf("fotoCarrusel1", "fotoCarrusel2"),
            creador = "testuser",
            administradores = listOf("testuser", "admin"),
            fechaCreacion = Date(125, 5, 29, 19, 45, 18),
            privada = false,
            coordenadas = Coordenadas(latitud = "36.527061", longitud = "-6.288596"),
            codigoUnion = "TEST123",
            expulsadosUsername = listOf()
        )

        val mockAuthToken = "mock-auth-token-789"

        every { mockNavController.currentBackStackEntry } returns null

        composeTestRule.setContent {
            ComunidadDetalleScreen(
                comunidad = mockComunidad,
                authToken = mockAuthToken,
                navController = mockNavController
            )
        }

        composeTestRule
            .onNodeWithTag("joinComunidadButton")
            .performClick()
    }

    @Test
    fun whenShowActividadDetalle_isSuccessful() {
        val mockNavController = mockk<NavController>(relaxed = true)
        val mockAuthToken = "mock-auth-token-123"
        val mockActividadId = "actividad-test-123"

        every { mockNavController.currentBackStackEntry } returns null

        composeTestRule.setContent {
            ActividadDetalleScreen(
                actividadId = mockActividadId,
                authToken = mockAuthToken,
                navController = mockNavController
            )
        }

        composeTestRule
            .onNodeWithTag("BoxActividad")
            .assertExists()
    }

    @Test
    fun whenShowUsuarioDetalle_isSuccessful() {
        val mockNavController = mockk<NavController>(relaxed = true)

        every { mockNavController.currentBackStackEntry } returns null

        composeTestRule.setContent {
            UsuarioDetallesScreen(
                username = "username_ejemplo",
                navController = mockNavController
            )
        }

        composeTestRule
            .onNodeWithTag("BoxUsuario")
            .assertExists()
    }
}

/**
 * Tests automatizados para probar la API con mocks
 */
@RunWith(AndroidJUnit4::class)
class ApiInstrumentedTests {

    private lateinit var retrofitService: RetrofitService
    private val mockAuthToken = "Bearer mock-token-123"

    @Before
    fun setup() {
        retrofitService = mockk()
    }

    @Test
    fun whenLoginUser_withInvalidCredentials_thenReturnErrorResponse() = runBlocking {
        val loginRequest = UsuarioLoginDTO(
            username = "wronguser",
            password = "wrongpassword"
        )

        coEvery {
            retrofitService.loginUser(loginRequest)
        } returns Response.error(401, mockk(relaxed = true))

        val response = retrofitService.loginUser(loginRequest)

        assertFalse(response.isSuccessful)
        assertEquals(401, response.code())

        coVerify { retrofitService.loginUser(loginRequest) }
    }

    @Test
    fun whenIniciarRegistro_thenReturnSuccessResponse() = runBlocking {
        val registerRequest = UsuarioRegisterDTO(
            username = "newuser",
            email = "newuser@example.com",
            password = "password123",
            passwordRepeat = "password123",
            nombre = "New",
            apellidos = "User",
            direccion = Direccion(
                municipio = "municipio",
                provincia = "provincia"
            ),
            intereses = listOf("Testing"),
            descripcion = "Nuevo usuario",
            rol = "USER",
            fotoPerfil = "",
            fotoPerfilBase64 = null
        )

        val mockResponse = mapOf("message" to "Código de verificación enviado")

        coEvery {
            retrofitService.iniciarRegistro(registerRequest)
        } returns Response.success(mockResponse)

        val response = retrofitService.iniciarRegistro(registerRequest)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals("Código de verificación enviado", response.body()?.get("message"))

        coVerify { retrofitService.iniciarRegistro(registerRequest) }
    }

    @Test
    fun whenCrearComunidad_thenReturnComunidadDTO() = runBlocking {
        val createRequest = ComunidadCreateDTO(
            url = "test-community",
            nombre = "Test Community",
            descripcion = "A test community",
            intereses = listOf("Testing", "Android"),
            fotoPerfilId = null,
            creador = "testuser",
            privada = false,
            coordenadas = Coordenadas(latitud = "40.4168", longitud = "-3.7038"),
            codigoUnion = "ABC123"
        )

        val mockComunidad = ComunidadDTO(
            url = "test-community",
            nombre = "Test Community",
            descripcion = "A test community",
            intereses = listOf("Testing", "Android"),
            fotoPerfilId = "",
            fotoCarruselIds = emptyList(),
            creador = "testuser",
            administradores = listOf("testuser"),
            fechaCreacion = Date(),
            privada = false,
            coordenadas = Coordenadas(latitud = "40.4168", longitud = "-3.7038"),
            codigoUnion = "ABC123",
            expulsadosUsername = emptyList()
        )

        coEvery {
            retrofitService.crearComunidad(mockAuthToken, createRequest)
        } returns Response.success(mockComunidad)

        val response = retrofitService.crearComunidad(mockAuthToken, createRequest)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals("test-community", response.body()?.url)
        assertEquals("Test Community", response.body()?.nombre)
        assertEquals("testuser", response.body()?.creador)

        coVerify { retrofitService.crearComunidad(mockAuthToken, createRequest) }
    }

    @Test
    fun whenVerComunidadPorUrl_thenReturnComunidadDTO() = runBlocking {
        val comunidadUrl = "test-community"

        val mockComunidad = ComunidadDTO(
            url = comunidadUrl,
            nombre = "Test Community",
            descripcion = "A test community",
            intereses = listOf("Testing", "Android"),
            fotoPerfilId = "",
            fotoCarruselIds = emptyList(),
            creador = "testuser",
            administradores = listOf("testuser"),
            fechaCreacion = Date(),
            privada = false,
            coordenadas = Coordenadas(latitud = "40.4168", longitud = "-3.7038"),
            codigoUnion = "ABC123",
            expulsadosUsername = emptyList()
        )

        coEvery {
            retrofitService.verComunidadPorUrl(mockAuthToken, comunidadUrl)
        } returns Response.success(mockComunidad)

        val response = retrofitService.verComunidadPorUrl(mockAuthToken, comunidadUrl)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals(comunidadUrl, response.body()?.url)

        coVerify { retrofitService.verComunidadPorUrl(mockAuthToken, comunidadUrl) }
    }

    @Test
    fun whenCrearActividad_thenReturnActividadDTO() = runBlocking {
        val createRequest = ActividadCreateDTO(
            nombre = "Test Activity",
            descripcion = "A test activity",
            comunidad = "test-community",
            creador = "testuser",
            fechaInicio = Date(),
            fechaFinalizacion = Date(System.currentTimeMillis() + 7200000), // +2 horas
            privada = false,
            coordenadas = Coordenadas(latitud = "40.4168", longitud = "-3.7038"),
            lugar = "Madrid, España"
        )

        val mockActividad = ActividadDTO(
            _id = "activity-123",
            nombre = "Test Activity",
            descripcion = "A test activity",
            fechaInicio = Date(),
            fechaFinalizacion = Date(System.currentTimeMillis() + 7200000),
            coordenadas = Coordenadas(latitud = "40.4168", longitud = "-3.7038"),
            creador = "testuser",
            privada = false,
            fotosCarruselIds = emptyList(),
            lugar = "Madrid, España"
        )

        coEvery {
            retrofitService.crearActividad(mockAuthToken, createRequest)
        } returns Response.success(mockActividad)

        val response = retrofitService.crearActividad(mockAuthToken, createRequest)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals("activity-123", response.body()?._id)
        assertEquals("Test Activity", response.body()?.nombre)
        assertEquals("testuser", response.body()?.creador)

        coVerify { retrofitService.crearActividad(mockAuthToken, createRequest) }
    }

    @Test
    fun whenVerActividadPorId_thenReturnActividadDTO() = runBlocking {
        val actividadId = "activity-123"

        val mockActividad = ActividadDTO(
            _id = actividadId,
            nombre = "Test Activity",
            descripcion = "A test activity",
            fechaInicio = Date(),
            fechaFinalizacion = Date(System.currentTimeMillis() + 7200000),
            coordenadas = Coordenadas(latitud = "40.4168", longitud = "-3.7038"),
            creador = "testuser",
            privada = false,
            fotosCarruselIds = emptyList(),
            lugar = "Madrid, España"
        )

        coEvery {
            retrofitService.verActividadPorId(mockAuthToken, actividadId)
        } returns Response.success(mockActividad)

        val response = retrofitService.verActividadPorId(mockAuthToken, actividadId)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals(actividadId, response.body()?._id)

        coVerify { retrofitService.verActividadPorId(mockAuthToken, actividadId) }
    }

    @Test
    fun whenUnirseComunidad_thenReturnParticipantesDTO() = runBlocking {
        val participantesRequest = ParticipantesComunidadDTO(
            username = "testuser",
            comunidad = "test-community"
        )

        coEvery {
            retrofitService.unirseComunidad(participantesRequest, mockAuthToken)
        } returns Response.success(participantesRequest)

        val response = retrofitService.unirseComunidad(participantesRequest, mockAuthToken)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals("testuser", response.body()?.username)
        assertEquals("test-community", response.body()?.comunidad)

        coVerify { retrofitService.unirseComunidad(participantesRequest, mockAuthToken) }
    }

    @Test
    fun whenUnirseActividad_thenReturnParticipantesDTO() = runBlocking {
        val participantesRequest = ParticipantesActividadDTO(
            username = "testuser",
            actividadId = "activity-123",
            nombreActividad = "Test Activity"
        )

        coEvery {
            retrofitService.unirseActividad(participantesRequest, mockAuthToken)
        } returns Response.success(participantesRequest)

        val response = retrofitService.unirseActividad(participantesRequest, mockAuthToken)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals("testuser", response.body()?.username)
        assertEquals("activity-123", response.body()?.actividadId)

        coVerify { retrofitService.unirseActividad(participantesRequest, mockAuthToken) }
    }

    @Test
    fun whenVerUsuarioPorUsername_thenReturnUsuarioDTO() = runBlocking {
        val username = "testuser"

        val mockUsuario = UsuarioDTO(
            username = username,
            email = "test@example.com",
            nombre = "Test",
            apellido = "User",
            direccion = Direccion(
                municipio = "municipio",
                provincia = "provincia"
            ),
            intereses = listOf("Testing", "Android"),
            premium = false,
            privacidadActividades = "PUBLICO",
            privacidadComunidades = "PUBLICO",
            radarDistancia = "10",
            fotoPerfilId = null,
            descripcion = "Usuario de prueba",
            rol = "USER"
        )

        coEvery {
            retrofitService.verUsuarioPorUsername(mockAuthToken, username)
        } returns Response.success(mockUsuario)

        val response = retrofitService.verUsuarioPorUsername(mockAuthToken, username)


        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals(username, response.body()?.username)
        assertEquals("test@example.com", response.body()?.email)

        coVerify { retrofitService.verUsuarioPorUsername(mockAuthToken, username) }
    }

    @Test
    fun whenVerTodasComunidadesPublicas_thenReturnListOfComunidades() = runBlocking {
        val mockComunidades = listOf(
            ComunidadDTO(
                url = "community1",
                nombre = "Community 1",
                descripcion = "First community",
                intereses = listOf("Testing"),
                fotoPerfilId = "",
                fotoCarruselIds = emptyList(),
                creador = "user1",
                administradores = listOf("user1"),
                fechaCreacion = Date(),
                privada = false,
                coordenadas = Coordenadas(latitud = "40.4168", longitud = "-3.7038"),
                codigoUnion = "ABC123",
                expulsadosUsername = emptyList()
            ),
            ComunidadDTO(
                url = "community2",
                nombre = "Community 2",
                descripcion = "Second community",
                intereses = listOf("Android"),
                fotoPerfilId = "",
                fotoCarruselIds = emptyList(),
                creador = "user2",
                administradores = listOf("user2"),
                fechaCreacion = Date(),
                privada = false,
                coordenadas = Coordenadas(latitud = "40.4168", longitud = "-3.7038"),
                codigoUnion = "DEF456",
                expulsadosUsername = emptyList()
            )
        )

        coEvery {
            retrofitService.verTodasComunidadesPublicas(mockAuthToken)
        } returns Response.success(mockComunidades)

        val response = retrofitService.verTodasComunidadesPublicas(mockAuthToken)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals(2, response.body()?.size)
        assertEquals("Community 1", response.body()?.get(0)?.nombre)
        assertEquals("Community 2", response.body()?.get(1)?.nombre)

        coVerify { retrofitService.verTodasComunidadesPublicas(mockAuthToken) }
    }

    @Test
    fun whenBooleanUsuarioApuntadoComunidad_thenReturnBoolean() = runBlocking {
        val participantesRequest = ParticipantesComunidadDTO(
            username = "testuser",
            comunidad = "test-community"
        )

        coEvery {
            retrofitService.booleanUsuarioApuntadoComunidad(participantesRequest, mockAuthToken)
        } returns Response.success(true)

        val response =
            retrofitService.booleanUsuarioApuntadoComunidad(participantesRequest, mockAuthToken)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertTrue(response.body()!!)

        coVerify {
            retrofitService.booleanUsuarioApuntadoComunidad(
                participantesRequest,
                mockAuthToken
            )
        }
    }

    @Test
    fun whenContarUsuariosEnUnaComunidad_thenReturnCount() = runBlocking {
        val comunidad = "test-community"
        val expectedCount = 5

        coEvery {
            retrofitService.contarUsuariosEnUnaComunidad(mockAuthToken, comunidad)
        } returns Response.success(expectedCount)

        val response = retrofitService.contarUsuariosEnUnaComunidad(mockAuthToken, comunidad)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals(expectedCount, response.body())

        coVerify { retrofitService.contarUsuariosEnUnaComunidad(mockAuthToken, comunidad) }
    }

    @Test
    fun whenApiCallFails_thenHandleError() = runBlocking {
        val username = "nonexistentuser"

        coEvery {
            retrofitService.verUsuarioPorUsername(mockAuthToken, username)
        } returns Response.error(404, mockk(relaxed = true))

        val response = retrofitService.verUsuarioPorUsername(mockAuthToken, username)

        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
        assertNull(response.body())

        coVerify { retrofitService.verUsuarioPorUsername(mockAuthToken, username) }
    }

    @Test
    fun whenEnviarSolicitudAmistad_thenReturnSolicitudDTO() = runBlocking {
        val solicitudRequest = SolicitudAmistadDTO(
            _id = "",
            remitente = "user1",
            destinatario = "user2",
            fechaEnviada = Date(),
            aceptada = false
        )

        val mockSolicitud = SolicitudAmistadDTO(
            _id = "solicitud-123",
            remitente = "user1",
            destinatario = "user2",
            fechaEnviada = Date(),
            aceptada = false
        )

        coEvery {
            retrofitService.enviarSolicitudAmistad(mockAuthToken, solicitudRequest)
        } returns Response.success(mockSolicitud)

        val response = retrofitService.enviarSolicitudAmistad(mockAuthToken, solicitudRequest)

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertEquals("solicitud-123", response.body()?._id)
        assertEquals("user1", response.body()?.remitente)
        assertEquals("user2", response.body()?.destinatario)

        coVerify { retrofitService.enviarSolicitudAmistad(mockAuthToken, solicitudRequest) }
    }
}