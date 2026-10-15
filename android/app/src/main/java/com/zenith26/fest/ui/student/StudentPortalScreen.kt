package com.zenith26.fest.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.zenith26.fest.viewmodel.StudentViewModel

@Composable
fun StudentPortalScreen(
    viewModel: StudentViewModel,
    onNavigateToAdmin: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Navbar
            item {
                StudentTopNavHeader(
                    onNavigateToAdmin = onNavigateToAdmin,
                    onOpenSettings = onOpenSettings
                )
            }

            // 2. Hero Section
            item {
                HeroSection()
            }

            // 3. Search and Category Filter Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SearchAndFilterCard(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = viewModel::onSearchQueryChanged,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = viewModel::onCategorySelected,
                        eventsCount = uiState.events.size,
                        isLoading = uiState.isLoading
                    )
                }
            }

            // 4. Content Area (Loading / Error / Empty / Events Grid)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    when {
                        uiState.isLoading && uiState.events.isEmpty() -> {
                            LoadingView()
                        }
                        uiState.errorMessage != null && uiState.events.isEmpty() -> {
                            ErrorView(
                                errorMessage = uiState.errorMessage!!,
                                onRetry = viewModel::loadEvents
                            )
                        }
                        uiState.events.isEmpty() -> {
                            EmptyEventsView(
                                searchQuery = uiState.searchQuery,
                                category = uiState.selectedCategory,
                                onResetFilters = viewModel::resetFilters
                            )
                        }
                        else -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                uiState.events.forEach { event ->
                                    EventCard(
                                        event = event,
                                        onViewDetailsClick = viewModel::openEventDetail
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Footer
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
                            text = "Zenith '26 Fest System — Built with FastAPI, SQLite & Android Kotlin",
                            color = Slate500,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fest Admin Dashboard",
                                color = BrandPurple600,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { onNavigateToAdmin() }
                            )
                            Text(text = "•", color = Slate400, fontSize = 11.sp)
                            Text(
                                text = "API Docs (/docs)",
                                color = BrandPurple600,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Event Detail & Registration Modal Dialog
        uiState.selectedEventForDetail?.let { activeEvent ->
            EventDetailModal(
                event = activeEvent,
                isSubmitting = uiState.isRegistering,
                submissionSuccessMessage = uiState.registrationSuccessMessage,
                submissionErrorMessage = uiState.registrationErrorMessage,
                onDismiss = viewModel::closeEventDetail,
                onSubmitRegistration = { name, roll, email, phone ->
                    viewModel.submitRegistration(name, roll, email, phone)
                }
            )
        }
    }
}
