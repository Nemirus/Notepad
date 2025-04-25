@file:OptIn(ExperimentalMaterial3Api::class)
package com.nemirus.notepad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import com.nemirus.notepad.ui.theme.MyComposeApplicationTheme
import java.util.*

data class Note(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var content: String = "",
    var category: String = "All"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyComposeApplicationTheme {
                NotepadApp()
            }
        }
    }
}

@Composable
fun NotepadApp() {
    val notes = remember {
        mutableStateListOf(
            Note(title = "Buy groceries", content = "Milk, Eggs, Bread", category = "Personal"),
            Note(title = "Read docs", content = "Jetpack Compose + Kotlin DSL", category = "Work"),
            Note(title = "Plan vacation", content = "Hawaii, July", category = "Personal")
        )
    }

    var selectedNoteId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredNotes = notes.filter {
        (it.title.contains(searchQuery.text, ignoreCase = true) || it.content.contains(searchQuery.text, ignoreCase = true)) &&
                (it.category == selectedCategory || selectedCategory == "All")
    }

    val selectedNote = selectedNoteId?.let { id -> notes.find { it.id == id } }

    if (selectedNote != null) {
        NoteDetail(
            note = selectedNote,
            onBack = { selectedNoteId = null },
            onDelete = {
                notes.removeIf { it.id == selectedNote.id }
                selectedNoteId = null
            }
        )
    } else {
        NotepadHome(
            notes = filteredNotes,
            onNoteClick = { selectedNoteId = it.id },
            onAddNote = {
                val newNote = Note(title = "Untitled", content = "", category = "All")
                notes.add(0, newNote)
                selectedNoteId = newNote.id
            },
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            selectedCategory = selectedCategory,
            onCategoryChange = { selectedCategory = it }
        )
    }
}

@Composable
fun NotepadHome(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onAddNote: () -> Unit,
    searchQuery: TextFieldValue,
    onSearchChange: (TextFieldValue) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Notepad") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNote) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                label = { Text("Search Notes") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Tabs for Categories
            TabRow(selectedTabIndex = when (selectedCategory) {
                "Personal" -> 1
                "Work" -> 2
                else -> 0
            }) {
                Tab(
                    text = { Text("All") },
                    selected = selectedCategory == "All",
                    onClick = { onCategoryChange("All") }
                )
                Tab(
                    text = { Text("Personal") },
                    selected = selectedCategory == "Personal",
                    onClick = { onCategoryChange("Personal") }
                )
                Tab(
                    text = { Text("Work") },
                    selected = selectedCategory == "Work",
                    onClick = { onCategoryChange("Work") }
                )
            }

            // Notes List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    NoteCard(note = note, onClick = { onNoteClick(note) })
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = note.title, style = MaterialTheme.typography.titleMedium)
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content.take(100),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Category: ${note.category}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun NoteDetail(note: Note, onBack: () -> Unit, onDelete: () -> Unit) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    var category by remember { mutableStateOf(note.category) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        note.title = title.trim()
                        note.content = content.trim()
                        note.category = category.trim()
                        onBack()
                    }) {
                        Text("Save")
                    }
                    TextButton(onClick = { onDelete() }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Note Content") },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                maxLines = Int.MAX_VALUE
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotepadHomePreview() {
    MyComposeApplicationTheme {
        NotepadHome(
            notes = listOf(
                Note(title = "Preview Note", content = "This is just a preview.", category = "Personal"),
            ),
            onNoteClick = {},
            onAddNote = {},
            searchQuery = TextFieldValue(""),
            onSearchChange = {},
            selectedCategory = "All",
            onCategoryChange = {}
        )
    }
}