package com.example.chatapp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController

@Composable
fun GroupChatScreen(navController: NavHostController, chatViewModel: ChatViewModel = viewModel())
{
    val context = LocalContext.current

    if(chatViewModel.groupsList.value.isEmpty()){
        Column(modifier = Modifier.fillMaxSize(),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = "No Groups Available")
            //Log.w("Check23","Value of groupStatus : ${chatViewModel.groupStatus.value}")
            Log.w("Check23","Size of group : ${chatViewModel.groupsList.value.size}")
        }
    }
    else
    {
        LazyColumn(modifier = Modifier.fillMaxSize()){
            items(chatViewModel.groupsList.value){
                item ->
                val groupId = item?.groupId
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                              if(groupId != null){
                                  navController.navigate(GroupRouteScreen.Singlegroup.createRoute(groupId))
                              }
                    }, verticalAlignment = Alignment.CenterVertically){
                    showCommonImage(data = item?.imageUrl, modifier = Modifier
                        .padding(8.dp)
                        .size(50.dp)
                        .clip(
                            CircleShape
                        )
                        .background(Color.Red))


                        item?.groupName?.let { Text(text = it) }

                }
            }
        }
    }
}