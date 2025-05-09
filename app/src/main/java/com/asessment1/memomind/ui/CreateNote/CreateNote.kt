package com.asessment1.memomind.ui.CreateNote

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.asessment1.memomind.MemoMindApp
import com.asessment1.memomind.R
import com.asessment1.memomind.ui.NotesList.NotesFab
import com.asessment1.memomind.ui.NotesViewModel
import com.asessment1.memomind.ui.theme.MemoMindTheme
import com.asessment1.memomind.util.SettingsDataStore

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CreateNoteScreen(
    navController: NavController,
    viewModel: NotesViewModel
) {
    val currentNote = remember { mutableStateOf("") }
    val currentTitle = remember { mutableStateOf("") }
    val currentPhotos = remember { mutableStateOf("") }
    val saveButtonState = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val dataStore = SettingsDataStore(LocalContext.current)
    val isDark by dataStore.themeFlow.collectAsState(false)

    val getImageRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) {
        if (it != null) {
            MemoMindApp.getUriPermission(it)
        }
        currentPhotos.value = it.toString()
    }

    MemoMindTheme(
        darkTheme = isDark
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.primary) {
            Scaffold(
                scaffoldState = scaffoldState,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                            }
                        },
                        title = { Text(stringResource(id = R.string.create_note)) },
                        actions = {
                            IconButton(
                                onClick = {
                                    viewModel.createNote(
                                        currentTitle.value,
                                        currentNote.value,
                                        currentPhotos.value
                                    )
                                    navController.popBackStack()
                                },
                                enabled = saveButtonState.value
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.save),
                                    contentDescription = stringResource(R.string.save_note),
                                    tint = if (saveButtonState.value) Color.Black else Color.Gray
                                )
                            }
                        },
                        backgroundColor = MaterialTheme.colors.primary
                    )
                },
                floatingActionButton = {
                    NotesFab(
                        contentDescription = stringResource(R.string.add_image),
                        action = { getImageRequest.launch(arrayOf("image/*")) },
                        icon = R.drawable.camera
                    )
                },
                content = {
                    Column(
                        Modifier
                            .padding(12.dp)
                            .fillMaxSize()
                    ) {
                        if (currentPhotos.value.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest
                                        .Builder(LocalContext.current)
                                        .data(data = Uri.parse(currentPhotos.value))
                                        .build()
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxHeight(0.3f)
                                    .padding(6.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        OutlinedTextField(
                            value = currentTitle.value,
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedLabelColor = MaterialTheme.colors.onBackground,
                                unfocusedLabelColor = MaterialTheme.colors.onBackground,
//                                cursorColor = Color.Black,
//                                focusedBorderColor = Color.Black,
//                                unfocusedBorderColor = Color.Black
                                cursorColor = MaterialTheme.colors.onBackground,
                                focusedBorderColor = MaterialTheme.colors.onBackground,
                                unfocusedBorderColor = MaterialTheme.colors.onBackground
                            ),
                            onValueChange = { value ->
                                currentTitle.value = value
                                saveButtonState.value =
                                    currentTitle.value.isNotEmpty() && currentNote.value.isNotEmpty()
                            },
                            label = {
                                Text(
                                    text = stringResource(id = R.string.title)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.padding(9.dp))
                        if (currentTitle.value.isEmpty()) {
                            Text(
                                text = stringResource(R.string.input_invalid),
                                color = MaterialTheme.colors.error,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }
                        OutlinedTextField(
                            value = currentNote.value,
                            modifier = Modifier
                                .fillMaxHeight(0.5f)
                                .fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedLabelColor = MaterialTheme.colors.onBackground,
                                unfocusedLabelColor = MaterialTheme.colors.onBackground,
//                                cursorColor = Color.Black,
//                                focusedBorderColor = Color.Black,
//                                unfocusedBorderColor = Color.Black
                                cursorColor = MaterialTheme.colors.onBackground,
                                focusedBorderColor = MaterialTheme.colors.onBackground,
                                unfocusedBorderColor = MaterialTheme.colors.onBackground
                            ),
                            onValueChange = { value ->
                                currentNote.value = value
                                saveButtonState.value =
                                    currentTitle.value.isNotEmpty() && currentNote.value.isNotEmpty()
                            },
                            label = { Text(
                                text = stringResource(id = R.string.body)) }
                        )
                        if (currentNote.value.isEmpty()) {
                            Text(
                                text = stringResource(R.string.input_invalid),
                                color = MaterialTheme.colors.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                    }
                }

        })
    }
}
}
