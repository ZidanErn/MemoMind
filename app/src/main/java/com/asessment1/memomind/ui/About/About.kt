package com.asessment1.memomind.ui.About

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.asessment1.memomind.R
import com.asessment1.memomind.ui.theme.MemoMindTheme
import com.asessment1.memomind.util.SettingsDataStore

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AboutScreen(navController: NavHostController) {

    val dataStore = SettingsDataStore(LocalContext.current)
    val isDark by dataStore.themeFlow.collectAsState(false)
    MemoMindTheme(
        darkTheme = isDark
    ) {
        Surface() {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(id = R.string.about)) },
                        navigationIcon = {
                            IconButton(onClick = {
                                // Pop back stack when back arrow is clicked
                                navController.popBackStack()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = stringResource(id = R.string.back)
                                )
                            }
                        }, backgroundColor = MaterialTheme.colors.primary
                    )
                },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.applogo), // Menggunakan applogo.png sebagai gambar
                            contentDescription = stringResource(id = R.string.app_logo),
                            modifier = Modifier.size(400.2.dp) // Mengatur ukuran gambar
                        )
                        Text(text = stringResource(id = R.string.aboutdesc))
                    }
                }
            )
        }
    }
}

