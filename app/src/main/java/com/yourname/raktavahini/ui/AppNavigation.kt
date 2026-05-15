package com.yourname.raktavahini.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yourname.raktavahini.data.FirebaseRepository
import com.yourname.raktavahini.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val startDest = if (FirebaseRepository.isLoggedIn) "home" else "login"

    NavHost(navController = navController, startDestination = startDest) {
        composable("login")       { LoginScreen(navController) }
        composable("home")        { HomeScreen(navController) }
        composable("register")    { RegisterScreen(navController) }
        composable("search")      { SearchScreen(navController) }
        composable("profile")     { ProfileScreen(navController) }
        composable("donationlog") { DonationLogScreen(navController) }
        composable("advisor")     { AiAdvisorScreen(navController) }
        composable("requests")    { BloodRequestScreen(navController) }
        composable("stats")       { StatsScreen(navController) }
    }
}