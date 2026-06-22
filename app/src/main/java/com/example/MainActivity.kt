package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.RegistryDatabase
import com.example.data.RegistryRepository
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = Room.databaseBuilder(
            applicationContext,
            RegistryDatabase::class.java, "constitutional_registry_db"
        ).fallbackToDestructiveMigration().build()
        
        val repository = RegistryRepository(database.registryDao())
        
        val registryFactory = RegistryViewModelFactory(repository)
        val registryViewModel = ViewModelProvider(this, registryFactory)[RegistryViewModel::class.java]
        val securityViewModel = SecurityViewModel(repository)
        val businessViewModel = BusinessViewModel(repository)
        val gstViewModel = GstViewModel(repository)
        val civilRegistryViewModel = CivilRegistryViewModel(repository)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val employee by securityViewModel.currentEmployee.collectAsState()
                
                if (employee == null) {
                    EmployeeLoginScreen(viewModel = securityViewModel)
                } else {
                    RegistryMainScreen(
                        viewModel = registryViewModel,
                        securityViewModel = securityViewModel,
                        businessViewModel = businessViewModel,
                        gstViewModel = gstViewModel,
                        civilRegistryViewModel = civilRegistryViewModel
                    )
                }
            }
        }
    }
}
