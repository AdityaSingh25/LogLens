.PHONY: help infra-up infra-down infra-logs infra-clean kafka-topics \
        build test lint dev clean

help:
	@echo ""
	@echo "LogLens — available commands:"
	@echo ""
	@echo "  make infra-up       Boot all infrastructure (Kafka, Postgres, ES, Qdrant, Redis, OTel)"
	@echo "  make infra-down     Stop infrastructure containers"
	@echo "  make infra-logs     Tail logs from all infra containers"
	@echo "  make infra-clean    Stop containers and delete all volumes (wipes all data)"
	@echo "  make kafka-topics   Create all Kafka topics (run after infra-up)"
	@echo ""
	@echo "  make build          Build all services"
	@echo "  make test           Run all tests"
	@echo "  make lint           Run linters across all services"
	@echo "  make dev            Start all services in development mode"
	@echo "  make clean          Stop infra and remove all build artifacts"
	@echo ""

infra-up:
	docker compose -f infra/docker-compose.yml up -d
	@echo "Waiting for services to be healthy..."
	@sleep 5
	@echo "Infrastructure is up. Run 'make kafka-topics' to create topics."

infra-down:
	docker compose -f infra/docker-compose.yml down

infra-logs:
	docker compose -f infra/docker-compose.yml logs -f

infra-clean:
	docker compose -f infra/docker-compose.yml down -v
	@echo "All containers stopped and volumes deleted."

kafka-topics:
	docker compose -f infra/docker-compose.yml exec kafka \
		bash /opt/bitnami/kafka/bin/kafka-topics.sh \
		--create --if-not-exists --topic raw-logs --partitions 12 --replication-factor 1 --bootstrap-server localhost:9092
	docker compose -f infra/docker-compose.yml exec kafka \
		bash /opt/bitnami/kafka/bin/kafka-topics.sh \
		--create --if-not-exists --topic parsed-logs --partitions 12 --replication-factor 1 --bootstrap-server localhost:9092
	docker compose -f infra/docker-compose.yml exec kafka \
		bash /opt/bitnami/kafka/bin/kafka-topics.sh \
		--create --if-not-exists --topic embedding-results --partitions 6 --replication-factor 1 --bootstrap-server localhost:9092
	docker compose -f infra/docker-compose.yml exec kafka \
		bash /opt/bitnami/kafka/bin/kafka-topics.sh \
		--create --if-not-exists --topic alert-events --partitions 6 --replication-factor 1 --bootstrap-server localhost:9092
	docker compose -f infra/docker-compose.yml exec kafka \
		bash /opt/bitnami/kafka/bin/kafka-topics.sh \
		--create --if-not-exists --topic raw-logs.DLT --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092
	docker compose -f infra/docker-compose.yml exec kafka \
		bash /opt/bitnami/kafka/bin/kafka-topics.sh \
		--create --if-not-exists --topic parsed-logs.DLT --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092
	@echo "All Kafka topics created."

build:
	mvn -pl services/auth-service package -DskipTests
	mvn -pl services/ingestion-service package -DskipTests
	mvn -pl services/parser-service package -DskipTests
	mvn -pl services/query-service package -DskipTests
	mvn -pl services/alerting-service package -DskipTests
	cd services/embedding-service && npm run build
	cd services/notification-service && npm run build
	cd frontend && npm run build

test:
	mvn -pl services/auth-service test
	mvn -pl services/ingestion-service test
	mvn -pl services/parser-service test
	mvn -pl services/query-service test
	mvn -pl services/alerting-service test
	cd services/embedding-service && npm test
	cd services/notification-service && npm test
	cd frontend && npm test

lint:
	mvn -pl services/auth-service checkstyle:check
	mvn -pl services/ingestion-service checkstyle:check
	mvn -pl services/parser-service checkstyle:check
	mvn -pl services/query-service checkstyle:check
	mvn -pl services/alerting-service checkstyle:check
	cd services/embedding-service && npm run lint
	cd services/notification-service && npm run lint
	cd frontend && npm run lint

dev:
	@echo "Start each service manually in separate terminals:"
	@echo "  Spring: mvn -pl services/<name> spring-boot:run"
	@echo "  Node:   cd services/<name> && npm run dev"
	@echo "  UI:     cd frontend && npm run dev"

clean: infra-down
	find services -name "target" -type d -exec rm -rf {} + 2>/dev/null || true
	find services -name "dist" -type d -exec rm -rf {} + 2>/dev/null || true
	find services -name "node_modules" -type d -exec rm -rf {} + 2>/dev/null || true
	rm -rf frontend/dist frontend/node_modules 2>/dev/null || true
	@echo "Clean complete."
