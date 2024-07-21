package com.example.chatapp.Graphs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.chatapp.AuthRouteScreen
import com.example.chatapp.ChatListScreen
import com.example.chatapp.Graph
import com.example.chatapp.GroupChatScreen
import com.example.chatapp.MainRouteScreen
import com.example.chatapp.ProfileScreen
import com.example.chatapp.RequestList


@Composable
fun MainNavGraph(rootNavController : NavHostController, homeNavController: NavHostController, pd : PaddingValues) {
    NavHost(
        navController = homeNavController,
        route = Graph.MainGraph,
        startDestination = MainRouteScreen.Chat.route
    ) {
        composable(MainRouteScreen.Chat.route) {
            ChatListScreen(rootNavController)
        }
        composable(MainRouteScreen.Group.route) {
            GroupChatScreen(rootNavController)
        }
        composable(MainRouteScreen.Profile.route) {
            ProfileScreen(rootNavController)
        }
        composable(MainRouteScreen.Request.route) {
            RequestList(rootNavController)
        }

    }
}