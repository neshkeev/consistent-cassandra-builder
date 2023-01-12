package com.github.neshkeev.papers.cassandra;

import com.github.neshkeev.papers.cassandra.domain.StarsLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.convert.CassandraCustomConversions;

import java.util.Collections;

@Configuration
public class CassandraConfig {

    @Bean
    public CassandraCustomConversions customConversions() {
        return new CassandraCustomConversions(Collections.singletonList(new StarsLevel.StarsLevelToByteConverter()));
    }
}
