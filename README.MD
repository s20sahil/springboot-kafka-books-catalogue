# README FIRST!

Welcome to the spring-boot-kafka producer & consumer samples with Springboot. The prupose of this project is to explore apache kafka configuration using SpringBoot V3. 

## Overview

This repository explores the features of Springboot to integrate Apache kafka event streaming

## Component Diagram

The diagram below self-explanatory and represents the high level components which are created by this project

![High level component diagram](blueprint.png)

## Quickstart

The simplest way to run the project is to run the `docker-compose.yml` file in the root directory. This docker compose file consists all the individual servers needed to run the entire application components , which includes the following list of server components
```bash
docker compose up
```

IMPORTANT : If tyou are running project in local and try to update the docker images in the repo then please take the following steps :
1. Make sure that root pom.xml has correct user listed in the docker hub registry url e.g. 
2. Go to the root directory and execute the following command to compile, build and upload new images to the docker registry publicly.
```bash
./mvnw clean compile jib:build -d 
```

**#Data stores**
postgres-library => image: postgres:latest

**#Event Brokers**
zookeeper => image: confluentinc/cp-zookeeper:latest
kafka1 => image : confluentinc/cp-kafka:latest
kafka2 => image : confluentinc/cp-kafka:latest
kafka3 => image : confluentinc/cp-kafka:latest


**#Spring boot Service Containers**

discovery-server => image: technolearn/discovery-server-books-catalogue:latest
api-gateway => image: technolearn/api-gateway-books-catalogue:latest
library-service image: technolearn/product-books-catalogue:latest

```bash
docker compose up -d #Because the file name is docker-compose.yml, you do not have to provide filename
```

## Quick Access Links Upon Startup

Once the application is up and running after executing the `docker compose up -d` command(Running all the spring boot services in their own containers) we can acces the links given below. 


1. Eureka Server UI client : http://localhost:8080/eureka/web    
2. API Gateway endpoint to make other service calls : http://localhost:8181/api/*
    1. library-service : http://localhost:8181/api/products
    

## Testing

To run the junit tests, open a terminal and go to the root directory and make use of the maven wrapper to run the maven test target as given below.

```bash
./mvnw clean test
```

## TODOs

1. 
2. 

## References

- [Auto generated references trhough SpringBoot Initializer](./HELP.md)