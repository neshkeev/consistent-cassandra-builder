package com.github.neshkeev.papers.cassandra;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.github.neshkeev.papers.cassandra.repositories.HotelByIdCassandraRepository;
import com.github.neshkeev.papers.cassandra.repositories.HotelByStarsCassandraRepository;
import com.github.neshkeev.papers.cassandra.repositories.HotelByStationCassandraRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.List;

public abstract class AbstractContainerSetup {
    @Autowired
    protected CassandraOperations ops;

    @Autowired
    protected HotelByIdCassandraRepository hotelByIdRepo;
    @Autowired
    protected HotelByStationCassandraRepository hotelByStationRepo;
    @Autowired
    protected HotelByStarsCassandraRepository hotelByStarsRepo;

    public static final Logger LOGGER = LoggerFactory.getLogger(AbstractContainerSetup.class);

    public static final String SETUP_SCRIPT = "/mnt/scripts/setup.cql";

    public static CassandraContainer cassandra = new CassandraContainer()
            .withLogConsumer(new Slf4jLogConsumer(LOGGER))
            .withFileSystemBind(System.getProperty("user.dir") + "/scripts/setup.cql", SETUP_SCRIPT, BindMode.READ_ONLY)
            .withExposedPorts(com.github.neshkeev.papers.cassandra.CassandraContainer.CQL_NATIVE_TRANSPORT_PORT)
            .withInitCmd("cqlsh -f " + SETUP_SCRIPT);

    @BeforeAll
    public static void before() {
        if (Boolean.parseBoolean(System.getProperty("use.cassandra.in.testcontainers", "true"))) {
            cassandra.start();
        }
    }

    protected abstract List<Object> getEntities();

    @BeforeEach
    public void beforeTest() {
        List<Object> entities = getEntities();

        // region insertOptions - Insert Options
        final InsertOptions insertOptions = InsertOptions.builder()
                .consistencyLevel(ConsistencyLevel.QUORUM)
                .build();
        // endregion

        ops.batchOps(BatchType.LOGGED)
                .insert(entities, insertOptions)
                .execute();
    }

    @DynamicPropertySource
    static void cassandraProperties(DynamicPropertyRegistry registry) {
        if (!cassandra.isRunning()) {
            LOGGER.warn("The container has not been started");
            return;
        }

        registry.add("spring.data.cassandra.contact-points", cassandra::getContactPoint);
        registry.add("spring.data.cassandra.local-datacenter", cassandra::getLocalDatacenter);
    }
}