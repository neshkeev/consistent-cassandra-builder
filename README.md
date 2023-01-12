[![Gitpod](https://img.shields.io/badge/Gitpod-908a85?logo=gitpod)](https://gitpod.io/#https://github.com/neshkeev/consistent-cassandra-builder)

# Consistent Builder API for Cassandra

## Overview

The project demonstrates an approach to organize a client's API to deal with duplicating data when working with Apache Cassandra

## Setup

### Testcontainers

By default, the test will use testcontainers to start cassandra, so the user is expected to simply run tests: `mvn test`:

- Direct approach: `mvn test -Dtest=com.github.neshkeev.papers.cassandra.DirectApproachTest`
- Factory Method + Builder: `mvn test -Dtest=com.github.neshkeev.papers.cassandra.FactoryTest`
- Factory Method + State Machine based Builder: `mvn test -Dtest=com.github.neshkeev.papers.cassandra.FactoryStateMachineTest`

**IMPORTANT**: Don't be alarmed with the `Cannot assign requested address` messages in the output, that is how the testcontainers library waits for the cassandra docker container to start

### External Cassandra

If the user is not able to use testcontainers, it possible to run the tests against an external instance of Cassandra. There is the -Duse.cassandra.in.testcontainers system property to control how Cassandra is accessed: when it's `false`, the external instance of Cassandra is used.

1. Start cassandra: `docker compose up`
1. Wait for cassandra to start. The `cassandra` container should be in the `healthy` state: `watch docker compose ps`
1. Set up the cluster: `docker compose exec -it cassandra cqlsh --file="/mnt/scripts/setup.cql"`
1. Set up the environment (skip if docker is on `localhost`):
   - set the `CONTACT_POINT` environment variable, if cassandra is not on `localhost:9042` (default): `export CONTACT_POINT=localhost:9042`
   - set the `KEYSPACE_NAME` environment variable, if the keyspace is not `CONSISTENT_BUILDER_DEMO_CLUSTER` (default): `export KEYSPACE_NAME=CONSISTENT_BUILDER_DEMO_CLUSTER`
1. Run tests:
   - Direct approach: `mvn test -Duse.cassandra.in.testcontainers=false -Dtest=com.github.neshkeev.papers.cassandra.DirectApproachTest`
   - Factory Method + Builder: `mvn test -Duse.cassandra.in.testcontainers=false -Dtest=com.github.neshkeev.papers.cassandra.FactoryTest`
   - Factory Method + State Machine based Builder: `mvn test -Duse.cassandra.in.testcontainers=false -Dtest=com.github.neshkeev.papers.cassandra.FactoryStateMachineTest`
