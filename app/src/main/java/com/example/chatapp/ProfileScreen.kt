package com.example.chatapp

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text2.input.rememberTextFieldState
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.chatapp.data.ChatUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(homeNavController: NavHostController, chatViewModel: ChatViewModel = viewModel(), otherUser : Boolean = false, otherUserData : ChatUser = ChatUser() )
{
    val imageUrl = chatViewModel.userData.value?.image
    val context = LocalContext.current
    val userData by chatViewModel.userData
    var name by  rememberSaveable{ mutableStateOf(chatViewModel.userData.value?.name ?: "")}
    var number by rememberSaveable  {mutableStateOf(chatViewModel.userData.value?.number ?: "")}


    LaunchedEffect(userData) {
        userData?.let {
            name = it.name ?: ""
            number = it.number ?: ""
        }
    }

    Log.w("Hello", "Name: $name")
    Log.w("Hello", "Number: $number")

       Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center)
       {
           if(!otherUser)
           {
               ProfileImage(imageUrl =  imageUrl , chatViewModel = chatViewModel)
           }
           Row(modifier = Modifier
               .fillMaxWidth()
               .padding(4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly)
           {
               Text(text = "Name")

               (if(!otherUser) name else otherUserData.name)?.let {it1 ->
                   TextField(value = it1, onValueChange =  {if(!otherUser) name = it}  , colors = TextFieldDefaults.textFieldColors(
                       focusedTextColor = Color.Black, containerColor = Color.Transparent
                   ))
               }

           }

           Spacer(modifier = Modifier.padding(16.dp))

           Row(modifier = Modifier
               .fillMaxWidth()
               .padding(4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly)
           {
               Text(text = "Number")

               (if(!otherUser) number else otherUserData.number)?.let {it1 ->
                   TextField(value = it1, onValueChange = {if(!otherUser) number = it} , colors = TextFieldDefaults.textFieldColors(
                       focusedTextColor = Color.Black, containerColor = Color.Transparent
                   ))
               }


           }

           Spacer(modifier = Modifier.padding(8.dp))

           if(!otherUser)
           {
               Button(onClick = {
                   Toast.makeText(context, "Saved Successfully", Toast.LENGTH_SHORT).show()
                   chatViewModel.createOrUpdateProfile(name = name, number = number)
               }) {
                   Text(text = "Save")
               }
           }

       }

}



@Composable
fun ProfileImage(imageUrl: String?, chatViewModel: ChatViewModel) {
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            chatViewModel.uploadProfileImage(uri)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                launcher.launch("image/*")
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center)
        {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray), // Default background in case of no image
                contentAlignment = Alignment.Center
            ) {
                showCommonImage(data = imageUrl, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Spacer(modifier = Modifier.padding(16.dp))
            Text(text = "Change Profile Image")
        }

    }
}

@Composable
fun showCommonImage(data : String?, modifier : Modifier = Modifier.wrapContentSize(),
                    contentScale : ContentScale = ContentScale.Crop){

    val painter = rememberImagePainter(data = data)
    
    Image(painter = painter, contentDescription = null, modifier, contentScale = contentScale)

}

