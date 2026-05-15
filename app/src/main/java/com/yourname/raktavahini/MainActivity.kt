package com.yourname.raktavahini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yourname.raktavahini.ui.AppNavigation
import com.yourname.raktavahini.ui.theme.RaktaVahiniTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RaktaVahiniTheme {
                AppNavigation()
            }
        }
    }
}
