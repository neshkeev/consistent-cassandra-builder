package com.github.neshkeev.papers.cassandra;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;

public class CassandraContainer extends GenericContainer<CassandraContainer> {
    public static final int CQL_NATIVE_TRANSPORT_PORT = 9042;
    private String[] cmd;

    public CassandraContainer() {
        super(DockerImageName.parse("cassandra:4.0"));
        getWaitStrategy().withStartupTimeout(Duration.ofMinutes(5));
    }

    public String getLocalDatacenter() {
        return "datacenter1";
    }

    public String getContactPoint() {
        return getHost() + ":" + getMappedPort(CQL_NATIVE_TRANSPORT_PORT);
    }

    public CassandraContainer withInitCmd(String cmd) {
        this.cmd = cmd.split(" ");
        return self();
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        try {
            execInContainer(cmd);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
