package com.zenith26.fest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zenith26.fest.data.model.CreateEventRequest
import com.zenith26.fest.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventModal(
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSaveEvent: (CreateEventRequest) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technical") }
    var icon by remember { mutableStateOf("⚡") }
    var eventDate by remember { mutableStateOf("2026-11-14") }
    var eventTime by remember { mutableStateOf("10:00 AM") }
    var venue by remember { mutableStateOf("") }
    var capacityText by remember { mutableStateOf("50") }
    var feeText by remember { mutableStateOf("0") }
    var description by remember { mutableStateOf("") }

    var localError by remember { mutableStateOf<String?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Technical", "Cultural", "Sports", "Gaming", "Workshop")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate900.copy(alpha = 0.6f))
                .clickable { onDismiss() }
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(width = 1.dp, color = Slate200, shape = RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ModalHeaderGradient)
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add New Fest Event",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    // Form Body
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val alertMsg = errorMessage ?: localError
                        if (!alertMsg.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Rose50)
                                    .border(width = 1.dp, color = Rose200, shape = RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "⚠️ $alertMsg",
                                    color = Rose800,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Title
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Event Title *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = { Text("e.g. Cloud Hackathon", fontSize = 12.sp, color = Slate400) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Category & Icon Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("Category *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                                ExposedDropdownMenuBox(
                                    expanded = categoryDropdownExpanded,
                                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = category,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { Text("▼", fontSize = 10.sp, color = Slate400) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = categoryDropdownExpanded,
                                        onDismissRequest = { categoryDropdownExpanded = false }
                                    ) {
                                        categories.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat, fontSize = 12.sp) },
                                                onClick = {
                                                    category = cat
                                                    categoryDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(0.6f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("Icon (Emoji)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                                OutlinedTextField(
                                    value = icon,
                                    onValueChange = { icon = it },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Date & Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("Date (YYYY-MM-DD) *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                                OutlinedTextField(
                                    value = eventDate,
                                    onValueChange = { eventDate = it },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("Time *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                                OutlinedTextField(
                                    value = eventTime,
                                    onValueChange = { eventTime = it },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Venue
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Venue Location *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                            OutlinedTextField(
                                value = venue,
                                onValueChange = { venue = it },
                                placeholder = { Text("e.g. Innovation Hall", fontSize = 12.sp, color = Slate400) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Capacity & Fee
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("Capacity (Seats) *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                                OutlinedTextField(
                                    value = capacityText,
                                    onValueChange = { capacityText = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("Fee (₹, 0 for free)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                                OutlinedTextField(
                                    value = feeText,
                                    onValueChange = { feeText = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Description
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Description", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = { Text("Brief overview...", fontSize = 12.sp, color = Slate400) },
                                maxLines = 3,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Save Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSaving) Slate300 else BrandPurple600)
                                .clickable(enabled = !isSaving) {
                                    val cap = capacityText.toIntOrNull() ?: 0
                                    val fee = feeText.toIntOrNull() ?: 0
                                    if (title.isBlank() || venue.isBlank() || eventDate.isBlank() || eventTime.isBlank()) {
                                        localError = "Title, venue, date, and time are required."
                                    } else if (cap <= 0) {
                                        localError = "Capacity must be greater than zero."
                                    } else {
                                        localError = null
                                        onSaveEvent(
                                            CreateEventRequest(
                                                title = title.trim(),
                                                category = category.trim(),
                                                description = description.trim(),
                                                eventDate = eventDate.trim(),
                                                eventTime = eventTime.trim(),
                                                venue = venue.trim(),
                                                capacity = cap,
                                                fee = fee,
                                                icon = icon.trim().ifEmpty { "⚡" }
                                            )
                                        )
                                    }
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isSaving) "Saving Event..." else "Save & Publish Event",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
