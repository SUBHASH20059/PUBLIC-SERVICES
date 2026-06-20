package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.RegistryDatabase
import com.example.data.RegistryRepository
import com.example.ui.RegistryMainScreen
import com.example.ui.RegistryViewModel
import com.example.ui.RegistryViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Core secure Room Database initialization
    val database = Room.databaseBuilder(
      applicationContext,
      RegistryDatabase::class.java, "constitutional_registry_db"
    ).fallbackToDestructiveMigration().build()
    
    val repository = RegistryRepository(database.registryDao())
    val factory = RegistryViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, factory)[RegistryViewModel::class.java]
    
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        RegistryMainScreen(viewModel = viewModel)
      }
    }
  }
}
