package com.example.socialme_interfazgrafica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialme_interfazgrafica.navigation.AppNavigation
import com.example.socialme_interfazgrafica.ui.theme.SocialMeInterfazGraficaTheme
import com.example.socialme_interfazgrafica.viewModel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialMeInterfazGraficaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Box(Modifier.padding(innerPadding)){
                    AppNavigation(viewModel())
                }
                }
            }
        }
    }
}
