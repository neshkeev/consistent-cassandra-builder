package com.github.neshkeev.papers.cassandra.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@SuppressWarnings("unused")
@Table(value = "hotels_by_metro")
public class HotelByStation {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 0, name = "station_name")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String station;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 1, name = "hotel_name")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String hotelName;

    @Column("hotel_id")
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID hotelId;

    public HotelByStation() {
    }

    public HotelByStation(String station, String hotelName, UUID hotelId) {
        this.station = station;
        this.hotelName = hotelName;
        this.hotelId = hotelId;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
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
