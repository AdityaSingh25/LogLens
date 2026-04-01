#!/bin/bash
set -e

BOOTSTRAP_SERVER="localhost:9092"

echo "Creating Kafka topics..."

kafka-topics.sh --create --if-not-exists \
  --topic raw-logs \
  --partitions 12 \
  --replication-factor 1 \
  --bootstrap-server $BOOTSTRAP_SERVER

kafka-topics.sh --create --if-not-exists \
  --topic parsed-logs \
  --partitions 12 \
  --replication-factor 1 \
  --bootstrap-server $BOOTSTRAP_SERVER

kafka-topics.sh --create --if-not-exists \
  --topic embedding-results \
  --partitions 6 \
  --replication-factor 1 \
  --bootstrap-server $BOOTSTRAP_SERVER

kafka-topics.sh --create --if-not-exists \
  --topic alert-events \
  --partitions 6 \
  --replication-factor 1 \
  --bootstrap-server $BOOTSTRAP_SERVER

# Dead Letter Topics
kafka-topics.sh --create --if-not-exists \
  --topic raw-logs.DLT \
  --partitions 3 \
  --replication-factor 1 \
  --bootstrap-server $BOOTSTRAP_SERVER

kafka-topics.sh --create --if-not-exists \
  --topic parsed-logs.DLT \
  --partitions 3 \
  --replication-factor 1 \
  --bootstrap-server $BOOTSTRAP_SERVER

echo "All topics created."
kafka-topics.sh --list --bootstrap-server $BOOTSTRAP_SERVER
