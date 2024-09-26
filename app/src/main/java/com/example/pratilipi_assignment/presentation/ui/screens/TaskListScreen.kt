package com.example.pratilipi_assignment.presentation.ui.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.pratilipi_assignment.domain.model.Task
import com.example.pratilipi_assignment.presentation.ui.components.TaskDialog
import com.example.pratilipi_assignment.presentation.ui.components.TaskItem
import com.example.pratilipi_assignment.presentation.viewmodel.TaskViewModel
import kotlinx.coroutines.launch



@Composable
fun TaskListScreen(viewModel: TaskViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val showDialog by viewModel.showDialog.collectAsState()
    val taskToEdit by viewModel.taskToEdit.collectAsState()

    // Drag state
    var draggingTaskId by remember { mutableStateOf<Int?>(null) }  // Use task ID for dragging
    var deltaY by remember { mutableStateOf(0f) } // Separate delta for Y-axis

    // List state for scrolling
    val listState = rememberLazyListState()

    // Calculate item height inside the composable scope
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 56.dp.toPx() }
    val swapThreshold = itemHeightPx / 3 // Threshold for swapping

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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
//                contentPadding = PaddingValues(16.dp)
            ) {
                itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->

                    // Check if the task is the one being dragged
                    val isDragging = draggingTaskId == task.id

                    // Log the current task being dragged
                    if (isDragging) {
                        Log.d("TaskDrag", "Dragging task: ${task.title}")
                    }

                    TaskItem(
                        task = task,
                        onCheckedChange = { updatedTask -> viewModel.updateTask(updatedTask) },
                        onClick = { viewModel.openEditTaskDialog(task) },
                        onMove = { fromIndex, toIndex ->
                            coroutineScope.launch {
                                viewModel.moveTask(fromIndex, toIndex)
                            }
                        },
                        modifier = Modifier
                            .padding(10.dp)
                            .zIndex(if (isDragging) 1f else 0f) // Only the dragged task gets the higher z-index
                            .offset {
                                if (isDragging) {
                                    IntOffset(x = 0, y = deltaY.toInt())
                                } else {
                                    IntOffset.Zero
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingTaskId = task.id
                                        Log.d(
                                            "TaskDrag",
                                            "Started dragging task: ${draggingTaskId}"
                                        )
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        deltaY += dragAmount.y

                                        // Calculate the new index based on the delta
                                        val newIndex = calculateNewIndex(
                                            tasks.indexOfFirst { it.id == draggingTaskId }, // Find the current index of the dragging task by ID
                                            deltaY,
                                            itemHeightPx,
                                            tasks.size,
                                            swapThreshold
                                        )

                                        if (newIndex != null && newIndex != tasks.indexOfFirst { it.id == draggingTaskId }) {
                                            Log.d(
                                                "TaskDrag",
                                                "Swapped task: ${task.title} to index $newIndex"
                                            )
                                            viewModel.moveTask(
                                                tasks.indexOfFirst { it.id == draggingTaskId },
                                                newIndex
                                            )
                                            deltaY = 0f // Reset delta after a swap
                                        }
                                    },
                                    onDragEnd = {
                                        Log.d(
                                            "TaskDrag",
                                            "Finished dragging task: ${draggingTaskId}"
                                        )
                                        draggingTaskId = null
                                        deltaY = 0f
                                    },
                                    onDragCancel = {
                                        draggingTaskId = null
                                        deltaY = 0f
                                    }
                                )
                            }
                    )
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
                            viewModel.updateTask(
                                taskToEdit!!.copy(
                                    title = title,
                                    subtitle = subtitle
                                )
                            )
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

fun calculateNewIndex(
    currentIndex: Int?,
    delta: Float,
    itemHeightPx: Float,
    totalItems: Int,
    swapThreshold: Float
): Int? {
    if (currentIndex == null) return null

    return when {
        delta > swapThreshold && currentIndex < totalItems - 1 -> currentIndex + 1 // Move down
        delta < -swapThreshold && currentIndex > 0 -> currentIndex - 1 // Move up
        else -> currentIndex
    }
}