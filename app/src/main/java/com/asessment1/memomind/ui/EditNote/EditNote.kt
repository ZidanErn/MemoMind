package com.asessment1.memomind.ui.EditNote

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.asessment1.memomind.Constants
import com.asessment1.memomind.MemoMindApp
import com.asessment1.memomind.R
import com.asessment1.memomind.model.Note
import com.asessment1.memomind.ui.NotesList.NotesFab
import com.asessment1.memomind.ui.NotesViewModel
import com.asessment1.memomind.ui.theme.MemoMindTheme
import com.asessment1.memomind.util.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun NoteEditScreen(noteId: Int, navController: NavController, viewModel: NotesViewModel) {
    val scope = rememberCoroutineScope()
    val note = remember {
        mutableStateOf(Constants.noteDetailPlaceHolder)
    }

    val currentNote = remember { mutableStateOf(note.value.note) }
    val currentTitle = remember { mutableStateOf(note.value.title) }
    val currentPhotos = remember { mutableStateOf(note.value.imageUri) }
    val saveButtonState = remember { mutableStateOf(false) }

    val getImageRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->

        if (uri != null) {
            MemoMindApp.getUriPermission(uri)
        }
        currentPhotos.value = uri.toString()
        if (currentPhotos.value != note.value.imageUri) {
            saveButtonState.value = true
        }
    }
    val dataStore = SettingsDataStore(LocalContext.current)
    val isDark by dataStore.themeFlow.collectAsState(false)

    LaunchedEffect(true) {
        scope.launch(Dispatchers.IO) {
            note.value = viewModel.getNote(noteId) ?: Constants.noteDetailPlaceHolder
            currentNote.value = note.value.note
            currentTitle.value = note.value.title
            currentPhotos.value = note.value.imageUri
        }
    }



    MemoMindTheme(
        darkTheme = isDark
    ) {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.primary) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = (R.string.back)))
                            }
                        },
                        title = {
                            Text(stringResource(id = R.string.edit_note))
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    viewModel.updateNote(
                                        Note(
                                            id = note.value.id,
                                            note = currentNote.value,
                                            title = currentTitle.value,
                                            imageUri = currentPhotos.value
                                        )
                                    )
                                    navController.popBackStack()
                                },
                                enabled = saveButtonState.value
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.save),
                                    contentDescription = stringResource(R.string.save_note)
                                )
                            }
                        },
                        backgroundColor = MaterialTheme.colors.primary
                    )
                },
                floatingActionButton = {
                    NotesFab(
                        contentDescription = stringResource(R.string.add_photo),
                        action = {
                            getImageRequest.launch(arrayOf("image/*"))
                        },
                        icon = R.drawable.camera
                    )
                },
                content = {
                    Column(
                        Modifier
                            .padding(12.dp)
                            .fillMaxSize()
                    ) {
                        if (currentPhotos.value != null && currentPhotos.value!!.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest
                                        .Builder(LocalContext.current)
                                        .data(data = Uri.parse(currentPhotos.value))
                                        .build()
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
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
                                if (currentTitle.value != note.value.title) {
                                    saveButtonState.value = true
                                } else if (currentNote.value == note.value.note &&
                                    currentTitle.value == note.value.title
                                ) {
                                    saveButtonState.value = false
                                }
                            },
                            label = { Text(
                                text = stringResource(id = R.string.title),
                                ) }
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
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
                                if (currentNote.value != note.value.note) {
                                    saveButtonState.value = true
                                } else if (currentNote.value == note.value.note &&
                                    currentTitle.value == note.value.title
                                ) {
                                    saveButtonState.value = false
                                }
                            },
                            label = { Text(
                                text = stringResource(id = R.string.body)
                                )
                            }
                        )
                        if (currentNote.value.isEmpty()) {
                            Text(
                                text = stringResource(R.string.input_invalid),
                                color = MaterialTheme.colors.error,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                    }
                }
                }
            )
        }
    }
}