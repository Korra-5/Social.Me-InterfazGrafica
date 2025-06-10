package com.example.socialme_interfazgrafica

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import com.example.socialme_interfazgrafica.screens.InicioSesionScreen
import org.junit.Test
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.Coordenadas
import com.example.socialme_interfazgrafica.screens.ActividadDetalleScreen
import com.example.socialme_interfazgrafica.screens.ComunidadDetalleScreen
import com.example.socialme_interfazgrafica.screens.CrearComunidadScreen
import com.example.socialme_interfazgrafica.screens.UsuarioDetallesScreen
import com.example.socialme_interfazgrafica.viewModel.UserViewModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Rule
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
            codigoUnion = "TEST123"
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

        // Verificar que los elementos de la UI se muestran correctamente
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
            codigoUnion = "TEST123"
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