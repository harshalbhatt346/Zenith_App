package com.zenith26.fest.data.api

import com.zenith26.fest.data.model.CreateEventRequest
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.RegistrationListItem
import com.zenith26.fest.data.model.RegistrationRequest
import com.zenith26.fest.data.model.RegistrationResponse
import com.zenith26.fest.data.model.StatsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ZenithApiService {

    @GET("/api/events")
    suspend fun getEvents(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): List<Event>

    @GET("/api/events/{event_id}")
    suspend fun getEventDetail(
        @Path("event_id") eventId: Int
    ): Response<Event>

    @POST("/api/events/{event_id}/register")
    suspend fun registerForEvent(
        @Path("event_id") eventId: Int,
        @Body request: RegistrationRequest
    ): Response<RegistrationResponse>

    @GET("/api/registrations")
    suspend fun getRegistrations(
        @Query("event_id") eventId: Int? = null
    ): List<RegistrationListItem>

    @GET("/api/stats")
    suspend fun getStats(): StatsResponse

    // Stretch goal 1: Add new event
    @POST("/api/events")
    suspend fun createEvent(
        @Body request: CreateEventRequest
    ): Response<Event>

    // Stretch goal 2: Cancel registration
    @DELETE("/api/registrations/{registration_id}")
    suspend fun cancelRegistration(
        @Path("registration_id") registrationId: Int
    ): Response<Map<String, String>>
}
