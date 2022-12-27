package com.github.neshkeev.papers.cassandra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@SpringBootApplication
@EnableCassandraRepositories(basePackages = "com.github.neshkeev.papers.cassandra")
public class CassandraDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CassandraDemoApplication.class, args);
    }
}
