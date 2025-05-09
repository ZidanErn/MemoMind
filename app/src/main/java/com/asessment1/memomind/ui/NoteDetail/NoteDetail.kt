package com.asessment1.memomind.ui.NoteDetail

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.asessment1.memomind.Constants
import com.asessment1.memomind.Constants.noteDetailPlaceHolder
import com.asessment1.memomind.R
import com.asessment1.memomind.model.Note
import com.asessment1.memomind.ui.NotesViewModel
import com.asessment1.memomind.ui.theme.MemoMindTheme
import com.asessment1.memomind.util.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ShareButton(note: Note) {
    val context = LocalContext.current

    FloatingActionButton(
        onClick = {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                val noteText = buildString {
                    append("${note.title}\n")
                    append("${note.dateUpdated}\n")
                    append("${note.note}\n")
                }
                putExtra(Intent.EXTRA_TEXT, noteText)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share note via"))
        },
        backgroundColor = MaterialTheme.colors.primary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
    ) {
        Icon(Icons.Filled.Share, contentDescription = "Share")
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun NoteDetailScreen(noteId: Int, navController: NavController, viewModel: NotesViewModel) {
    val scope = rememberCoroutineScope()
    val note = remember {
        mutableStateOf(noteDetailPlaceHolder)
    }
    val dataStore = SettingsDataStore(LocalContext.current)
    val isDark by dataStore.themeFlow.collectAsState(false)

    var expanded by remember { mutableStateOf(false)}

    LaunchedEffect(true) {
        scope.launch(Dispatchers.IO) {
            note.value = viewModel.getNote(noteId) ?: noteDetailPlaceHolder
        }
    }

    val openDialog = remember { mutableStateOf(false) }
    val deleteText = remember { mutableStateOf("") }
    val notesToDelete = remember { mutableStateOf(listOf<Note>()) }

    MemoMindTheme(
        darkTheme = isDark
    ) {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                            }
                        },
                        title = {
                            Text(note.value.title ?: "", fontWeight = FontWeight.Bold)
                        },
                        actions = {
                            // Ubah menjadi dropdown menu
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(imageVector = Icons.Filled.Menu, contentDescription = "")
                            }
                            DropdownMenu (
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(onClick = {
                                    navController.navigate(Constants.noteEditNavigation(note.value.id ?: 0))
                                }) {
                                    Text(stringResource(id = R.string.edit_note))
                                }
                                DropdownMenuItem(onClick = {
                                    // Tampilkan dialog penghapusan catatan
                                    openDialog.value = true
                                    deleteText.value = ""
                                    notesToDelete.value = mutableListOf(note.value)
                                }) {
                                    Text(stringResource(id = R.string.delete_note))
                                }
                            }
                        },
                        backgroundColor = MaterialTheme.colors.primary
                    )
                },
                floatingActionButton = {
                    ShareButton(note = note.value)
                },
                floatingActionButtonPosition = FabPosition.End,
                isFloatingActionButtonDocked = true,
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                ) {
                    if (note.value.imageUri != null && note.value.imageUri!!.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest
                                    .Builder(LocalContext.current)
                                    .data(data = Uri.parse(note.value.imageUri))
                                    .build()
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxHeight(0.3f)
                                .fillMaxWidth()
                                .padding(6.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text(
                        text = note.value.title ?: "",
                        modifier = Modifier.padding(top = 24.dp, start = 12.dp, end = 24.dp),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = note.value.dateUpdated ?: "", Modifier.padding(12.dp), color = Color.Gray)
                    Text(text = note.value.note ?: "", Modifier.padding(12.dp))
                }
            }
        }
    }

    // Dialog penghapusan catatan
    DeleteDialog(
        navController = navController,
        openDialog = openDialog,
        text = deleteText,
        action = {
            notesToDelete.value.forEach {
                viewModel.deleteNotes(it)
            }
        },
        notesToDelete = notesToDelete

    )
}

@Composable
fun DeleteButton(
    note: Note,
    openDialog: MutableState<Boolean>,
    deleteText: MutableState<String>,
    navController: NavController,
    notesToDelete: MutableState<List<Note>>,) {
    FloatingActionButton(
        onClick = {
            // Tampilkan dialog penghapusan catatan
            openDialog.value = true
            deleteText.value = ""
            notesToDelete.value = mutableListOf(note)
        },
        backgroundColor = MaterialTheme.colors.primary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
    ) {
        Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.delete_note))
    }
}

@Composable
fun DeleteDialog(
    navController: NavController,
    openDialog: MutableState<Boolean>,
    text: MutableState<String>,
    action: () -> Unit,
    notesToDelete: MutableState<List<Note>>
) {
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(text = stringResource(id = R.string.delete_note_message))
            },
            text = {
                Column() {
                    Text(text.value)
                }
            },
            buttons = {
                Row(
                    modifier = Modifier.padding(all = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column() {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Black,
                                contentColor = Color.White
                            ),
                            onClick = {
                                action.invoke()
                                openDialog.value = false
                                notesToDelete.value = mutableListOf()
                                navController.popBackStack()
                            }
                        ) {
                            Text(stringResource(id = R.string.yes))
                        }
                        Spacer(modifier = Modifier.padding(12.dp))
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Black,
                                contentColor = Color.White
                            ),
                            onClick = {
                                openDialog.value = false
                                notesToDelete.value = mutableListOf()
                            }
                        ) {
                            Text(stringResource(id = R.string.no))
                        }
                    }
                }
            }
        )
    }
}
