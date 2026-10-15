package com.zenith26.fest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zenith26.fest.data.api.RetrofitClient
import com.zenith26.fest.ui.admin.AdminDashboardScreen
import com.zenith26.fest.ui.components.ApiUrlDialog
import com.zenith26.fest.ui.navigation.Screen
import com.zenith26.fest.ui.student.StudentPortalScreen
import com.zenith26.fest.ui.theme.Slate50
import com.zenith26.fest.ui.theme.ZenithFestTheme
import com.zenith26.fest.viewmodel.AdminViewModel
import com.zenith26.fest.viewmodel.StudentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenithFestTheme {
                val navController = rememberNavController()
                var showSettingsDialog by remember { mutableStateOf(false) }
                var currentBaseUrl by remember { mutableStateOf(RetrofitClient.getBaseUrl()) }

                val studentViewModel: StudentViewModel = viewModel()
                val adminViewModel: AdminViewModel = viewModel()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Slate50
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.StudentPortal.route
                    ) {
                        composable(Screen.StudentPortal.route) {
                            StudentPortalScreen(
                                viewModel = studentViewModel,
                                onNavigateToAdmin = {
                                    adminViewModel.loadDashboardData()
                                    navController.navigate(Screen.AdminDashboard.route)
                                },
                                onOpenSettings = { showSettingsDialog = true }
                            )
                        }

                        composable(Screen.AdminDashboard.route) {
                            AdminDashboardScreen(
                                viewModel = adminViewModel,
                                onNavigateBack = {
                                    studentViewModel.loadEvents()
                                    navController.popBackStack()
                                },
                                onOpenSettings = { showSettingsDialog = true }
                            )
                        }
                    }

                    if (showSettingsDialog) {
                        ApiUrlDialog(
                            currentUrl = currentBaseUrl,
                            onDismiss = { showSettingsDialog = false },
                            onUrlUpdated = { newUrl ->
                                RetrofitClient.updateBaseUrl(newUrl)
                                currentBaseUrl = newUrl
                                studentViewModel.loadEvents()
                                adminViewModel.loadDashboardData()
                            }
                        )
                    }
                }
            }
        }
    }
}
