package com.example.chatapp.Graphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.chatapp.ChatRouteScreen
import com.example.chatapp.Graph
import com.example.chatapp.GroupRouteScreen
import com.example.chatapp.SingleChatScreen
import com.example.chatapp.SingleGroupScreen


fun NavGraphBuilder.GroupChatNavGraph(rootNavController:  NavHostController)
{
    navigation(route = Graph.GroupGraph, startDestination = GroupRouteScreen.Singlegroup.route){
        composable(GroupRouteScreen.Singlegroup.route){
            val groupId = it.arguments?.getString("groupId")
            groupId?.let {
                SingleGroupScreen(rootNavController, groupId)
            }

        }

    }
}