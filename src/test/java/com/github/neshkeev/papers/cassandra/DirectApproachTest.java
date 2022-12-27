package com.github.neshkeev.papers.cassandra;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.github.neshkeev.papers.cassandra.domain.HotelById;
import com.github.neshkeev.papers.cassandra.domain.HotelByStars;
import com.github.neshkeev.papers.cassandra.domain.HotelByStation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.InsertOptions;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class DirectApproachTest {

    @Autowired
    private CassandraOperations ops;

    @Test
    public void test() {
        List<Object> entities = getEntities(UUID.fromString("b37f3fb5-0b79-4313-baea-3c4d93a7f362"),
                "Гостиница Центральная",
                5,
                "Просторный отель рядом с метро Дыбенко",
                "Дыбенко");

        // region insertOptions - Insert Options
        final InsertOptions insertOptions = InsertOptions.builder()
                .consistencyLevel(ConsistencyLevel.QUORUM)
                .build();
        // endregion

        ops.batchOps(BatchType.LOGGED)
                .insert(entities, insertOptions)
                .execute();
    }

    @SuppressWarnings("SameParameterValue")
    private static List<Object> getEntities(UUID hotelId, String hotelName, int stars, String description, String station) {
        // region Entities Creation
        HotelById hotelById = new HotelById(hotelId, hotelName, stars, description);
        HotelByStars hotelByStars = new HotelByStars(stars, hotelName, hotelId);
        HotelByStation hotelByStation = new HotelByStation(station, hotelName, hotelId);
        // endregion

        return Arrays.asList(hotelById, hotelByStars, hotelByStation);
    }
}
