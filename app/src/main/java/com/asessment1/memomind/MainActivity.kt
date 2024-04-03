package com.asessment1.memomind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.asessment1.memomind.ui.About.AboutScreen
import com.asessment1.memomind.ui.EditNote.NoteEditScreen
import com.asessment1.memomind.ui.NoteDetail.NoteDetailScreen
import com.asessment1.memomind.ui.NotesList.NotesList
import com.asessment1.memomind.ui.NotesViewModel
import com.asessment1.memomind.ui.NotesViewModelFactory
import com.asessment1.memomind.ui.CreateNote.CreateNoteScreen


class MainActivity : ComponentActivity() {

    private lateinit var notesViewModel : NotesViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieve viewModel
        notesViewModel =  NotesViewModelFactory(MemoMindApp.getDao()).create(NotesViewModel::class.java)


        setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = Constants.NAVIGATION_NOTES_LIST
            ) {
                // Notes List
                composable(Constants.NAVIGATION_NOTES_LIST) { NotesList(navController, notesViewModel) }

                // Notes Detail page
                composable(
                    Constants.NAVIGATION_NOTE_DETAIL,
                    arguments = listOf(navArgument(Constants.NAVIGATION_NOTE_ID_Argument) {
                        type = NavType.IntType
                    })
                ) { backStackEntry ->
                    backStackEntry.arguments?.getInt(Constants.NAVIGATION_NOTE_ID_Argument)
                        ?.let { NoteDetailScreen(noteId = it, navController, notesViewModel) }
                }

                // Notes Edit page
                composable(
                    Constants.NAVIGATION_NOTE_EDIT,
                    arguments = listOf(navArgument(Constants.NAVIGATION_NOTE_ID_Argument) {
                        type = NavType.IntType
                    })
                ) { backStackEntry ->
                    backStackEntry.arguments?.getInt(Constants.NAVIGATION_NOTE_ID_Argument)
                        ?.let { NoteEditScreen(noteId = it, navController, notesViewModel) }
                }

                // Create Note Page
                composable(Constants.NAVIGATION_NOTES_CREATE) { CreateNoteScreen(navController, notesViewModel) }

                // About Page
                composable(Constants.NAVIGATION_ABOUT) {
                    AboutScreen(navController = navController)
                }


            }

        }
    }
}




