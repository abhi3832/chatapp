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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController,chatViewModel: ChatViewModel = viewModel())
{
    val context = LocalContext.current
    if(chatViewModel.chats.value.isEmpty()){
        Column(modifier = Modifier.fillMaxSize(),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = "No Chats Available")
        }
    }
    else {
        Log.w("NewChat","${chatViewModel.chats.value}")
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(chatViewModel.chats.value) { chat ->

                val chatuser =
                    if (chat.user1.userId == chatViewModel.userData.value?.userId) chat.user2 else chat.user1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            chat.chatId?.let {
                                navController.navigate(ChatRouteScreen.SingleChat.createRoute(it))

                            }
                        }, verticalAlignment = Alignment.CenterVertically
                )
                {
                    showCommonImage(
                        data = chatuser.imageUrl, modifier = Modifier
                            .padding(8.dp)
                            .size(50.dp)
                            .clip(
                                CircleShape
                            )
                            .background(Color.Red)
                    )
                    Column {
                        Text(
                            text = chatuser.name ?: "No Name",
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif
                        )

                        if (chat.messages.isEmpty()) {
                            Text(
                                text = "NewChat",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontStyle = FontStyle.Italic,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (chatViewModel.userData.value?.userId == chat.user1.userId) {
                            if (chat.one == 0) {
                                chat.messages[chat.messages.size - 1].msg?.let {
                                    Text(
                                        text = it,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontStyle = FontStyle.Italic,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Text(
                                    text = "${chat.one} New Messages",
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontStyle = FontStyle.Italic,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            if (chat.two == 0) {
                                chat.messages[chat.messages.size - 1].msg?.let {
                                    Text(
                                        text = it,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontStyle = FontStyle.Italic,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Text(
                                    text = "${chat.two} New Messages",
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontStyle = FontStyle.Italic,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }


                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAB(
    showDialog: MutableState<Boolean>,
    onFabClick: () -> Unit,
    onDismiss: () -> Unit,
    onAddChat: (String) -> Unit,
    onSendRequest : (String) -> Unit
)
{
    val addChatNumber = remember { mutableStateOf("") }

    val temp = showDialog.value

    if(temp)
    {
        AlertDialog(onDismissRequest = { onDismiss(); addChatNumber.value = ""},
            confirmButton = { Button(onClick = { onSendRequest(addChatNumber.value) }) {
                Text(text = "Send")
            } }, title = { Text(text = "Send Request")}, text = { OutlinedTextField(
                value = addChatNumber.value,
                onValueChange = {addChatNumber.value = it},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )})
        

    }
    FloatingActionButton(onClick = { onFabClick() }, shape = CircleShape, modifier= Modifier.padding(40.dp)) {
        Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = Color.White)
    }
}
