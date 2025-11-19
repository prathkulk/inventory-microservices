#!/bin/bash

echo "🚀 Setting up Minikube environment..."
eval $(minikube docker-env)

echo "📦 Building JARs and Docker Images..."

# Function to build service
build_service() {
    service_name=$1
    echo "----------------------------------------------------"
    echo "🔨 Building $service_name..."
    echo "----------------------------------------------------"
    cd $service_name
    ./mvnw clean package -DskipTests
    docker build -t $service_name:latest .
    cd ..
}

build_service "discovery-server"
build_service "inventory-service"
build_service "order-service"
build_service "api-gateway"

echo "----------------------------------------------------"
echo "☸️  Applying Kubernetes Manifests..."
echo "----------------------------------------------------"
kubectl apply -f k8s/discovery-server.yaml
kubectl apply -f k8s/inventory-service.yaml
kubectl apply -f k8s/order-service.yaml
kubectl apply -f k8s/api-gateway.yaml

echo "----------------------------------------------------"
echo "✅ Deployment Complete!"
echo "----------------------------------------------------"
echo "To access the API Gateway, run one of the following:"
echo "  1. minikube tunnel (Keep this running in a separate terminal, then access http://localhost:9090)"
echo "  2. minikube service api-gateway --url"
