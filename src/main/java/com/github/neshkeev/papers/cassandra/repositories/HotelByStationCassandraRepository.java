package com.github.neshkeev.papers.cassandra.repositories;

import com.github.neshkeev.papers.cassandra.domain.HotelByStation;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelByStationCassandraRepository extends CassandraRepository<HotelByStation, String> {}
