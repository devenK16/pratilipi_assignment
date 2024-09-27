package com.example.pratilipi_assignment.presentation.ui.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@Composable
fun TaskListScreen(viewModel: TaskViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val showDialog by viewModel.showDialog.collectAsState()
    val taskToEdit by viewModel.taskToEdit.collectAsState()

    // Drag state
    var draggingTaskId by remember { mutableStateOf<Int?>(null) }
    var deltaY by remember { mutableStateOf(0f) }

    // List state for scrolling
    val listState = rememberLazyListState()

    // Calculate item height inside the composable scope
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 56.dp.toPx() }
    val swapThreshold = itemHeightPx / 3 // Threshold for swapping

    LaunchedEffect(tasks) {
        if (!isLoading) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .distinctUntilChanged()
                .collect { lastIndex ->
                    if (lastIndex != null && lastIndex >= tasks.size - 1) {
                        viewModel.loadNextPage()
                    }
                }
        }
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                    val isDragging = draggingTaskId == task.id

                    TaskItem(
                        task = task,
                        onCheckedChange = { updatedTask -> viewModel.updateTask(updatedTask) },
                        onClick = { viewModel.openEditTaskDialog(task) },
                        onMove = { fromIndex, toIndex ->
                            coroutineScope.launch {
                                viewModel.moveTask(fromIndex, toIndex)
                            }
                        },
                        isDragging = isDragging,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .zIndex(if (isDragging) 1f else 0f)
                            .offset {
                                if (isDragging) {
                                    IntOffset(x = 0, y = deltaY.toInt())
                                } else {
                                    IntOffset.Zero
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { draggingTaskId = task.id },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        deltaY += dragAmount.y

                                        val newIndex = calculateNewIndex(
                                            tasks.indexOfFirst { it.id == draggingTaskId },
                                            deltaY,
                                            itemHeightPx,
                                            tasks.size,
                                            swapThreshold
                                        )

                                        if (newIndex != null && newIndex != tasks.indexOfFirst { it.id == draggingTaskId }) {
                                            viewModel.moveTask(
                                                tasks.indexOfFirst { it.id == draggingTaskId },
                                                newIndex
                                            )
                                            deltaY = 0f
                                        }
                                    },
                                    onDragEnd = {
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

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
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

    val newIndex = when {
        delta > swapThreshold && currentIndex < totalItems - 1 -> currentIndex + 1 // Move down
        delta < -swapThreshold && currentIndex > 0 -> currentIndex - 1 // Move up
        else -> currentIndex
    }


    return newIndex.coerceIn(0, totalItems - 1)
}