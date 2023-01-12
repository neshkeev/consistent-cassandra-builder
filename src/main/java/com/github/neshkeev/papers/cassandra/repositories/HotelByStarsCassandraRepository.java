package com.github.neshkeev.papers.cassandra.repositories;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.github.neshkeev.papers.cassandra.domain.HotelByStars;
import com.github.neshkeev.papers.cassandra.domain.StarsLevel;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Consistency;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelByStarsCassandraRepository extends CassandraRepository<HotelByStars, StarsLevel> {

    @Consistency(DefaultConsistencyLevel.ONE)
    boolean existsByStars(StarsLevel stars);
}
