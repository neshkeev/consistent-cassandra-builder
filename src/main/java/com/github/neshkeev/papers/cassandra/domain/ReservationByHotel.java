package com.github.neshkeev.papers.cassandra.domain;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(value = "reservations_by_hotel")
public class ReservationByHotel {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 0, name = "hotel_id")
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID hotelId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING, ordinal = 1, name = "start")
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    private LocalDateTime startDate;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING, ordinal = 2, name = "end")
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    private LocalDateTime endDate;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 3, name = "room_id")
    @CassandraType(type = CassandraType.Name.SMALLINT)
    private int roomId;

    @Column("hotel_name")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String hotelName;

    @Column("username")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String username;

    public ReservationByHotel() {
    }

    public ReservationByHotel(UUID hotelId, LocalDateTime startDate, LocalDateTime endDate, int roomId, String hotelName, String username) {
        this.hotelId = hotelId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.roomId = roomId;
        this.hotelName = hotelName;
        this.username = username;
    }

    public UUID getHotelId() {
        return hotelId;
    }

    public void setHotelId(UUID hotelId) {
        this.hotelId = hotelId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}