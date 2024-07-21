package com.example.chatapp.Graphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.chatapp.AuthRouteScreen
import com.example.chatapp.Graph
import com.example.chatapp.LoginScreen
import com.example.chatapp.SignUpScreen


fun NavGraphBuilder.authNavGraph(rootNavController:  NavHostController)
{
    navigation(route = Graph.AuthGraph, startDestination = AuthRouteScreen.SignUp.route){
        composable(AuthRouteScreen.SignUp.route){
                SignUpScreen(rootNavController)
        }
        composable(AuthRouteScreen.Login.route){
                LoginScreen(rootNavController)
        }
    }
}