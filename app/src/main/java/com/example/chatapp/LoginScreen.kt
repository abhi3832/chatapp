    package com.example.chatapp

    import android.util.Log
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material.CircularProgressIndicator
    import androidx.compose.material.Icon
    import androidx.compose.material.MaterialTheme
    import androidx.compose.material.OutlinedTextField
    import androidx.compose.material3.Button
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.lifecycle.viewmodel.compose.viewModel
    import androidx.navigation.NavController
    import androidx.navigation.NavHostController
    import androidx.navigation.compose.currentBackStackEntryAsState

    @Composable
    fun LoginScreen(rootNavController: NavHostController, chatViewModel: ChatViewModel = viewModel()) {
        var email by remember { mutableStateOf("") }
        var passward by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var signUpSuccess by remember { mutableStateOf(false) }


        if (!chatViewModel.signIn.value) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            )
            {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_chat_24),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp, 100.dp)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = "Login",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(8.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(text = "Email") })
                Spacer(modifier = Modifier.padding(8.dp))
                OutlinedTextField(
                    value = passward,
                    onValueChange = { passward = it },
                    label = { Text(text = "Passward") })
                Spacer(modifier = Modifier.padding(8.dp))

                Button(onClick =
                {
                    if (email.isEmpty() or passward.isEmpty()) {
                        errorMessage = "Please Fill All the required Fields"
                    } else {
                        isLoading = true
                        chatViewModel.login(email, passward) { success ->
                            isLoading = false
                            if (success) {
                                signUpSuccess = true
                                chatViewModel.signIn.value = true
                                chatViewModel.auth.currentUser?.uid?.let {
                                    chatViewModel.getUserData(it)
                                }
                                rootNavController.navigate(Graph.MainGraph)
                                {
                                    popUpTo(Graph.AuthGraph) { inclusive = true }
                                }
                            } else {
                                if (chatViewModel.alreadyExits.value) {
                                    errorMessage = "User already exists with this number"
                                } else {
                                    errorMessage = "SignUp failed"
                                }
                            }
                        }
                    }


                }, modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "LOG IN")
                }
                Text(text = "New User ? Go to Sign Up ->", modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        rootNavController.navigate(AuthRouteScreen.SignUp.route) {
                            popUpTo(Graph.AuthGraph)
                            launchSingleTop = true
                        }
                    })

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(  16.dp))
                }
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

            }
        }
    }

