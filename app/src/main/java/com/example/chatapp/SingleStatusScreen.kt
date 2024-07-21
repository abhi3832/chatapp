package com.example.chatapp

import android.content.res.Configuration
import android.view.ViewTreeObserver
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TextField

import androidx.compose.material.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.chatapp.data.Message
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleGroupScreen(navController: NavHostController, groupId : String, chatViewModel: ChatViewModel = viewModel()){

    var reply by rememberSaveable { mutableStateOf("") }
    val myUser = chatViewModel.userData.value
    val currentGroup = chatViewModel.groupsList.value.firstOrNull { it?.groupId == groupId }

    LaunchedEffect(key1 = Unit) {
        chatViewModel.getGroupMsg(groupId)
    }

    BackHandler {
        navController.popBackStack()
        chatViewModel.depopulateGroupMsgs()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        androidx.compose.material3.TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    showCommonImage(
                        data = currentGroup?.imageUrl ?: "",
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Text(text = currentGroup?.groupName ?: "")
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(painter = painterResource(id = R.drawable.baseline_keyboard_backspace_24),
                        contentDescription = "Back")
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
            groupMessageBox(
                modifier = Modifier.fillMaxSize(),
                groupMessage = chatViewModel.groupMsgs.value ,
                        currentUserId = chatViewModel.userData.value?.userId

            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp).background(color = Color.Transparent),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = reply,
                onValueChange = { reply = it },
                placeholder = { Text(text = "Type your message...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            Button(
                onClick = {
                    if (reply.isNotBlank()) {
                        chatViewModel.onSendGroupReply(groupId, reply)
                        reply = ""
                    }
                },
                modifier = Modifier.height(56.dp)
            ) {
                Text(text = "Send")
            }
        }
    }

}

@Composable
fun groupMessageBox(modifier: Modifier, groupMessage: List<Message>, currentUserId : String?){

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



    LazyColumn(state = listState,modifier = Modifier
        .fillMaxSize()
    ){
        items(groupMessage){
                msg ->
            val alignment = if(msg.sentBy == currentUserId) Alignment.End else Alignment.Start
            val background = if (msg.sentBy == currentUserId) Color(85, 33, 138) else Color(45, 45, 56)

            Column(modifier = Modifier.fillMaxSize()
                ,horizontalAlignment = alignment)
            {
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
                    msg.timestamp?.let { Text(
                        text = it,
                        style = MaterialTheme.typography.caption.copy(color = Color.Black),
                        textAlign = TextAlign.End,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = if (isDarkMode) Color.White else Color.Black

                    ) }
                }
            }

        }
    }
    LaunchedEffect(groupMessage.size) {
        if (groupMessage.isNotEmpty()) {
            listState.animateScrollToItem(groupMessage.size - 1)
        }
    }

    LaunchedEffect(imeVisible) {
        if (imeVisible && groupMessage.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(groupMessage.size - 1)
            }
        }
    }


}

