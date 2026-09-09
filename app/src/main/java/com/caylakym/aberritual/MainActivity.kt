package com.caylakym.aberritual

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.caylakym.aberritual.data.repository.WallpaperRepositoryImpl
import com.caylakym.aberritual.ui.creator.StudioScreen
import com.caylakym.aberritual.ui.creator.StudioViewModel
import com.caylakym.aberritual.ui.theme.AberritualTheme

class MainActivity : ComponentActivity() {

    private val viewModel: StudioViewModel by viewModels {
        StudioViewModel.Factory(WallpaperRepositoryImpl(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AberritualTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StudioScreen(viewModel = viewModel)
                }
            }
        }
    }
}