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
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.ui.theme.*

@Composable
fun EventDetailModal(
    event: Event,
    isSubmitting: Boolean,
    submissionSuccessMessage: String?,
    submissionErrorMessage: String?,
    onDismiss: () -> Unit,
    onSubmitRegistration: (name: String, roll: String, email: String, phone: String?) -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var errorName by remember { mutableStateOf<String?>(null) }
    var errorRoll by remember { mutableStateOf<String?>(null) }
    var errorEmail by remember { mutableStateOf<String?>(null) }

    val isFull = event.isFull

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
                    .wrapContentHeight()
                    .clickable(enabled = false) {} // Prevent dismiss on modal click
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(width = 1.dp, color = Slate200, shape = RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Modal Header with Gradient
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ModalHeaderGradient)
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = event.icon ?: "🎉", fontSize = 28.sp)
                            Column {
                                Text(
                                    text = event.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    lineHeight = 22.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = event.category,
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

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

                    // Modal Scrollable Body
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 520.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Quick Meta Info Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Slate50)
                                .border(width = 1.dp, color = Slate200, shape = RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DATE & TIME",
                                    color = Slate400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${event.formattedDate}, ${event.eventTime}",
                                    color = Slate800,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "VENUE",
                                    color = Slate400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = event.venue,
                                    color = Slate800,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "ENTRY FEE",
                                    color = Slate400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = event.feeDisplay,
                                    color = if (event.fee > 0) Slate900 else Emerald600,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Seats Availability Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isFull) Rose50 else BrandPurple50)
                                .border(
                                    width = 1.dp,
                                    color = if (isFull) Rose200 else BrandPurple100,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Available Capacity",
                                    color = if (isFull) Rose800 else BrandPurple800,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (isFull) {
                                    Text(
                                        text = "FULL • 0 of ${event.capacity} seats remaining",
                                        color = Rose600,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "${event.availableSeats} of ${event.capacity} seats remaining",
                                        color = BrandPurple700,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // About This Event Description
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "ABOUT THIS EVENT",
                                color = Slate400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = event.description.takeIf { !it.isNullOrBlank() }
                                    ?: "No full description provided.",
                                color = Slate600,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp
                            )
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Slate100)
                        )

                        // Registration Form Section
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Register for this Event",
                                color = Slate800,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Provide your details to claim a registration seat.",
                                color = Slate500,
                                fontSize = 11.5.sp
                            )

                            // Status Alert (Success or Server Error)
                            if (!submissionSuccessMessage.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Emerald50)
                                        .border(width = 1.dp, color = Emerald200, shape = RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = submissionSuccessMessage,
                                        color = Emerald800,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            if (!submissionErrorMessage.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Rose50)
                                        .border(width = 1.dp, color = Rose200, shape = RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "⚠️ $submissionErrorMessage",
                                        color = Rose800,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Input 1: Student Full Name
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Student Full Name *",
                                    color = Slate700,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                OutlinedTextField(
                                    value = studentName,
                                    onValueChange = {
                                        studentName = it
                                        if (it.isNotBlank()) errorName = null
                                    },
                                    placeholder = { Text("e.g. Priya Sharma", fontSize = 12.5.sp, color = Slate400) },
                                    isError = errorName != null,
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandPurple600,
                                        unfocusedBorderColor = Slate200,
                                        errorBorderColor = Rose500
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (errorName != null) {
                                    Text(
                                        text = errorName!!,
                                        color = Rose600,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Input 2: Roll Number
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Roll / ID Number *",
                                    color = Slate700,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                OutlinedTextField(
                                    value = rollNumber,
                                    onValueChange = {
                                        rollNumber = it
                                        if (it.isNotBlank()) errorRoll = null
                                    },
                                    placeholder = { Text("e.g. 2026CS104", fontSize = 12.5.sp, color = Slate400) },
                                    isError = errorRoll != null,
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandPurple600,
                                        unfocusedBorderColor = Slate200,
                                        errorBorderColor = Rose500
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (errorRoll != null) {
                                    Text(
                                        text = errorRoll!!,
                                        color = Rose600,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Input 3: Phone Number (Optional)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Phone Number (Optional)",
                                    color = Slate700,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    placeholder = { Text("e.g. +91 9876543210", fontSize = 12.5.sp, color = Slate400) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandPurple600,
                                        unfocusedBorderColor = Slate200
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Input 4: College Email Address *
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "College Email Address *",
                                    color = Slate700,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = {
                                        email = it
                                        if (it.isNotBlank() && it.contains("@") && it.contains(".")) errorEmail = null
                                    },
                                    placeholder = { Text("e.g. priya@college.edu", fontSize = 12.5.sp, color = Slate400) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    isError = errorEmail != null,
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandPurple600,
                                        unfocusedBorderColor = Slate200,
                                        errorBorderColor = Rose500
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (errorEmail != null) {
                                    Text(
                                        text = errorEmail!!,
                                        color = Rose600,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Confirm Registration Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isFull || isSubmitting) Slate200 else BrandPurple600
                                    )
                                    .clickable(enabled = !isFull && !isSubmitting) {
                                        // Client-side validation matching web app
                                        var hasError = false
                                        if (studentName.trim().isEmpty()) {
                                            errorName = "Please enter your full name."
                                            hasError = true
                                        }
                                        if (rollNumber.trim().isEmpty()) {
                                            errorRoll = "Please enter your college roll / student ID."
                                            hasError = true
                                        }
                                        if (email.trim().isEmpty()) {
                                            errorEmail = "Please enter your email address."
                                            hasError = true
                                        } else if (!email.contains("@") || !email.contains(".")) {
                                            errorEmail = "Please enter a valid email address with '@' and domain."
                                            hasError = true
                                        }

                                        if (!hasError) {
                                            onSubmitRegistration(
                                                studentName.trim(),
                                                rollNumber.trim(),
                                                email.trim(),
                                                phone.trim().takeIf { it.isNotEmpty() }
                                            )
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(
                                            color = BrandPurple600,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Submitting registration...",
                                            color = Slate600,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else if (isFull) {
                                        Text(
                                            text = "Registration Closed (Capacity Full)",
                                            color = Slate500,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        Text(
                                            text = "Confirm Registration",
                                            color = Color.White,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
