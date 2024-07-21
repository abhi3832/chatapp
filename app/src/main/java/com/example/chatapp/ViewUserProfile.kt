package com.example.chatapp

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.chatapp.data.ChatUser
import com.example.chatapp.data.Request
import com.example.chatapp.data.UserData
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun viewUserProfile(rootNavController: NavHostController, userId : String, chatViewModel: ChatViewModel = viewModel(), fromPersonalChat : Boolean = false)
{
    var user by remember { mutableStateOf<UserData?>(null) }
    val context  = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId)
    {
        chatViewModel.db.collection("users").document(userId).get().addOnSuccessListener{
            if(it.exists())
            {
                //Log.w("See", "$user")
                user = it.toObject<UserData>()!!
                Log.w("See", "$user")

            }
        }
    }

    var Reject by remember { mutableStateOf<Boolean>(false) }
    var Accept by remember { mutableStateOf<Boolean>(false) }
    var Block by remember { mutableStateOf<Boolean>(false) }
    var navigateBack by remember { mutableStateOf(false) }
    var acceptRequestCompleted by remember { mutableStateOf(false) }



    Column {
        androidx.compose.material3.TopAppBar(title = { Text(text = "User Profile")}, navigationIcon = {
            Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back", modifier = Modifier.clickable {
                rootNavController.popBackStack()
            })
        })
        Box(
            Modifier
                .size(200.dp)
                .fillMaxWidth()
                .background(Color.White)
                .align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) {
            showCommonImage(data = user?.image ,modifier = Modifier
                .padding(8.dp)
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Red))
        }
        Spacer(modifier = Modifier.padding(20.dp))
        Box()
        {
            Row(Modifier.padding(start = 15.dp)) {
                Text(text = "Name : ", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                user?.name?.let { Text(text = it, fontSize = 20.sp, fontWeight = FontWeight.Bold) }

            }
        }
        Spacer(modifier = Modifier.padding(10.dp))
        Box()
        {
            Row(Modifier.padding(start = 15.dp)){
                Text(text = "Number : ", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                user?.number?.let { Text(text = it, fontSize = 20.sp, fontWeight = FontWeight.Bold) }

            }
        }
       // Spacer(modifier = Modifier.padding(end = 30.dp))
        if(!fromPersonalChat)
        {
            Row()
            {
                Button(onClick = {
                    user?.number?.let { number ->
                        coroutineScope.launch {
                            AcceptRequest(userId, chatViewModel, context, number) {
                                acceptRequestCompleted = true
                            }
                            chatViewModel.onAddChat(user?.number!!, context)
                            rootNavController.popBackStack()
                        }
                    }


                }, modifier = Modifier.padding(24.dp)) {
                    Text(text = "Accept")
                }
                Button(onClick = {
                    Block = true


                }, modifier = Modifier.padding(24.dp)) {
                    Text(text = "Block")
                }
                Button(onClick = {
                    Reject = true


                }, modifier = Modifier.padding(24.dp)) {
                    Text(text = "Reject")
                }

            }
        }

    }
    if(Reject)
    {
        RejectRequest(userId = userId, context = context)
        Reject = false
    }
//    if(Accept)
//    {
//        user?.number?.let { AcceptRequest(userId = userId, context = context, number = it) }
//        Accept = false
//        chatViewModel.onAddChat(user?.number!!, context)
//        //rootNavController.popBackStack()
//        navigateBack = true
//    }
    if(Block)
    {
        BlockRequest(userId = userId, context = context)
        Block = false
    }

//    if(acceptRequestCompleted)
//    {
//        LaunchedEffect(Unit) {
//            rootNavController.popBackStack()
//            acceptRequestCompleted = false
//        }
//    }

}

@Composable
fun RejectRequest(userId : String, chatViewModel: ChatViewModel = viewModel(), context: Context)
{
    chatViewModel.db.collection("requests").
    where(Filter.and(Filter.equalTo("sentTo.userId", chatViewModel.userData.value?.userId), Filter.equalTo("sentBy.userId", userId))).get().addOnSuccessListener {
        if(it != null)
        {

            for (document in it.documents) {
                document.reference.delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Request Rejected!!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("Error", e)
                    }
            }
        }
    }
}


fun AcceptRequest(userId : String, chatViewModel: ChatViewModel, context: Context, number : String, onComplete : () -> Unit)
{

    chatViewModel.db.collection("requests").
    where(Filter.and(Filter.equalTo("sentTo.userId", chatViewModel.userData.value?.userId), Filter.equalTo("sentBy.userId", userId))).get().addOnSuccessListener {
        if(it != null)
        {
            for (document in it.documents) {
                document.reference.delete()
                    .addOnSuccessListener {
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        Log.w("Error", e)
                    }
            }
        }
    }
}

@Composable
fun BlockRequest(userId : String, chatViewModel: ChatViewModel = viewModel(), context: Context)
{
    chatViewModel.db.collection("requests").
    where(Filter.and(Filter.equalTo("sentTo.userId", chatViewModel.userData.value?.userId), Filter.equalTo("sentBy.userId", userId))).get().addOnSuccessListener {
        if(it != null)
        {
            val ReqObj = it.toObjects<Request>()[0]
            if(ReqObj.requestId != null)
            {
                chatViewModel.db.collection("requests").document(ReqObj.requestId!!).update("isBlocked", true)
                Toast.makeText(context, "User Blocked!", Toast.LENGTH_SHORT).show()
            }


        }
    }
}
