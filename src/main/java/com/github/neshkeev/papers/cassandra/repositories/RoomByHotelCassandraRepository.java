package com.github.neshkeev.papers.cassandra.repositories;

import com.github.neshkeev.papers.cassandra.domain.RoomByHotel;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomByHotelCassandraRepository extends CassandraRepository<RoomByHotel, UUID> {}
