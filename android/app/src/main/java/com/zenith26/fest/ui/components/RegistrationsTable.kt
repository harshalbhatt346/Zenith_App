package com.zenith26.fest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.RegistrationListItem
import com.zenith26.fest.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationsTableView(
    registrations: List<RegistrationListItem>,
    events: List<Event>,
    selectedEventIdFilter: Int?,
    onFilterChanged: (Int?) -> Unit,
    onCancelRegistrationClick: (RegistrationListItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    val filterLabel = if (selectedEventIdFilter == null) {
        "All Events (${registrations.size})"
    } else {
        val ev = events.find { it.id == selectedEventIdFilter }
        ev?.let { "${it.title} (#${it.id})" } ?: "Event #$selectedEventIdFilter"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(width = 1.dp, color = Slate200, shape = RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header & Filter Dropdown
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Student Registration Logs",
                            color = Slate900,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "All student admissions verified against SQLite",
                            color = Slate500,
                            fontSize = 11.sp
                        )
                    }
                }

                // Filter by Event Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Filter:",
                        color = Slate600,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown },
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate50)
                                .border(width = 1.dp, color = Slate200, shape = RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = filterLabel,
                                    color = Slate800,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(text = "▼", color = Slate400, fontSize = 10.sp)
                            }
                        }

                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Events", fontSize = 12.sp) },
                                onClick = {
                                    onFilterChanged(null)
                                    expandedDropdown = false
                                }
                            )
                            events.forEach { ev ->
                                DropdownMenuItem(
                                    text = { Text("${ev.title} (#${ev.id})", fontSize = 12.sp) },
                                    onClick = {
                                        onFilterChanged(ev.id)
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Slate200)
            )

            // Registrations List
            if (registrations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No student registrations found for this selection.",
                        color = Slate500,
                        fontSize = 12.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    registrations.forEachIndexed { index, reg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (index % 2 == 0) Color.White else Slate50)
                                .padding(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Student Name, ID, & Event badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "#${reg.id}",
                                            color = Slate400,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = reg.studentName,
                                            color = Slate900,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                        Text(
                                            text = "(${reg.rollNumber})",
                                            color = Slate500,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Cancel Button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Rose50)
                                            .border(width = 1.dp, color = Rose200, shape = RoundedCornerShape(8.dp))
                                            .clickable { onCancelRegistrationClick(reg) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Cancel",
                                            color = Rose700,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                // Event Title Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BrandPurple50)
                                        .border(width = 1.dp, color = BrandPurple100, shape = RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "🎪 ${reg.eventTitle}",
                                        color = BrandPurple700,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }

                                // Contact & Timestamp
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${reg.email} • ${reg.phone ?: "No phone"}",
                                        color = Slate500,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = reg.registeredAt,
                                        color = Slate400,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        if (index < registrations.size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Slate100)
                            )
                        }
                    }
                }
            }
        }
    }
}
