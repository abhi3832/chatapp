package com.example.chatapp.Graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.chatapp.ChatRouteScreen
import com.example.chatapp.Graph
import com.example.chatapp.SingleChatScreen
import com.example.chatapp.UserProfileRouteScreen
import com.example.chatapp.viewUserProfile

fun NavGraphBuilder.UserProfileNavGraph(rootNavController: NavHostController)
{
    navigation(route = Graph.UserProfile, startDestination = UserProfileRouteScreen.ViewUserProfile.route){
        composable(UserProfileRouteScreen.ViewUserProfile.route){
            val userId = it.arguments?.getString("userId")
            userId?.let {
                viewUserProfile(rootNavController,userId)
            }

        }

    }
}