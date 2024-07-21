package com.example.chatapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun bottomMenu( navController: NavController)
{
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentroute = navBackStackEntry?.destination?.route


    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        BottomNavigation(
             // Adjust bottom padding as needed
        ) {
            BottomScreen.entries.forEach { item ->
                BottomNavigationItem(
                    selected = (currentroute == item.route),
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(painter = painterResource(id = item.icon), contentDescription = item.Btitle) },
                    label = { Text(item.Btitle) }
                )
            }
        }
    }

}