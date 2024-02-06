# README FIRST!

This branch contains the code for setting up Kafka cluster with a single broker `kafka1` and zookeeper instance `zoo1`. Both these instances are setup to communicate with SSL protocol to prevent man in the middle attack.
Once the kafka cluster is up the apps need to be run up with profile `ssl` which will then refer to the properties-ssl.properties file to configure the key & trust stores for establishing SSL handshake with the broker. Following are the detailed steps to replicate the setup on a linux platform.

## Pre-requisites

1. Generate slef-signed certificates and keystores(jks files) using Openssl
    - Open a terminal and change directory to `kafka-ssl/secrets`
    - Run the shell script from the folder to generate the required keystore files. Please note that the it uses the pre-saved config and hostnames as per the kafka & zookeeper containers run up by the `docker-compose-ssl.yml` file
      - ```./create-certs.sh```
    - Types yes whenenever terminal shows a prompt for yes/no answer
    - This will generate a bunch of files in the [scerets](./secrets/) fodler
2. Setup relative keystore paths in the application-ssl.properties file of the producer spring boot services.
   - Open file [kafka-producer/src/main/resources/application-ssl.properties](/kafka-producer/src/main/resources/application-ssl.properties)
   - Update relative paths of the properties 
     - ```bash
        ## SSL Configuration
        spring.kafka.producer.ssl.trust-store-location=file:../kafka-ssl/secrets/kafka.producer.truststore.jks
        spring.kafka.producer.ssl.trust-store-password=confluent
        spring.kafka.producer.ssl.key-store-location=file:../kafka-ssl/secrets/kafka.producer.keystore.jks
        spring.kafka.producer.ssl.key-store-password=confluent
        spring.kafka.producer.properties.security.protocol=SSL
        spring.kafka.producer.properties.ssl.endpoint.identification.algorithm=
       ```
3. Setup relative keystore paths in the application-ssl.properties file of the consumer spring boot services.
   - Open file [kafka-consumer/src/main/resources/application-ssl.properties](/kafka-consumer/src/main/resources/application-ssl.properties)
   - Update relative paths of the properties 
     - ```bash
        ## SSL Configuration
        spring.kafka.consumer.ssl.trust-store-location=file:../kafka-ssl/secrets/kafka.consumer.truststore.jks
        spring.kafka.consumer.ssl.trust-store-password=confluent
        spring.kafka.consumer.ssl.key-store-location=file:../kafka-ssl/secrets/kafka.consumer.keystore.jks
        spring.kafka.consumer.ssl.key-store-password=confluent
        spring.kafka.properties.security.protocol=SSL
        spring.kafka.properties.ssl.endpoint.identification.algorithm=
       ```
4. Upload images with updated SSL configs to the docker registry
   
## Run the Application

1. Open the terminal and go to the root directory of the application where the `docker-compose-ssl.yml` file resides
2. Run the docker compose command for the above file using
- ```bash 
  docker compose -f docker-compose-ssl.yml up
  ```
2 **OPTIONAL** : If you have used the docker-compose-ssl.yml file , then please stop the conatiners for kafka-producer & kafka-consumer. Once done, run each spring boot applications one at a time using profile `ssl` 
3. Wait till the conatiners run up. Go to the container logs and make sure the security protocal is listed as SSL.
4. You are good to go!

**Note:** Please note that, SSL communication takes place in between producer->broker & broker->consumer services. The API calls to producer & consumer spring boot apps are not SSL enabled through this setup!


