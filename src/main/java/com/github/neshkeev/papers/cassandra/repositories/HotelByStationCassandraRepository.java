package com.github.neshkeev.papers.cassandra.repositories;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.github.neshkeev.papers.cassandra.domain.HotelByStation;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Consistency;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelByStationCassandraRepository extends CassandraRepository<HotelByStation, String> {

    @Consistency(DefaultConsistencyLevel.ONE)
    boolean existsByStation(String station);
}
