package com.github.neshkeev.papers.cassandra.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table(value = "room_by_hotel")
public class RoomByHotel {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 0, name = "hotel_id")
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID hotelId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 1, name = "room_id")
    @CassandraType(type = CassandraType.Name.SMALLINT)
    private int roomId;

    @Column("hotel_name")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String hotelName;

    public RoomByHotel() {
    }

    public RoomByHotel(UUID hotelId, int roomId, String hotelName) {
        this.hotelId = hotelId;
        this.roomId = roomId;
        this.hotelName = hotelName;
    }

    public UUID getHotelId() {
        return hotelId;
    }

    public void setHotelId(UUID hotelId) {
        this.hotelId = hotelId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }
}
