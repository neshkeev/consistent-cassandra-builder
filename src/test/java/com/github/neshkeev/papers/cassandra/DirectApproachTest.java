package com.github.neshkeev.papers.cassandra;

import com.github.neshkeev.papers.cassandra.domain.HotelById;
import com.github.neshkeev.papers.cassandra.domain.HotelByStars;
import com.github.neshkeev.papers.cassandra.domain.HotelByStation;
import com.github.neshkeev.papers.cassandra.domain.StarsLevel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class DirectApproachTest extends AbstractContainerSetup {

    // region constants
    public static final UUID HOTEL_ID = UUID.fromString("b37f3fb5-0b79-4313-baea-3c4d93a7f362");
    public static final StarsLevel STARS = StarsLevel.FIVE;
    public static final String METRO_STATION = "Дыбенко";
    public static final String HOTEL_NAME = "Гостиница Центральная";
    public static final String DESCRIPTION = "Просторный отель рядом с метро Дыбенко";
    // endregion

    protected List<Object> getEntities() {
        // region Entities Creation
        HotelById hotelById = new HotelById(HOTEL_ID, HOTEL_NAME, STARS, DESCRIPTION);
        HotelByStars hotelByStars = new HotelByStars(STARS, HOTEL_NAME, HOTEL_ID);
        HotelByStation hotelByStation = new HotelByStation(METRO_STATION, HOTEL_NAME, HOTEL_ID);
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
