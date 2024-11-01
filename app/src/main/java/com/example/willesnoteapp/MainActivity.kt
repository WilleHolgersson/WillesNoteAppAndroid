package com.example.willesnoteapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.willesnoteapp.ui.theme.WillesNoteAppTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

data class Note(
    var noteTitleLabel: String,
    var noteDescriptLabel: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WillesNoteAppTheme {
                NotesAppW()  // detta sätter up navigation i appen
            }
        }
    }
}

@Composable
fun NotesAppW() {
    val navController = rememberNavController()
    val userNotesW = remember { mutableStateListOf<Note>() }

    NavHost(navController = navController, startDestination = "main") {

        composable("main") {
            MainScreen(navController, userNotesW)
        }

        // detta är för en mer detaljerad view av note
        composable("noteDetail/{index}") { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: -1
            if (index != -1) {
                NoteDetailScreen(
                    navController,
                    note = userNotesW[index],
                    noteIndex = index
                )
            }
        }

        // för att skapa en ny note
        composable("editNote") {
            CreateEditNoteScreen(navController, userNotesW)
        }

        // för att ändra i en redan exsterande note  (with note index)
        composable("editNote/{noteIndex}") { backStackEntry ->
            val noteIndex = backStackEntry.arguments?.getString("noteIndex")?.toIntOrNull()
            CreateEditNoteScreen(navController, userNotesW, noteIndex = noteIndex)
        }
    }
}


// navController används för att navigerea mellan olika skärmar i appen
// notes är en mutable lista av note object för display
// modiefie tillåter flera stilar eller beteenden
@Composable
fun MainScreen(navController: NavHostController, notes: MutableList<Note>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        FloatingActionButton(
            onClick = {
                try {
                    Log.d("MainScreen", "Navigating to create note screen")
                    navController.navigate("editNote")
                } catch (e: Exception) {
                    Log.e("MainScreen", "Navigation error: ${e.localizedMessage}")
                }
            },
            modifier = Modifier   // tillåter extra styling eller beteenden
                .size(56.dp)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create New Note")
        }

        Spacer(modifier = Modifier.height(9.dp)) // lägger till 16.db mellanrum mellan notes

        LazyColumn(            // lazyColumn visar listan av notes vertikalt
            verticalArrangement = Arrangement.spacedBy(9.dp),    // säkethetsställer att varje note är seperad med 9.db
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(notes) { index, note ->
                NoteItem(
                    note = note,
                    onClick = {
                        Log.d("MainScreen", "Navigating to note detail with index: $index")
                        navController.navigate("noteDetail/$index")
                    }
                )
            }
        }
    }
}

@Composable
fun NoteItem(note: Note, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = note.noteTitleLabel, style = MaterialTheme.typography.titleLarge)
            Text(text = note.noteDescriptLabel, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun NoteDetailScreen(navController: NavHostController, note: Note, noteIndex: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Title: ${note.noteTitleLabel}", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Description: ${note.noteDescriptLabel}", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))


        IconButton(
            onClick = { navController.navigate("editNote/$noteIndex") },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Note")
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Back to Main Screen")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Main Screen")
        }
    }
}

@Composable
fun CreateEditNoteScreen(
    navController: NavHostController,
    notes: MutableList<Note>,
    noteIndex: Int? = null
) {

    val note = noteIndex?.let { notes[it] }
    var noteTitleLabel by remember(note) { mutableStateOf(note?.noteTitleLabel ?: "") }
    var noteDescriptLabel by remember(note) { mutableStateOf(note?.noteDescriptLabel ?: "") }


    var titleLengthError by remember { mutableStateOf(false) }
    var descriptionLengthError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {

        TextField(
            value = noteTitleLabel,
            onValueChange = { title ->
                noteTitleLabel = title
                titleLengthError = title.isNotEmpty() && (title.length < 3 || title.length > 50)
            },
            label = { Text("Enter note title") },
            modifier = Modifier.fillMaxWidth(),
            isError = titleLengthError
        )

        if (titleLengthError) {
            Text(
                text = "Title must be between 3 and 50 characters",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))


        TextField(
            value = noteDescriptLabel,
            onValueChange = { description ->
                noteDescriptLabel = description
                descriptionLengthError = description.length > 120
            },
            label = { Text("Enter note description") },
            modifier = Modifier.fillMaxWidth(),
            isError = descriptionLengthError
        )

        if (descriptionLengthError) {
            Text(
                text = "description cannot exceed 120 characters",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = {
                if (!titleLengthError && !descriptionLengthError && noteTitleLabel.isNotEmpty() && noteDescriptLabel.isNotEmpty()) {
                    if (noteIndex == null) {

                        notes.add(Note(
                            noteTitleLabel = noteTitleLabel,
                            noteDescriptLabel = noteDescriptLabel
                        ))
                    } else {

                        notes[noteIndex] = Note(noteTitleLabel, noteDescriptLabel)
                    }
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !titleLengthError && !descriptionLengthError && noteTitleLabel.isNotEmpty() && noteDescriptLabel.isNotEmpty()
        ) {
            Icon(Icons.Default.Check, contentDescription = "Save")
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (noteIndex == null) "Create Note" else "Save Changes")
        }

        if (noteIndex != null) {
            Spacer(modifier = Modifier.height(16.dp))


            Button(
                onClick = {
                    notes.removeAt(noteIndex)
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Note")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val notes = remember { mutableStateListOf<Note>() }
    WillesNoteAppTheme {
        MainScreen(rememberNavController(), notes)
    }
}
