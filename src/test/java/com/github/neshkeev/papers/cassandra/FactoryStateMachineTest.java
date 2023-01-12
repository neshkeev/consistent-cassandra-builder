package com.github.neshkeev.papers.cassandra;

import com.github.neshkeev.papers.cassandra.domain.HotelById;
import com.github.neshkeev.papers.cassandra.domain.HotelByStars;
import com.github.neshkeev.papers.cassandra.domain.HotelByStation;
import com.github.neshkeev.papers.cassandra.domain.StarsLevel;
import com.github.neshkeev.papers.cassandra.factories.HotelStateMachineFactory;
import com.github.neshkeev.papers.cassandra.factories.HotelStateMachineFactory.Builder;
import com.github.neshkeev.papers.cassandra.factories.HotelStateMachineFactory.Present;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class FactoryStateMachineTest extends AbstractContainerSetup {

    // region constants
    public static final UUID HOTEL_ID = UUID.fromString("176c39cd-b93d-33a5-a218-8eb06a56f66e");
    public static final StarsLevel STARS = StarsLevel.FIVE;
    public static final String METRO_STATION = "Беговая";
    public static final String HOTEL_NAME = "Гостиница Центральная";
    public static final String DESCRIPTION = "Просторный отель рядом с метро Дыбенко";
    // endregion

    @SuppressWarnings("SameParameterValue")
    protected List<Object> getEntities() {
        // region Initialize a Factory
        Builder<Present, Present, Present, Present> builder = HotelStateMachineFactory.builder()
                .setHotelId(HOTEL_ID)
                .setHotelName(HOTEL_NAME)
                .setDescription(DESCRIPTION)
                .setStars(STARS)
                .setStation(METRO_STATION);

        HotelStateMachineFactory hotelFactory = HotelStateMachineFactory.of(builder);
        // endregion

        // region Entities Creation
        HotelById hotelById = hotelFactory.createHotelById();
        HotelByStars hotelByStars = hotelFactory.createHotelByStars();
        HotelByStation hotelByStation = hotelFactory.createHotelByStation();
        // endregion

        return Arrays.asList(hotelById, hotelByStars, hotelByStation);
    }

    @Test
    public void testHotelById() {
        assertThat(hotelByIdRepo.existsByHotelId(HOTEL_ID))
                .withFailMessage("Hotel by Id should be exist")
                .isTrue();
    }

    @Test
    public void testHotelByStars() {
        assertThat(hotelByStarsRepo.existsByStars(STARS))
                .withFailMessage("Hotel by stars should be exist")
                .isTrue();
    }

    @Test
    public void testHotelByMetroStation() {
        assertThat(hotelByStationRepo.existsByStation(METRO_STATION))
                .withFailMessage("Hotel by metro should be exist")
                .isTrue();
    }
}
