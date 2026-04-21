#!/bin/bash
set -e

AUTH_URL="http://localhost:8081"
INGEST_URL="http://localhost:8082"
ALERTING_URL="http://localhost:8085"

echo "=== LogLens Demo Seed ==="

# 1. Create tenant
echo "Creating tenant demo-corp..."
TENANT_RESPONSE=$(curl -sf -X POST "$AUTH_URL/auth/tenants" \
  -H "Content-Type: application/json" \
  -d '{"name":"demo-corp"}')
TENANT_ID=$(echo "$TENANT_RESPONSE" | grep -o '"tenant_id":"[^"]*"' | cut -d'"' -f4)
echo "Tenant ID: $TENANT_ID"

# 2. Register admin user
echo "Registering admin user..."
curl -sf -X POST "$AUTH_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@demo-corp.com\",\"password\":\"password123\",\"tenantId\":\"$TENANT_ID\"}" > /dev/null

# 3. Login and get token
echo "Logging in..."
LOGIN_RESPONSE=$(curl -sf -X POST "$AUTH_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@demo-corp.com","password":"password123"}')
TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "ERROR: Could not get token"
  exit 1
fi
echo "Got JWT token."

# 4. Helper to ingest a log
ingest() {
  curl -sf -X POST "$INGEST_URL/ingest/logs" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$1" > /dev/null
}

echo "Ingesting 200 demo logs across 5 services..."

# payment-service: 50 logs (mix of INFO + ERROR)
for i in $(seq 1 35); do
  ingest "{\"serviceName\":\"payment-service\",\"level\":\"INFO\",\"message\":\"Payment processed successfully\",\"metadata\":{\"order_id\":\"ord_$i\",\"amount\":\"$((RANDOM % 1000))\"}}"
done
for i in $(seq 1 15); do
  ingest "{\"serviceName\":\"payment-service\",\"level\":\"ERROR\",\"message\":\"Payment gateway timeout after 30s\",\"metadata\":{\"order_id\":\"ord_err_$i\",\"gateway\":\"stripe\"}}"
done

# auth-service: 30 logs
for i in $(seq 1 25); do
  ingest "{\"serviceName\":\"auth-service\",\"level\":\"INFO\",\"message\":\"User logged in successfully\",\"metadata\":{\"user_id\":\"usr_$i\"}}"
done
for i in $(seq 1 5); do
  ingest "{\"serviceName\":\"auth-service\",\"level\":\"WARN\",\"message\":\"Failed login attempt — invalid credentials\",\"metadata\":{\"ip\":\"192.168.1.$i\"}}"
done

# order-service: 40 logs including ERROR burst (to trigger z-score)
for i in $(seq 1 10); do
  ingest "{\"serviceName\":\"order-service\",\"level\":\"INFO\",\"message\":\"Order created\",\"metadata\":{\"order_id\":\"ord_$i\"}}"
done
for i in $(seq 1 30); do
  ingest "{\"serviceName\":\"order-service\",\"level\":\"ERROR\",\"message\":\"Database connection pool exhausted\",\"metadata\":{\"pool_size\":\"10\",\"waiting\":\"$i\"}}"
done

# inventory-service: 30 logs
for i in $(seq 1 28); do
  ingest "{\"serviceName\":\"inventory-service\",\"level\":\"INFO\",\"message\":\"Stock level updated\",\"metadata\":{\"sku\":\"SKU_$i\",\"quantity\":\"$((RANDOM % 100))\"}}"
done
for i in $(seq 1 2); do
  ingest "{\"serviceName\":\"inventory-service\",\"level\":\"ERROR\",\"message\":\"Stock sync failed — downstream timeout\",\"metadata\":{\"warehouse\":\"WH_$i\"}}"
done

# notification-service: 50 logs
for i in $(seq 1 45); do
  ingest "{\"serviceName\":\"notification-service\",\"level\":\"INFO\",\"message\":\"Email sent successfully\",\"metadata\":{\"recipient\":\"user$i@example.com\",\"template\":\"order_confirmation\"}}"
done
for i in $(seq 1 5); do
  ingest "{\"serviceName\":\"notification-service\",\"level\":\"ERROR\",\"message\":\"Slack webhook delivery failed after 3 retries\",\"metadata\":{\"channel\":\"#alerts\",\"attempt\":\"3\"}}"
done

echo "Done ingesting logs."

# 5. Create alert rules
echo "Creating alert rules..."

curl -sf -X POST "$ALERTING_URL/alerting/rules" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Payment Service Anomaly\",
    \"condition\": {\"type\":\"ZSCORE\",\"metric\":\"LOG_VOLUME\",\"windowMinutes\":5,\"serviceName\":\"payment-service\"},
    \"severity\": \"HIGH\",
    \"notificationChannels\": []
  }" > /dev/null

curl -sf -X POST "$ALERTING_URL/alerting/rules" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"High Error Volume\",
    \"condition\": {\"type\":\"THRESHOLD\",\"metric\":\"LOG_VOLUME\",\"windowMinutes\":5,\"threshold\":50},
    \"severity\": \"CRITICAL\",
    \"notificationChannels\": []
  }" > /dev/null

echo ""
echo "=== Seed Complete ==="
echo ""
echo "  Tenant:  demo-corp ($TENANT_ID)"
echo "  User:    admin@demo-corp.com / password123"
echo "  Logs:    200 across 5 services"
echo "  Rules:   2 alert rules created"
echo ""
echo "  Open:    http://localhost:5173"
echo ""
echo "  Try searching: 'payment gateway errors'"
echo "                 'database connection failures'"
echo "                 'failed login attempts'"
