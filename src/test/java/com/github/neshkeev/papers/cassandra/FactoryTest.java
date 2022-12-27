package com.github.neshkeev.papers.cassandra;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.github.neshkeev.papers.cassandra.domain.HotelById;
import com.github.neshkeev.papers.cassandra.domain.HotelByStars;
import com.github.neshkeev.papers.cassandra.domain.HotelByStation;
import com.github.neshkeev.papers.cassandra.factories.HotelFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.InsertOptions;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class FactoryTest {

    @Autowired
    private CassandraOperations ops;

    @Test
    public void test() {
        List<Object> entities = getEntities(UUID.fromString("892c7c3c-3bce-3d2a-a51e-726a3fc46c74"),
                "Невский Отель",
                4,
                "Уютный отель, расчитанный на небольшое колличество гостей обеспечит вас комфортным отдыхом",
                "Невский проспект");

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
        HotelFactory.Builder builder = HotelFactory.builder()
                .setHotelId(hotelId)
                .setHotelName(hotelName)
                .setDescription(description)
                .setStars(stars)
                .setStation(station);

        HotelFactory hotelFactory = builder.createHotelFactory();
        // endregion

        // region Entities Creation
        HotelById hotelById = hotelFactory.createHotelById();
        HotelByStars hotelByStars = hotelFactory.createHotelByStars();
        HotelByStation hotelByStation = hotelFactory.createHotelByStation();
        // endregion

        return Arrays.asList(hotelById, hotelByStars, hotelByStation);
    }
}
