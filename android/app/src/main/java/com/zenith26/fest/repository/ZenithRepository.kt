package com.zenith26.fest.repository

import com.zenith26.fest.data.api.RetrofitClient
import com.zenith26.fest.data.model.CreateEventRequest
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.RegistrationListItem
import com.zenith26.fest.data.model.RegistrationRequest
import com.zenith26.fest.data.model.RegistrationResponse
import com.zenith26.fest.data.model.StatsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ZenithRepository {

    private val apiService get() = RetrofitClient.getApiService()

    suspend fun getEvents(category: String?, search: String?): Result<List<Event>> =
        withContext(Dispatchers.IO) {
            try {
                val catParam = if (category.isNullOrBlank() || category.equals("All", ignoreCase = true)) null else category
                val searchParam = if (search.isNullOrBlank()) null else search
                val events = apiService.getEvents(category = catParam, search = searchParam)
                Result.success(events)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getEventDetail(eventId: Int): Result<Event> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEventDetail(eventId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorDetail = RetrofitClient.parseErrorDetail(response.errorBody()?.string())
                    Result.failure(Exception(errorDetail))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun registerForEvent(
        eventId: Int,
        request: RegistrationRequest
    ): Result<RegistrationResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.registerForEvent(eventId, request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorDetail = RetrofitClient.parseErrorDetail(rawError)
                    Result.failure(Exception(errorDetail))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getRegistrations(eventId: Int?): Result<List<RegistrationListItem>> =
        withContext(Dispatchers.IO) {
            try {
                val regs = apiService.getRegistrations(eventId)
                Result.success(regs)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getStats(): Result<StatsResponse> =
        withContext(Dispatchers.IO) {
            try {
                val stats = apiService.getStats()
                Result.success(stats)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun createEvent(request: CreateEventRequest): Result<Event> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.createEvent(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorDetail = RetrofitClient.parseErrorDetail(response.errorBody()?.string())
                    Result.failure(Exception(errorDetail))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun cancelRegistration(registrationId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.cancelRegistration(registrationId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorDetail = RetrofitClient.parseErrorDetail(response.errorBody()?.string())
                    Result.failure(Exception(errorDetail))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
