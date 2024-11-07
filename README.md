webflux，
分布式锁，
mybatis-plus,
langchain4j,ai

## Kubernetes Deployment

This project can be deployed using Kubernetes with separate configurations for development, testing, and production environments.

### Prerequisites

- Kubernetes cluster
- kubectl configured to interact with your cluster
- Docker to build the application image

### Building the Docker Image

First, build the Docker image for the application:

```sh
docker build -t hfai:latest .
```

### Deploying to Development Environment

Apply the development deployment and service configurations:

```sh
kubectl apply -f k8s/deployment-dev.yaml
kubectl apply -f k8s/service-dev.yaml
```

### Deploying to Testing Environment

Apply the testing deployment and service configurations:

```sh
kubectl apply -f k8s/deployment-test.yaml
kubectl apply -f k8s/service-test.yaml
```

### Deploying to Production Environment

Apply the production deployment and service configurations:

```sh
kubectl apply -f k8s/deployment-prod.yaml
kubectl apply -f k8s/service-prod.yaml
```

### Accessing the Application

After deploying, you can access the application using the service's external IP or the cluster IP, depending on your Kubernetes setup.
