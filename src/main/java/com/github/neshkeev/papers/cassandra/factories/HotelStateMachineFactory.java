package com.github.neshkeev.papers.cassandra.factories;

import com.github.neshkeev.papers.cassandra.domain.HotelById;
import com.github.neshkeev.papers.cassandra.domain.HotelByStars;
import com.github.neshkeev.papers.cassandra.domain.HotelByStation;

import java.util.UUID;

public class HotelStateMachineFactory {
    public interface State {}
    public interface Present extends State {}
    public interface Absent extends State {}

    private final int stars;
    private final String description;
    private final String hotelName;
    private final String station;
    private final UUID hotelId;

    public static HotelStateMachineFactory of(Builder<Present, Present, Present, Present> builder) {
        return new HotelStateMachineFactory(builder.stars, builder.description, builder.hotelName, builder.station, builder.hotelId);
    }

    public static Builder<Absent, Absent, Absent, Absent> builder() {
        return new Builder<>(-1, null, null, null, null);
    }

    public HotelById createHotelById() {
        return new HotelById(hotelId, hotelName, stars, description);
    }
    public HotelByStars createHotelByStars() {
        return new HotelByStars(stars, hotelName, hotelId);
    }
    public HotelByStation createHotelByStation() {
        return new HotelByStation(station, hotelName, hotelId);
    }

    private HotelStateMachineFactory(int stars, String description, String hotelName, String station, UUID hotelId) {
        this.stars = stars;
        this.description = description;
        this.hotelName = hotelName;
        this.station = station;
        this.hotelId = hotelId;
    }

    public static final class Builder<
            HOTEL_ID_STATE extends State,
            STARS_STATE extends State,
            STATION_STATE extends State,
            HOTEL_NAME_STATE extends State
            > {
        private final int stars;
        private final String description;
        private final String hotelName;
        private final UUID hotelId;
        private final String station;

        public Builder(int stars, String description, String hotelName, UUID hotelId, String station) {
            this.stars = stars;
            this.description = description;
            this.hotelName = hotelName;
            this.hotelId = hotelId;
            this.station = station;
        }

        public Builder<Present, STARS_STATE, STATION_STATE, HOTEL_NAME_STATE> setHotelId(UUID hotelId) {
            return new Builder<>(stars, description, hotelName, hotelId, station);
        }

        public Builder<HOTEL_ID_STATE, Present, STATION_STATE, HOTEL_NAME_STATE> setStars(int stars) {
            return new Builder<>(stars, description, hotelName, hotelId, station);
        }

        public Builder<HOTEL_ID_STATE, STARS_STATE, Present, HOTEL_NAME_STATE> setStation(String station) {
            return new Builder<>(stars, description, hotelName, hotelId, station);
        }

        public Builder<HOTEL_ID_STATE, STARS_STATE, STATION_STATE, Present> setHotelName(String hotelName) {
            return new Builder<>(stars, description, hotelName, hotelId, station);
        }

        public Builder<HOTEL_ID_STATE, STARS_STATE, STATION_STATE, HOTEL_NAME_STATE> setDescription(String description) {
            return new Builder<>(stars, description, hotelName, hotelId, station);
        }
    }
}
