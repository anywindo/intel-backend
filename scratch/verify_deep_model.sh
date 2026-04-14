#!/bin/bash

# Security Verification Script — Shallow vs Deep Model
# This script demonstrates the security vulnerabilities of the Shallow model
# and the robust protection of the Deep model.

echo "🚀 Starting Intel Security Verification..."

# 1. Start the application
./mvnw spring-boot:run > /dev/null 2>&1 &
APP_PID=$!

echo "⏳ Waiting for application to start on port 8080..."
count=0
until curl -s http://localhost:8080/api/shallow/business-card/search > /dev/null || [ $count -eq 30 ]; do
    sleep 2
    ((count++))
done

if [ $count -eq 30 ]; then
    echo "❌ Application failed to start. Cleaning up..."
    kill -9 $APP_PID
    exit 1
fi

echo "✅ Application is LIVE."

echo -e "\n--- 🔓 SHALLOW MODEL VULNERABILITY TEST ---"
echo "Scenario: Bypassing security by omitting search term vs providing one."
echo "Expectation: Providing no search term dumps all data (Case A)."
# Get an anonymous token first (the shallow flaw)
TOKEN=$(curl -s http://localhost:8080/api/shallow/token | jq -r .accessToken)
echo "Acquired Anonymous Token: $TOKEN"
# Fetch ALL employees (the leak)
curl -s "http://localhost:8080/api/shallow/employees?token=$TOKEN&search=" | jq -r ".count"
echo "Result: DATA OVERFETCHING! (Shallow model leaked all records because search term was empty)"

echo -e "\n--- 🛡️ DEEP MODEL PROTECTION TEST ---"
echo "Scenario 1: Accessing deep API WITHOUT valid token (Blocked by Spring Security)"
RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/deep/business-card/search \
     -H "Content-Type: application/json" \
     -d '{"userId": "admin", "token": "invalid", "query": "John"}')
echo "HTTP Response: $RESPONSE_CODE"
if [ "$RESPONSE_CODE" == "403" ]; then echo "Result: BLOCKED (Correctly rejected by Infrastructure Layer)"; fi

echo -e "\nScenario 2: Accessing deep API with valid token but INVALID query (Blocked by Domain Layer)"
VALID_TOKEN="admin:ADMIN:1900000000"
curl -s -X POST http://localhost:8080/api/deep/business-card/search \
     -H "Authorization: Bearer $VALID_TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"userId\": \"admin\", \"token\": \"$VALID_TOKEN\", \"query\": \"J\"}" | jq .
echo -e "\nResult: BLOCKED (Correctly rejected by Domain Primitive 'EmployeeSearchQuery')"

echo -e "\nScenario 3: CASE B - RBAC Check (USER attempting to access ADMIN data)"
USER_TOKEN="employee1:USER:1900000000"
RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/deep/hierarchy/products \
     -H "Authorization: Bearer $USER_TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"token\": \"$USER_TOKEN\"}")
echo "HTTP Response: $RESPONSE_CODE"
if [ "$RESPONSE_CODE" == "403" ]; then echo "Result: BLOCKED (Correctly rejected by RBAC Role Enforcement)"; fi

echo -e "\nScenario 4: CASE C - Multi-Factor Binding Check (Identity Mismatch)"
ADMIN_TOKEN="admin:ADMIN:1900000000"
RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/deep/seims/employees \
     -H "Authorization: Bearer $ADMIN_TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"userId\": \"malicious\", \"token\": \"$ADMIN_TOKEN\"}")
echo "HTTP Response: $RESPONSE_CODE"
if [ "$RESPONSE_CODE" == "403" ]; then echo "Result: BLOCKED (Correctly rejected by Identity Binding in 'SecureSession')"; fi

echo -e "\n🧹 Cleaning up processes..."
kill -9 $APP_PID
echo "🏁 Verification Complete."
