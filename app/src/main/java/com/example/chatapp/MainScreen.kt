package com.example.chatapp

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text

import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.Graphs.MainNavGraph

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun MainScreen(rootNavController: NavHostController,homeNavController : NavHostController = rememberNavController(),
               chatViewModel: ChatViewModel = viewModel())
{
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val onFabClick : () -> Unit = {showDialog.value = true}
    val onDismiss : () -> Unit = {showDialog.value = false}
    val onAddChat : (String) -> Unit = {
        chatViewModel.onAddChat(it,context)
        showDialog.value = false
    }

    val onSendRequest : (String) -> Unit = {
        chatViewModel.sendRequest(it,context)
        showDialog.value = false
    }
    val showDialogAdd = remember { mutableStateOf(false) }
    val showDialogJoin = remember { mutableStateOf(false) }
    val groupName = remember { mutableStateOf("") }
    val groupId= remember { mutableStateOf("") }


    val navBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentroute = navBackStackEntry?.destination?.route




    val scaffoldState = rememberScaffoldState()
    val currentTitle = titleMap[currentroute] ?: "Default Title"

    Log.d("MainScreen", "Current route: $currentroute")

    androidx.compose.material.Scaffold(
            scaffoldState = scaffoldState,
       floatingActionButton = {if(currentTitle == "Chat") {
           FAB(showDialog,onFabClick,onDismiss,onAddChat,onSendRequest)
       } },
        bottomBar = {
            BottomNavigation(backgroundColor = Color.White)
            {

                BottomScreen.entries.forEach { screen ->
                    BottomNavigationItem(

                        selected = (currentroute == screen.route),
                        onClick = {
                            homeNavController.navigate(screen.route) {
                                popUpTo(homeNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            Log.d("MainScreen", "Navigating to ${screen.route}")
                        },

                        icon = {
                            Icon(painter = painterResource(id = screen.icon), contentDescription = screen.Btitle)
                        },
                        label = { Text(text = screen.Btitle) }
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(text = currentTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black

                ), navigationIcon = {
                    if (currentroute == "profile")
                    {
                        IconButton(onClick = { homeNavController.popBackStack() }, modifier = Modifier.padding(8.dp)) {
                            Icon(painter = painterResource(id = R.drawable.baseline_keyboard_backspace_24),
                                contentDescription = "Back")
                        }
                    }
                },
                actions = { if(currentroute == "profile")
                            {
                                Button(onClick = {

                                        Toast.makeText(context, "Logged Out Successfully", Toast.LENGTH_SHORT).show()
                                        chatViewModel.auth.signOut()
                                        chatViewModel.signIn.value = false
                                        chatViewModel.userData.value = null
                                    homeNavController.popBackStack(homeNavController.graph.startDestinationId, true)
                                    rootNavController.navigate(Graph.RootGraph) {
                                        popUpTo(Graph.RootGraph) { inclusive = true }
                                        launchSingleTop = true
                                    }}, modifier = Modifier.padding(8.dp)) {
                                    Text(text = "LogOut")
                                }
                            }


                            GroupChatScreen(
                                showDialogAdd = showDialogAdd,
                                groupName = groupName,
                                showDialogJoin = showDialogJoin,
                                groupId = groupId,
                                currentRoute = currentroute
                            )
                          },

            )

        }
    ) {
        MainNavGraph(rootNavController = rootNavController,homeNavController,it)
    }

}

@Composable
fun GroupChatScreen(showDialogAdd: MutableState<Boolean>, groupName: MutableState<String>,
                    showDialogJoin : MutableState<Boolean>,groupId: MutableState<String> ,
                    currentRoute: String?) {
    if (currentRoute == "group") {
        Row {
            Button(onClick = { showDialogAdd.value = true }) {
                Text(text = "New")
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = { showDialogJoin.value = true}) {
                Text(text = "Join")
            }
        }
    }


    if (showDialogAdd.value) {
        showAddGroup(showDialogAdd, groupName)
    }
    if (showDialogJoin.value) {
        showJoinGroup(showDialogJoin, groupId)
    }
}

@Composable
fun showAddGroup(showDialogAdd : MutableState<Boolean>, groupName : MutableState<String>, chatViewModel: ChatViewModel = viewModel()){

    val context = LocalContext.current
    AlertDialog(onDismissRequest = { showDialogAdd.value = false; groupName.value = ""},
        confirmButton = { Button(onClick = {showDialogAdd.value = false ;chatViewModel.onAddGroup(groupName.value, context);groupName.value = "" }) {
            Text(text = "Add Group")
        } }, title = { Text(text = "Add Group")}, text = { OutlinedTextField(
            value = groupName.value,
            onValueChange = {groupName.value = it},
            label = { Text(text = "Enter Group Name")}

            )
        })

}

@Composable
fun showJoinGroup(showDialogJoin : MutableState<Boolean>, groupId : MutableState<String>, chatViewModel: ChatViewModel = viewModel()){
    val context = LocalContext.current
    AlertDialog(onDismissRequest = { showDialogJoin.value = false; groupId.value = ""},
        confirmButton = { Button(onClick = { showDialogJoin.value = false;chatViewModel.onJoinGroup(groupId.value, context);groupId.value = "" }) {
            Text(text = "Join Group")
        } }, title = { Text(text = "Join Group")}, text = { OutlinedTextField(
            value = groupId.value,
            onValueChange = {groupId.value = it},
            label = { Text(text = "Enter Group Id")}

            )
        })

}