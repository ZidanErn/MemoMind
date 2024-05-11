package com.asessment1.memomind.ui.NotesList

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.asessment1.memomind.Constants
import com.asessment1.memomind.Constants.orPlaceHolderList
import com.asessment1.memomind.R
import com.asessment1.memomind.model.Note
import com.asessment1.memomind.model.getDay
import com.asessment1.memomind.ui.NotesViewModel
import com.asessment1.memomind.ui.theme.MemoMindTheme
import com.asessment1.memomind.util.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun NotesList(navController: NavController, viewModel: NotesViewModel) {
    val openDialog = remember { mutableStateOf(false) }
    val deleteText = remember { mutableStateOf("") }
    val notesQuery = remember { mutableStateOf("") }
    val notesToDelete = remember { mutableStateOf(listOf<Note>()) }
    val notes = viewModel.notes.observeAsState()

    val dataStore = SettingsDataStore(LocalContext.current)
    val isDark by dataStore.themeFlow.collectAsState(false)

    MemoMindTheme(
        darkTheme = isDark
    ) {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = stringResource(id = R.string.app_name))
                        },
                        backgroundColor = MaterialTheme.colors.primary,
                        actions = {

                            IconButton(onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    dataStore.saveDarkMode(!isDark)
                                }
                            }) {
                              Icon(
                                  painter = painterResource(id = if(isDark) R.drawable.baseline_light_mode_24 else R.drawable.baseline_dark_mode_24),
                                  contentDescription = "")
                            }

                            IconButton(onClick = {
                                // Navigate to About
                                navController.navigate(Constants.NAVIGATION_ABOUT)
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = stringResource(id = R.string.about)
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    NotesFab(
                        contentDescription = stringResource(R.string.create_note),
                        action = { navController.navigate(Constants.NAVIGATION_NOTES_CREATE) },
                        icon = R.drawable.note_add_icon
                    )
                }

            ) {
                if (notes.value?.isNotEmpty() == true) {
                    Column {
                        SearchBar(notesQuery)
                        NotesList(
                            notes = notes.value.orPlaceHolderList(),
                            query = notesQuery,
                            openDialog = openDialog,
                            deleteText = deleteText,
                            navController = navController,
                            notesToDelete = notesToDelete
                        )
                    }
                } else {
                    Text(
                        text = stringResource(id = R.string.no_notes_found),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.body1
                    )
                }

                DeleteDialog(
                    openDialog = openDialog,
                    text = deleteText,
                    notesToDelete = notesToDelete,
                    action = {
                        notesToDelete.value.forEach {
                            viewModel.deleteNotes(it)
                        }
                    }
                )
            }
        }
    }
}



@Composable
fun SearchBar(query: MutableState<String>) {
    Column(Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 0.dp)) {
        OutlinedTextField(
            value = query.value,
            placeholder = { Text(stringResource(
                id = R.string.search),
                color = MaterialTheme.colors.onBackground) },
            maxLines = 1,
            onValueChange = { query.value = it },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
//                cursorColor = Color.Black,
//                focusedBorderColor = Color.Black,
//                unfocusedBorderColor = Color.Black
            ),
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.value.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = { query.value = "" }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.icon_cross),
                            contentDescription = stringResource(R.string.clear_search),
                            tint = Color.Black
                        )
                    }
                }

            })

    }
}

@Composable
fun NotesList(
    notes: List<Note>,
    openDialog: MutableState<Boolean>,
    query: MutableState<String>,
    deleteText: MutableState<String>,
    navController: NavController,
    notesToDelete: MutableState<List<Note>>,
) {
    var previousHeader = ""
    LazyColumn(
        contentPadding = PaddingValues(12.dp)
    ) {
        val queriedNotes = if (query.value.isEmpty()) {
            notes
        } else {
            notes.filter { it.note.contains(query.value) || it.title.contains(query.value) }
        }
        itemsIndexed(queriedNotes) { index, note ->
            if (note.getDay() != previousHeader) {
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = note.getDay())
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )
                previousHeader = note.getDay()
            }

            val switchChecked = remember { mutableStateOf(false) } // Menggunakan remember { mutableStateOf(...) }
            NoteListItem(
                note = note,
                openDialog = openDialog,
                deleteText = deleteText,
                navController = navController,
                notesToDelete = notesToDelete,
                switchChecked = switchChecked,
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteListItem(
    note: Note,
    openDialog: MutableState<Boolean>,
    deleteText: MutableState<String>,
    navController: NavController,
    notesToDelete: MutableState<List<Note>>,
    switchChecked: MutableState<Boolean> // State untuk menyimpan status switch
) {
    Box(modifier = Modifier
        .height(120.dp)
        .clip(RoundedCornerShape(12.dp))) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.primary)
                .fillMaxWidth()
                .height(120.dp)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false),
                    onClick = {
                        if (note.id != 0) {
                            navController.navigate(Constants.noteDetailNavigation(note.id ?: 0))
                        }
                    }
                )
        ) {
            Row {
                if (note.imageUri != null && note.imageUri.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(data = Uri.parse(note.imageUri))
                                .build()
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.3f),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    modifier = Modifier.weight(1f) // memberikan bobot agar kolom ini mengambil ruang yang tersisa
                ) {
                    Text(
                        text = note.title,
                        color = MaterialTheme.colors.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                    Text(
                        text = note.note,
                        color = MaterialTheme.colors.onPrimary,
                        maxLines = 3,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    Text(
                        text = note.dateUpdated,
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                }
            }
        }
    }







@Composable
fun NotesFab(contentDescription: String, icon: Int, action: () -> Unit) {
    return FloatingActionButton(
        onClick = { action.invoke() },
        backgroundColor = MaterialTheme.colors.primary
    ) {
        Icon(
            ImageVector.vectorResource(id = icon),
            contentDescription = contentDescription
        )

    }
}

@Composable
fun DeleteDialog(
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
                Text(text = stringResource(id = R.string.delete_note))
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

