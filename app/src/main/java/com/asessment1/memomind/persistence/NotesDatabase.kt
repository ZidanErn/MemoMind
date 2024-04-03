package com.asessment1.memomind.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.asessment1.memomind.model.Note

@Database(entities = [
  Note::class], version = 1)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun NotesDao(): NotesDao

}