# Kubernetes Deployment Guide

This guide outlines the steps to set up a Kubernetes cluster using `kind` and deploy the multiple services, including an Nginx API Gateway, Authentication Backend, Web Scraping Service, User Profile Management Service, and User Product Service.
(Note :  the *secret.yaml files have not been shared intentionally)

---

## Prerequisites

1. Install [Kind](https://kind.sigs.k8s.io/)
2. Install [Kubectl](https://kubernetes.io/docs/tasks/tools/)

---

## Steps to Deploy

### Step 1: Setup Kind Cluster
kind create cluster --config kind-config.yaml

### Step 2: Deploy the Nginx API Gateway
kubectl apply -f nginx-api-gateway-configmap.yaml  
kubectl apply -f nginx-api-gateway-deployment.yaml  
kubectl apply -f nginx-api-gateway-service.yaml

### Step 3: Deploy the Authentication Backend
kubectl apply -f auth-app-secret.yaml  
kubectl apply -f auth-app-configmap.yaml  
kubectl apply -f auth-app-deployment.yaml  
kubectl apply -f auth-app-service.yaml

### Step 4: Deploy the Web Scraping Service
kubectl apply -f wss-app-secret.yaml  
kubectl apply -f wss-app-configmap.yaml  
kubectl apply -f wss-app-deployment.yaml  
kubectl apply -f wss-app-service.yaml

### Step 5: Deploy the User Profile Management Service
kubectl apply -f upm-app-secret.yaml  
kubectl apply -f upm-app-configmap.yaml  
kubectl apply -f upm-app-deployment.yaml  
kubectl apply -f upm-app-service.yaml

### Step 6: Deploy the User Product Service
kubectl apply -f ups-app-secret.yaml  
kubectl apply -f ups-app-configmap.yaml  
kubectl apply -f ups-app-deployment.yaml  
kubectl apply -f ups-app-service.yaml

### Step 7: Verify All Deployments and Services
kubectl get pods  
kubectl get services  
kubectl get all

### URLs to access the services locally via localhost:
### Authentication Service:
  URL: http://localhost:30080/auth/
  This forwards requests to the auth-app-service on port 8098.  
  
### User Profile Management Service:
  URL: http://localhost:30080/users  
  This forwards requests to the upm-app-service on port 8088.
  
### User Product Service:
  URL: http://localhost:30080/user-products/  
  This forwards requests to the ups-app-service on port 8082.
  
### Default Root:
  URL: http://localhost:30080/  
  This serves static files or routes requests as defined in the try_files directive.

