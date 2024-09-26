package com.example.pratilipi_assignment.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.pratilipi_assignment.domain.model.Task

//@Composable
//fun TaskItem(
//    task: Task,
//    onCheckedChange: (Task) -> Unit,
//    onClick: () -> Unit,
//    onMove: (Int, Int) -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
//            .clickable { onClick() }
//            .padding(16.dp)
//    ) {
//        Checkbox(
//            checked = task.isCompleted,
//            onCheckedChange = {
//                onCheckedChange(task.copy(isCompleted = it))
//            }
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Column(modifier = Modifier.weight(1f)) {
//            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
//            Text(text = task.subtitle, style = MaterialTheme.typography.bodyMedium)
//        }
//        Icon(
//            imageVector = Icons.Default.MoreVert,
//            contentDescription = "Drag Handle",
//            modifier = Modifier
//                .size(24.dp)
//                .pointerInput(Unit) {
//                    detectDragGestures { change, dragAmount ->
//
//                    }
//                }
//        )
//    }
//}

@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Task) -> Unit,
    onClick: () -> Unit,
    onStartDrag: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = {
                onCheckedChange(task.copy(isCompleted = it))
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
            Text(text = task.subtitle, style = MaterialTheme.typography.bodyMedium)
        }
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Drag Handle",
            modifier = Modifier
                .size(24.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { onStartDrag() }
                    )
                }
        )
    }
}


