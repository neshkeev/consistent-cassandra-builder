package com.github.neshkeev.papers.cassandra.repositories;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.github.neshkeev.papers.cassandra.domain.HotelById;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Consistency;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HotelByIdCassandraRepository extends CassandraRepository<HotelById, UUID> {

    @Consistency(DefaultConsistencyLevel.ONE)
    boolean existsByHotelId(UUID uuid);
}
