package com.example.chatapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.chatapp.data.ChatUser

@Composable
fun RequestList(rootNavController: NavHostController, chatViewModel: ChatViewModel = viewModel())
{
    var showProfile by remember { mutableStateOf(false) }


    if(chatViewModel.requests.value.isEmpty())
    {
        Text(text = "No Requests!!")
    }
    else
    {
        LazyColumn()
        {
            items(chatViewModel.requests.value) { request ->


                Row(modifier = Modifier.clickable {
                        if(request.sentBy.userId != null)
                        {
                            rootNavController.navigate(UserProfileRouteScreen.ViewUserProfile.createRoute(request.sentBy.userId))
                        }
                    }, verticalAlignment = Alignment.CenterVertically)
                {
                    showCommonImage(
                        data = request.sentBy.imageUrl, modifier = Modifier
                            .padding(8.dp)
                            .size(50.dp)
                            .clip(
                                CircleShape
                            )
                            .background(Color.Red)
                    )

                    request.sentBy.name?.let { Text(text = it) }
                }
            }
        }
    }
}

