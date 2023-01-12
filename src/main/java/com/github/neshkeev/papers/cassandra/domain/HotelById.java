package com.github.neshkeev.papers.cassandra.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@SuppressWarnings("unused")
@Table(value = "hotels_by_id")
public class HotelById {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 0, name = "hotel_id")
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID hotelId;

    @Column("hotel_name")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String hotelName;

    @Column("stars")
    @CassandraType(type = CassandraType.Name.TINYINT)
    private StarsLevel stars;

    @Column("description")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String description;

    public HotelById() {
    }

    public HotelById(UUID hotelId, String hotelName, StarsLevel stars, String description) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.stars = stars;
        this.description = description;
    }

    public UUID getHotelId() {
        return hotelId;
    }

    public void setHotelId(UUID hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public StarsLevel getStars() {
        return stars;
    }

    public void setStars(StarsLevel stars) {
        this.stars = stars;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
