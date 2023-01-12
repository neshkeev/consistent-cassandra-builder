package com.github.neshkeev.papers.cassandra.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@SuppressWarnings("unused")
@Table(value = "hotels_by_stars")
public class HotelByStars {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 0, name = "stars")
    @CassandraType(type = CassandraType.Name.TINYINT)
    private StarsLevel stars;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 1, name = "hotel_name")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String hotelName;

    @CassandraType(type = CassandraType.Name.UUID)
    @Column("hotel_id")
    private UUID hotelId;

    public HotelByStars() {
    }

    public HotelByStars(StarsLevel stars, String hotelName, UUID hotelId) {
        this.stars = stars;
        this.hotelName = hotelName;
        this.hotelId = hotelId;
    }

    public StarsLevel getStars() {
        return stars;
    }

    public void setStars(StarsLevel stars) {
        this.stars = stars;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public UUID getHotelId() {
        return hotelId;
    }

    public void setHotelId(UUID hotelId) {
        this.hotelId = hotelId;
    }
}
