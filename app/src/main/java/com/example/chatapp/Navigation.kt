package com.example.chatapp

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


enum class BottomScreen(val Btitle : String , val route : String, @DrawableRes val icon : Int)
{
      Chatlist("Chat",MainRouteScreen.Chat.route,R.drawable.baseline_chat_24),
      GROUPLIST("Group",MainRouteScreen.Group.route, R.drawable.baseline_update_24),
      Profile("Profile",MainRouteScreen.Profile.route,R.drawable.baseline_account_circle_24),
      REQUEST("Requests", MainRouteScreen.Request.route,R.drawable.baseline_emoji_people_24)

}


val titleMap = mapOf(
     MainRouteScreen.Profile.route to BottomScreen.Profile.Btitle,
     MainRouteScreen.Group.route to BottomScreen.GROUPLIST.Btitle,
     MainRouteScreen.Chat.route to BottomScreen.Chatlist.Btitle,
     MainRouteScreen.Request.route to BottomScreen.REQUEST.Btitle
)


object Graph{
     const val RootGraph = "rootGraph"
     const val AuthGraph = "authGraph"
     const val MainGraph = "mainScreenGraph"
     const val ChatGraph = "chatGraph"
     const val GroupGraph = "groupChatGraph"
     const val UserProfile = "userProfile"
}

sealed class AuthRouteScreen(val route : String){
     object Login : AuthRouteScreen("login")
     object SignUp : AuthRouteScreen("signup")
}

sealed class MainRouteScreen(val route : String){
     object Chat : MainRouteScreen("chatList")
     object Group : MainRouteScreen("group")
     object Profile : MainRouteScreen("profile")
     object Request : MainRouteScreen("requestList")
}

sealed class ChatRouteScreen(val route : String)
{
     object SingleChat : ChatRouteScreen("singleChat/{chatId}")
     {
          fun createRoute(id:String) = "singleChat/$id"
     }
}

sealed class GroupRouteScreen(val route : String)
{
     object Singlegroup : GroupRouteScreen("singleGroup/{groupId}")
     {
          fun createRoute(groupId:String) = "singleGroup/$groupId"
     }
}

sealed class UserProfileRouteScreen(val route : String)
{
     object ViewUserProfile : UserProfileRouteScreen("userProfile/{userId}")
     {
          fun createRoute(userId:String) = "userProfile/$userId"
     }
}
