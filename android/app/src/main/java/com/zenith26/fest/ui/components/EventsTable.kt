package com.zenith26.fest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.ui.theme.*

@Composable
fun EventsTableView(
    events: List<Event>,
    modifier: Modifier = Modifier
) {
    // Compute utilization % and sort descending
    val enrichedEvents = events.map { ev ->
        val registered = ev.capacity - ev.availableSeats
        val utilPct = if (ev.capacity > 0) (registered.toDouble() / ev.capacity.toDouble()) * 100.0 else 0.0
        EnrichedEvent(ev, registered, utilPct)
    }.sortedByDescending { it.utilizationPct }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(width = 1.dp, color = Slate200, shape = RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Event Utilization Rankings",
                        color = Slate900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Sorted by Capacity Utilization % (descending)",
                        color = Slate500,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Rose100)
                        .border(width = 1.dp, color = Rose200, shape = RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚠️ >90% Alert",
                        color = Rose800,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Slate200)
            )

            // Event Rows
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                enrichedEvents.forEachIndexed { index, item ->
                    val isOver90 = item.utilizationPct >= 90.0
                    val (catBg, catText, catBorder) = getCategoryColors(item.event.category)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isOver90) Rose50.copy(alpha = 0.7f) else if (index % 2 == 0) Color.White else Slate50)
                            .border(
                                width = if (isOver90) 1.dp else 0.dp,
                                color = if (isOver90) Rose200 else Color.Transparent
                            )
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Title row with Icon, ID, Category & Fee
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = item.event.icon ?: "🎉", fontSize = 20.sp)
                                    Column {
                                        Text(
                                            text = item.event.title,
                                            color = Slate900,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                        Text(
                                            text = "ID: #${item.event.id} • ${item.event.venue}",
                                            color = Slate400,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(catBg)
                                            .border(width = 1.dp, color = catBorder, shape = RoundedCornerShape(50))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.event.category,
                                            color = catText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Text(
                                        text = item.event.feeDisplay,
                                        color = if (item.event.fee > 0) Slate900 else Emerald600,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }

                            // Utilization & Capacity Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Registered:",
                                        color = Slate500,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "${item.registered} / ${item.event.capacity}",
                                        color = if (isOver90) Rose700 else Slate800,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(70.dp)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Slate200)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth((item.utilizationPct / 100.0).coerceIn(0.0, 1.0).toFloat())
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(50))
                                                .background(if (isOver90) Rose600 else BrandPurple600)
                                        )
                                    }
                                    Text(
                                        text = String.format("%.1f%%", item.utilizationPct),
                                        color = if (isOver90) Rose700 else Slate700,
                                        fontWeight = if (isOver90) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    if (isOver90) {
                                        Text(text = "⚠️", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (index < enrichedEvents.size - 1) {
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

private data class EnrichedEvent(
    val event: Event,
    val registered: Int,
    val utilizationPct: Double
)
