package com.example.chatapp.Graphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.chatapp.ChatRouteScreen
import com.example.chatapp.Graph
import com.example.chatapp.MainRouteScreen
import com.example.chatapp.SingleChatScreen


fun NavGraphBuilder.ChatNavGraph(rootNavController:  NavHostController)
{
    navigation(route = Graph.ChatGraph, startDestination = ChatRouteScreen.SingleChat.route){
        composable(ChatRouteScreen.SingleChat.route){
            val chatId = it.arguments?.getString("chatId")
           chatId?.let {
                SingleChatScreen(rootNavController, chatId)
           }

        }

    }
}