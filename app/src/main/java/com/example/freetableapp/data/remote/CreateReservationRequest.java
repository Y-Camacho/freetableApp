package com.example.freetableapp.data.remote;

public class CreateReservationRequest {
    public String reservation_time;
    public int people;

    public CreateReservationRequest(String reservation_time, int people) {
        this.reservation_time = reservation_time;
        this.people = people;
    }
}

