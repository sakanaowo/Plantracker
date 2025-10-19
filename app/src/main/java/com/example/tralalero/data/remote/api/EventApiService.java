package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.event.EventDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface EventApiService {


    @GET("events")
    Call<List<EventDTO>> getEventsByProject(@Query("projectId") String projectId);


    @GET("events/{id}")
    Call<EventDTO> getEventById(@Path("id") String eventId);


    @POST("events")
    Call<EventDTO> createEvent(@Body EventDTO event);


    @PATCH("events/{id}")
    Call<EventDTO> updateEvent(
        @Path("id") String eventId,
        @Body EventDTO event
    );


    @DELETE("events/{id}")
    Call<Void> deleteEvent(@Path("id") String eventId);
}

