package com.example.pratilipi_assignment.presentation.ui.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.pratilipi_assignment.domain.model.Task
import com.example.pratilipi_assignment.presentation.ui.components.TaskDialog
import com.example.pratilipi_assignment.presentation.ui.components.TaskItem
import com.example.pratilipi_assignment.presentation.viewmodel.TaskViewModel

@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun TaskListScreen(viewModel: TaskViewModel = hiltViewModel()) {
//    val tasks = viewModel.tasks.collectAsLazyPagingItems()
//    val showDialog by viewModel.showDialog.collectAsState()
//    val taskToEdit by viewModel.taskToEdit.collectAsState()
//
//    val coroutineScope = rememberCoroutineScope()
//
//    // Drag-and-Drop State
//    val listState = rememberLazyListState()
//
//    Scaffold(
//        floatingActionButton = {
//            FloatingActionButton(onClick = { viewModel.openAddTaskDialog() }) {
//                Icon(Icons.Default.Add, contentDescription = "Add Task")
//            }
//        }
//    ) { paddingValues ->
//        Box(modifier = Modifier.padding(paddingValues)) {
//            LazyColumn(
//                state = listState,
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(16.dp)
//            ) {
//                items(
//                    count = tasks.itemCount,
//                    key = { index -> tasks[index]?.id ?: index }
//                ) { index ->
//                    val task = tasks[index]
//                    task?.let {
//                        TaskItem(
//                            task = it,
//                            onCheckedChange = { updatedTask ->
//                                viewModel.updateTask(updatedTask)
//                            },
//                            onClick = { viewModel.openEditTaskDialog(it) },
//                            onMove = { from, to ->
//                                coroutineScope.launch {
//
//                                }
//                            }
//                        )
//                    }
//                }
//            }
//
//            if (showDialog) {
//                TaskDialog(
//                    task = taskToEdit,
//                    onDismiss = { viewModel.closeDialog() },
//                    onConfirm = { title, subtitle ->
//                        if (taskToEdit == null) {
//                            viewModel.addTask(title, subtitle)
//                        } else {
//                            viewModel.updateTask(taskToEdit!!.copy(title = title, subtitle = subtitle))
//                        }
//                    },
//                    onDelete = {
//                        taskToEdit?.let { viewModel.deleteTask(it) }
//                    }
//                )
//            }
//        }
//    }
//}
//

@Composable
fun TaskListScreen(viewModel: TaskViewModel = hiltViewModel()) {
    val tasks = viewModel.tasks.collectAsLazyPagingItems()
    val showDialog by viewModel.showDialog.collectAsState()
    val taskToEdit by viewModel.taskToEdit.collectAsState()
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var draggedItem by remember { mutableStateOf<Task?>(null) }
    var draggedOverItem by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(refreshTrigger) {
        tasks.refresh()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddTaskDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(
                    count = tasks.itemCount,
                    key = { index -> tasks[index]?.id ?: index }
                ) { index ->
                    val task = tasks[index]
                    task?.let {
                        val dragging = it.id == draggedItem?.id
                        val draggedOver = it.id == draggedOverItem?.id

                        TaskItem(
                            task = it,
                            onCheckedChange = { updatedTask ->
                                viewModel.updateTask(updatedTask)
                            },
                            onClick = { viewModel.openEditTaskDialog(it) },
                            onStartDrag = {
                                draggedItem = it
                            },
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDrag = { change, _ ->
                                            change.consumeAllChanges()
                                            draggedOverItem = it
                                        },
                                        onDragEnd = {
                                            if (draggedItem != null && draggedOverItem != null) {
                                                val fromIndex = tasks.itemSnapshotList.indexOf(draggedItem)
                                                val toIndex = tasks.itemSnapshotList.indexOf(draggedOverItem)
                                                if (fromIndex != -1 && toIndex != -1) {
                                                    val updatedTasks = tasks.itemSnapshotList.toMutableList()
                                                    val movedItem = updatedTasks.removeAt(fromIndex)
                                                    updatedTasks.add(toIndex, movedItem)
                                                    viewModel.reorderTasks(updatedTasks)
                                                    Log.d("TaskReorder", "Reordering from $fromIndex to $toIndex")
                                                }
                                            }
                                            draggedItem = null
                                            draggedOverItem = null
                                        }
                                    )
                                }
                                .graphicsLayer {
                                    if (dragging) {
                                        scaleX = 1.05f
                                        scaleY = 1.05f
                                        alpha = 0.9f
                                    }
                                }
                                .background(
                                    when {
                                        dragging -> Color.LightGray.copy(alpha = 0.5f)
                                        draggedOver -> Color.LightGray.copy(alpha = 0.3f)
                                        else -> Color.Transparent
                                    }
                                )
                        )
                    }
                }
            }

            if (showDialog) {
                TaskDialog(
                    task = taskToEdit,
                    onDismiss = { viewModel.closeDialog() },
                    onConfirm = { title, subtitle ->
                        if (taskToEdit == null) {
                            viewModel.addTask(title, subtitle)
                        } else {
                            viewModel.updateTask(taskToEdit!!.copy(title = title, subtitle = subtitle))
                        }
                    },
                    onDelete = {
                        taskToEdit?.let { viewModel.deleteTask(it) }
                    }
                )
            }
        }
    }
}
