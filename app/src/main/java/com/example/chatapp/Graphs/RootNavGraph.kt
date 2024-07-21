package com.example.chatapp.Graphs

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.ChatListScreen
import com.example.chatapp.ChatViewModel
import com.example.chatapp.Graph
import com.example.chatapp.MainScreen

@Composable
fun RootNavGraph(chatViewModel: ChatViewModel = viewModel())
{
    val rootNavController : NavHostController = rememberNavController()

    NavHost(navController = rootNavController , route = Graph.RootGraph, startDestination = (if(chatViewModel.signIn.value) Graph.MainGraph else Graph.AuthGraph))
    {
        authNavGraph(rootNavController = rootNavController)
        composable(route = Graph.MainGraph){
           MainScreen(rootNavController)
        }
        ChatNavGraph(rootNavController = rootNavController)
        GroupChatNavGraph(rootNavController = rootNavController)
        UserProfileNavGraph(rootNavController)
    }
}