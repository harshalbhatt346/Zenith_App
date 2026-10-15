package com.zenith26.fest.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenith26.fest.ui.components.*
import com.zenith26.fest.ui.theme.*
import com.zenith26.fest.viewmodel.AdminViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Admin Top Navbar
            item {
                AdminTopNavHeader(
                    onNavigateBack = onNavigateBack,
                    onOpenSettings = onOpenSettings
                )
            }

            // 2. Dashboard Title & Action Buttons
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Operations Dashboard",
                                color = Slate900,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Live metrics, capacity alerts, and student registration control.",
                                color = Slate500,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Refresh Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(width = 1.dp, color = Slate200, shape = RoundedCornerShape(12.dp))
                                    .clickable { viewModel.loadDashboardData() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "🔄", fontSize = 13.sp)
                                    Text(
                                        text = "Refresh Data",
                                        color = Slate700,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Add Event Button (Stretch Goal)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BrandPurple600)
                                    .clickable { viewModel.openAddEventModal() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "➕", color = Color.White, fontSize = 12.sp)
                                    Text(
                                        text = "Add Event",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Stats Error Alert (if any)
            if (uiState.errorMessage != null) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Rose50)
                                .border(width = 1.dp, color = Rose200, shape = RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚠️ ${uiState.errorMessage}",
                                    color = Rose800,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Retry",
                                    color = Rose700,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.clickable { viewModel.loadDashboardData() }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Four Metric Stat Cards
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val stats = uiState.stats
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "Total Events",
                                value = stats?.totalEvents?.toString() ?: "--",
                                subtext = "Across 5 categories",
                                icon = "🎪",
                                iconBgColor = Color(0xFFEEF2FF),
                                iconBorderColor = Color(0xFFE0E7FF),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Total Registrations",
                                value = stats?.totalRegistrations?.toString() ?: "--",
                                subtext = "out of ${stats?.totalCapacity ?: "--"} seats",
                                icon = "👥",
                                iconBgColor = BrandPurple50,
                                iconBorderColor = BrandPurple100,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "Capacity Utilization",
                                value = stats?.let { "${it.overallUtilizationPct}%" } ?: "--%",
                                subtext = "Capacity fill rate",
                                icon = "📈",
                                iconBgColor = Emerald50,
                                iconBorderColor = Emerald100,
                                showProgressBar = true,
                                progressPct = stats?.overallUtilizationPct ?: 0.0,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Upcoming Events",
                                value = stats?.upcomingEventsCount?.toString() ?: "--",
                                subtext = "Nov 14–15, 2026",
                                icon = "⏳",
                                iconBgColor = Amber50,
                                iconBorderColor = Amber100,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Category Breakdown
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CategoryBreakdownSection(
                        categories = uiState.stats?.eventsByCategory ?: emptyMap()
                    )
                }
            }

            // 5. Tabs: Events Table vs Raw Registrations List
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(width = 1.dp, color = Slate200, shape = RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (uiState.selectedTab == 0) BrandPurple600 else Color.Transparent)
                                .clickable { viewModel.selectTab(0) }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Events Table",
                                color = if (uiState.selectedTab == 0) Color.White else Slate600,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (uiState.selectedTab == 1) BrandPurple600 else Color.Transparent)
                                .clickable { viewModel.selectTab(1) }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Registrations (${uiState.registrations.size})",
                                color = if (uiState.selectedTab == 1) Color.White else Slate600,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 6. Tab Content (Events Table or Registrations Table)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (uiState.selectedTab == 0) {
                        EventsTableView(events = uiState.events)
                    } else {
                        RegistrationsTableView(
                            registrations = uiState.registrations,
                            events = uiState.events,
                            selectedEventIdFilter = uiState.selectedEventIdFilter,
                            onFilterChanged = viewModel::setEventFilter,
                            onCancelRegistrationClick = viewModel::promptCancelRegistration
                        )
                    }
                }
            }

            // 7. Footer
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(width = 1.dp, color = Slate200, shape = RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Zenith '26 Coordinator Dashboard — Live SQLite & FastAPI Analytics",
                            color = Slate500,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "← Student Portal",
                                color = BrandPurple600,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { onNavigateBack() }
                            )
                            Text(text = "•", color = Slate400, fontSize = 11.sp)
                            Text(
                                text = "Swagger API Docs",
                                color = BrandPurple600,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Add Event Modal
        if (uiState.showAddEventModal) {
            AddEventModal(
                isSaving = uiState.isAddingEvent,
                errorMessage = uiState.addEventErrorMessage,
                onDismiss = viewModel::closeAddEventModal,
                onSaveEvent = viewModel::createEvent
            )
        }

        // Cancel Registration Modal
        uiState.pendingCancelRegistration?.let { reg ->
            CancelRegistrationModal(
                registration = reg,
                onDismiss = viewModel::dismissCancelModal,
                onConfirmCancel = viewModel::confirmCancelRegistration
            )
        }
    }
}
