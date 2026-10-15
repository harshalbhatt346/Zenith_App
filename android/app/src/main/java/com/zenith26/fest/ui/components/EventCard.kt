package com.zenith26.fest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.ui.theme.*

fun getCategoryColors(category: String): Triple<Color, Color, Color> {
    return when (category.lowercase()) {
        "technical" -> Triple(Blue100, Blue800, Blue200)
        "cultural" -> Triple(BrandPurple100, BrandPurple800, Color(0xFFDDD6FE))
        "sports" -> Triple(Emerald100, Emerald800, Emerald200)
        "gaming" -> Triple(Rose100, Rose800, Rose200)
        "workshop" -> Triple(Amber100, Amber800, Amber200)
        else -> Triple(Slate100, Slate800, Slate200)
    }
}

@Composable
fun EventCard(
    event: Event,
    onViewDetailsClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFull = event.isFull
    val (catBg, catText, catBorder) = getCategoryColors(event.category)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(width = 1.dp, color = Slate200, shape = RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Status Bar Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(if (isFull) androidx.compose.ui.graphics.SolidColor(Rose500) else CardTopBarGradient)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header: Emoji Icon, Category Badge & FULL Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Slate50)
                            .border(width = 1.dp, color = Slate200, shape = RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = event.icon ?: "🎉", fontSize = 22.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(catBg)
                                .border(width = 1.dp, color = catBorder, shape = RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = event.category,
                                color = catText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // FULL Badge
                        if (isFull) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Rose600)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "FULL",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // Event Title & Description
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = event.title,
                        color = Slate900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 22.sp
                    )

                    Text(
                        text = event.description.takeIf { !it.isNullOrBlank() } ?: "No description provided.",
                        color = Slate500,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Event Meta Details: Date, Time & Venue
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "📅", fontSize = 12.sp)
                        Text(
                            text = "${event.formattedDate}, ${event.eventTime}",
                            color = Slate600,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "📍", fontSize = 12.sp)
                        Text(
                            text = event.venue,
                            color = Slate600,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Slate100)
                )

                // Footer: Seats, Fee & Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Seats Info
                    Column {
                        Text(
                            text = "SEATS",
                            color = Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        if (isFull) {
                            Text(
                                text = "0 / ${event.capacity}",
                                color = Rose600,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                buildAnnotatedString {
                                    withStyle(SpanStyle(color = BrandPurple600, fontWeight = FontWeight.Bold)) {
                                        append("${event.availableSeats}")
                                    }
                                    withStyle(SpanStyle(color = Slate700, fontWeight = FontWeight.Normal)) {
                                        append(" / ${event.capacity} left")
                                    }
                                },
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Fee Info
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "FEE",
                            color = Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = event.feeDisplay,
                            color = if (event.fee > 0) Slate900 else Emerald600,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // View Details Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isFull) Slate100 else BrandPurple600)
                            .clickable { onViewDetailsClick(event) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View Details",
                            color = if (isFull) Slate700 else Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
