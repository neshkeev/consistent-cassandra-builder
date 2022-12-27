package com.github.neshkeev.papers.cassandra;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.github.neshkeev.papers.cassandra.domain.HotelById;
import com.github.neshkeev.papers.cassandra.domain.HotelByStars;
import com.github.neshkeev.papers.cassandra.domain.HotelByStation;
import com.github.neshkeev.papers.cassandra.factories.HotelStateMachineFactory;
import com.github.neshkeev.papers.cassandra.factories.HotelStateMachineFactory.Builder;
import com.github.neshkeev.papers.cassandra.factories.HotelStateMachineFactory.Present;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.InsertOptions;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class FactoryStateMachineTest {

    @Autowired
    private CassandraOperations ops;

    @Test
    public void test() {
        List<Object> entities = getEntities(UUID.fromString("176c39cd-b93d-33a5-a218-8eb06a56f66e"),
                "Лахта Отель",
                5,
                "Отель бизнес-класса для тех кто ценит свое время. Внутри есть специальные пространства для деловых встреч, а так же крытый паркинг для ваших гостей",
                "Беговая");

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
        // region Initialize a Factory
        Builder<Present, Present, Present, Present> builder = HotelStateMachineFactory.builder()
                .setHotelId(hotelId)
                .setHotelName(hotelName)
                .setDescription(description)
                .setStars(stars)
                .setStation(station);

        HotelStateMachineFactory hotelFactory = HotelStateMachineFactory.of(builder);
        // endregion

        // region Entities Creation
        HotelById hotelById = hotelFactory.createHotelById();
        HotelByStars hotelByStars = hotelFactory.createHotelByStars();
        HotelByStation hotelByStation = hotelFactory.createHotelByStation();
        // endregion

        return Arrays.asList(hotelById, hotelByStars, hotelByStation);
    }
}
