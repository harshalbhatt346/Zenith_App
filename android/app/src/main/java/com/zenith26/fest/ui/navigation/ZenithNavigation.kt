package com.zenith26.fest.ui.navigation

sealed class Screen(val route: String) {
    object StudentPortal : Screen("student_portal")
    object AdminDashboard : Screen("admin_dashboard")
}
