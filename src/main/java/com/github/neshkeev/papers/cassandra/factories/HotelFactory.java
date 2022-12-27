package com.github.neshkeev.papers.cassandra.factories;

import com.github.neshkeev.papers.cassandra.domain.HotelById;
import com.github.neshkeev.papers.cassandra.domain.HotelByStars;
import com.github.neshkeev.papers.cassandra.domain.HotelByStation;

import java.util.UUID;

public final class HotelFactory {
    private final int stars;
    private final String description;
    private final String hotelName;
    private final String station;
    private final UUID hotelId;

    private HotelFactory(final int stars,
                         final String description,
                         final String hotelName,
                         final String station,
                         final UUID hotelId) {
        this.stars = stars;
        this.description = description;
        this.hotelName = hotelName;
        this.station = station;
        this.hotelId = hotelId;
    }

    public static Builder builder() {
        return new Builder();
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

    public static final class Builder {
        private int stars;
        private String description;
        private String hotelName;
        private String station;
        private UUID hotelId;

        private Builder() {
        }

        public Builder setStars(int stars) {
            this.stars = stars;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setHotelName(String hotelName) {
            this.hotelName = hotelName;
            return this;
        }

        public Builder setStation(String station) {
            this.station = station;
            return this;
        }

        public Builder setHotelId(UUID hotelId) {
            this.hotelId = hotelId;
            return this;
        }

        public HotelFactory createHotelFactory() {
            return new HotelFactory(stars, description, hotelName, station, hotelId);
        }
    }
}
