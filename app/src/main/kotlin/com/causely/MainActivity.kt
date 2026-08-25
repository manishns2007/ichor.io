package com.causely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.causely.navigation.CauselyNavGraph
import com.causely.ui.theme.BackgroundDeep
import com.causely.ui.theme.CauselyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CauselyTheme {
                val navController = rememberNavController()
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDeep)
                ) {
                    CauselyNavGraph(navController = navController)
                }
            }
        }
    }
}
