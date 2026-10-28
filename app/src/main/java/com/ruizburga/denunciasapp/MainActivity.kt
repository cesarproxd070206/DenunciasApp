package com.ruizburga.denunciasapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ruizburga.denunciasapp.ui.DenunciasApp
import com.ruizburga.denunciasapp.ui.theme.DenunciasAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DenunciasAppTheme {
                DenunciasApp()
            }
        }
    }
}
