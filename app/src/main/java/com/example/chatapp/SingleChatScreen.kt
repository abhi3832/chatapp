package com.example.chatapp

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon


import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.chatapp.data.Message
import kotlinx.coroutines.launch

import android.view.ViewTreeObserver
import androidx.compose.foundation.ExperimentalFoundationApi

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.rounded.Send
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chatapp.data.ChatData


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable

fun SingleChatScreen(navController: NavHostController, chatId : String,  chatViewModel: ChatViewModel = viewModel()) {

    var reply by rememberSaveable { mutableStateOf("") }
    val myUser = chatViewModel.userData.value
    val currentChat = chatViewModel.chats.value.firstOrNull{it.chatId == chatId}
    val currentChatData = chatViewModel.chats.value.firstOrNull{it.chatId == chatId}
    val chatUser = if(myUser?.userId == currentChat?.user1?.userId) currentChat?.user2 else currentChat?.user1


    LaunchedEffect(key1 = Unit) {
        chatViewModel.getMessages(chatId)
    }

    BackHandler {
        chatViewModel.dePopulateMessages()
        navController.popBackStack()
    }


   // Log.w("Chat", "$chatUser")
    Column(modifier = Modifier.fillMaxSize())
    {
        androidx.compose.material3.TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    showCommonImage(
                        data = chatUser?.imageUrl ?: "",
                        modifier = Modifier
                            .padding(4.dp)
                            .size(35.dp)
                            .clip(CircleShape)
                    )
                    Text(text = chatUser?.name ?: "", modifier = Modifier.clickable {
                        navController.navigate(UserProfileRouteScreen.ViewUserProfile.createRoute(chatUser?.userId!!))
                    })
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(painter = painterResource(id = R.drawable.baseline_keyboard_backspace_24),
                        contentDescription = "Back", modifier = Modifier.padding(end = 15.dp))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color.Black,
                navigationIconContentColor = Color.Black
            )
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (currentChat != null) {
                messageBox(
                    modifier = Modifier.fillMaxSize(),
                    //chatViewModel.messages.value,
                    currentUserId = chatViewModel.userData.value?.userId,
                    chatId,
                    currentChatData,
                    chatViewModel.particularChat.value
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(color = Color.Transparent),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Box(modifier = Modifier.background( color = Color.Transparent))
            {
                TextField(
                    value = reply,
                    onValueChange = { reply = it },
                    placeholder = { Text(text = "Type your message...", fontFamily = FontFamily.SansSerif) },
                    modifier = Modifier
                        .width(320.dp)
                        .padding(end = 8.dp)
                        .background(shape = RoundedCornerShape(20), color = Color.LightGray)
                )
            }


            Box(modifier = Modifier

                .background(color = Color.Green, shape = RoundedCornerShape(16.dp)) )
            {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            if (reply.isNotBlank()) {
                                chatViewModel.onSendReply(chatId, reply.trimEnd(), chatUser!!)
                                reply = ""
                            }
                        }
                        .size(40.dp),
                    tint = Color.Black
                )
            }
        }
    }
    

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun messageBox(
    modifier: Modifier,
//    message: List<Message>,
    currentUserId: String?,
    chatId: String,
    currentChatData: ChatData?,
    particularChat: ChatData?,
    chatViewModel: ChatViewModel = viewModel()
){

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val imeState = rememberImeState()
    val imeVisible = imeState.value


    val context = LocalContext.current
    val configuration = context.resources.configuration
    val isDarkMode = when (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        Configuration.UI_MODE_NIGHT_NO -> false
        else -> false // Default to light mode
    }

    var lastDate = remember { mutableStateOf("") }

    //val message by remember { mutableStateOf<List<Message>>(particularChat?.messages!!) }

    LazyColumn(state = listState,modifier = Modifier
        .fillMaxSize()
        ){


        items(particularChat?.messages!!){
            msg ->
            val alignment = if(msg.sentBy == currentUserId) Alignment.End else Alignment.Start
            val background = if (msg.sentBy == currentUserId) Color(85, 33, 138) else Color(45, 45, 56)


            if(!msg.seen && msg.sentTo == chatViewModel.userData.value?.userId)
            {
                msg.seen = true
                chatViewModel.db.collection("chats").document(chatId).set(particularChat)
                if(msg.sentBy == particularChat.user1.userId)
                {
                    particularChat.two = particularChat.two-1
                }
                else
                {
                    particularChat.one = particularChat.one- 1
                }
                chatViewModel.db.collection("chats").document(chatId).set(particularChat)

            }

            val temp = msg.timestamp?.substring(0, 11) ?: ""
            if(lastDate.value != temp)
            {
                lastDate.value = msg.timestamp?.substring(0,11) ?: ""
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center)
                {
                    Text(text = lastDate.value, fontSize = 12.sp, modifier = Modifier.background(Color.LightGray))
                }
            }

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp), horizontalAlignment = alignment) {
                Box(modifier = Modifier
                    .wrapContentSize()
                    .background(
                        color = background,
                        shape = RoundedCornerShape(
                            topEnd = 16.dp,
                            topStart = 16.dp,
                            bottomEnd = 16.dp,
                            bottomStart = 16.dp
                        )
                    ))
                {
                    msg.msg?.let { Text(text = it, modifier = Modifier
                        .padding(8.dp)
                        .widthIn(max = 250.dp),
                        color = Color.White, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold) }
                }
                Row(

                    horizontalArrangement = Arrangement.End
                )
                {
                    val timePart = msg.timestamp?.substringAfter(' ')
                    if (timePart != null) {
                        Text(
                            text = timePart,
                            style = MaterialTheme.typography.caption.copy(color = Color.Black),
                            textAlign = TextAlign.End,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = if (isDarkMode) Color.White else Color.Black

                        )
                    }
                }
            }

        }
    }

    LaunchedEffect(particularChat?.messages!!.size) {
        if (particularChat?.messages!!.isNotEmpty())
        {
            listState.animateScrollToItem(particularChat?.messages!!.size-1)
        }
    }

    LaunchedEffect(imeVisible) {
        if (imeVisible && particularChat?.messages!!.isNotEmpty())
        {
            coroutineScope.launch {
                listState.animateScrollToItem(particularChat?.messages!!.size-1)
            }
        }
    }


}

@Composable
fun rememberImeState(): State<Boolean> {
    val imeState = remember {
        mutableStateOf(false)
    }

    val view = LocalView.current
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val isKeyboardOpen = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
            imeState.value = isKeyboardOpen
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
    return imeState
}