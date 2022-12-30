CREATE KEYSPACE IF NOT EXISTS CONSISTENT_BUILDER_DEMO_CLUSTER
WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};