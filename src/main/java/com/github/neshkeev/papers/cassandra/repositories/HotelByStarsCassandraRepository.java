package com.github.neshkeev.papers.cassandra.repositories;

import com.github.neshkeev.papers.cassandra.domain.HotelByStars;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelByStarsCassandraRepository extends CassandraRepository<HotelByStars, Integer> {}
