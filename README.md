# Consistent Builder API for Cassandra

## Overview

The project demonstrates an approach to organize a client's API to deal with duplicating data when working with Apache Cassandra

## Setup

1. Start cassandra: `docker compose up`;
1. Wait for cassandra to start;
1. Set up the cluster: `docker compose exec -it cassandra cqlsh --file="/mnt/scripts/setup.cql"`;
1. Set up the environment:
   - set the `CONTACT_POINT` environment variable, if cassandra is not on `localhost:9042` (default): `export CONTACT_POINT=localhost:9042`;
   - set the `KEYSPACE_NAME` environment variable, if the keyspace is not `CONSISTENT_BUILDER_DEMO_CLUSTER` (default): `export KEYSPACE_NAME=CONSISTENT_BUILDER_DEMO_CLUSTER`;
1. Run tests:
   - Direct approach: `mvn test -Dtest=com.github.neshkeev.papers.cassandra.DirectApproachTest`;
   - Factory Method + Builder: `mvn test -Dtest=com.github.neshkeev.papers.cassandra.FactoryTest`;
   - Factory Method + State Machine for Builder: `mvn test -Dtest=com.github.neshkeev.papers.cassandra.FactoryStateMachineTest`.
