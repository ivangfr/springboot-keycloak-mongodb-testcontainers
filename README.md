# springboot-keycloak-mongodb-testcontainers

The goals of this project are:

- Create a [`Spring Boot`](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/) application that manages books, called `book-service`;
- Use [`Keycloak`](https://www.keycloak.org) as authentication and authorization server;
- Test using [`Testcontainers`](https://www.testcontainers.org/);
- Explore the utilities and annotations that `Spring Boot` provides when testing applications.

> **Note:** In [`kubernetes-minikube-environment`](https://github.com/ivangfr/kubernetes-minikube-environment/tree/master/book-service-kong-keycloak) repository, it's shown how to deploy this project in `Kubernetes` (`Minikube`)

## Application

- ### book-service
  
  `Spring Boot` Web application that manages books. [`MongoDB`](https://www.mongodb.com) is used as storage, and the application's sensitive endpoints (like add, update and delete books) are secured.
  
  ![book-service-swagger](images/book-service-swagger.png)

## Prerequisites

- [`Java 11+`](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- [`Docker`](https://www.docker.com/)
- [`Docker-Compose`](https://docs.docker.com/compose/install/)
- [`jq`](https://stedolan.github.io/jq)

## Start Environment

- Open a terminal and inside `springboot-keycloak-mongodb-testcontainers` root folder run
  ```
  docker-compose up -d
  ```

- Wait a bit until all containers are `Up (healthy)`. You can check their status by running
  ```
  docker-compose ps
  ```

## Configure Keycloak

There are two ways: running a script or using `Keycloak` website

### Running script

- In a terminal, make sure you are in `springboot-keycloak-mongodb-testcontainers` root folder

- Run the following script to configure `Keycloak` for `book-service` application
  ```
  ./init-keycloak.sh
  ```

  This script creates `company-services` realm, `book-service` client, `manage_books` client role and the user `ivan.franchin` with the role `manage_books` assigned.

- Copy the `BOOK_SERVICE_CLIENT_SECRET` value printed at the end. It will be needed whenever we call `Keycloak` to get a token to access `book-service` 

### Using Keycloak website

![keycloak](images/keycloak.png)

#### Login

- Access http://localhost:8080/auth/admin/master/console

- Login with the credentials
  ```
  Username: admin
  Password: admin
  ```

#### Create a new Realm

- Go to top-left corner and hover the mouse over `Master` realm. Click the `Add realm` blue button that will appear
- Set `company-services` to the `Name` field and click `Create` button

#### Create a new Client

- On the left menu, click `Clients`
- Click `Create` button
- Set `book-service` to `Client ID` and click `Save` button
- In `Settings` tab
    - Set `confidential` to `Access Type`
    - Set `http://localhost:9080` to `Valid Redirect URIs`
    - Click `Save` button
- In `Credentials` tab, you can find the secret `Keycloak` generated for `simple-service`
- In `Roles` tab
    - Click `Add Role` button
    - Set `manage_books` to `Role Name` and click `Save` button

#### Create a new User

- On the left menu, click `Users`
- Click `Add User` button
- Set `ivan.franchin` to `Username` field
- Click `Save`
- In `Credentials` tab
    - Set the value `123` to `Password` and `Password Confirmation`
    - Turn off the `Temporary` field
    - Click `Set password` button
    - Confirm the pop up and clock `Set Password` red button
- In `Role Mappings` tab
    - Select `book-service` on the combo-box `Client Roles`
    - In `Available Roles`, select `manage_books` role and click `Add selected >>` button

## Running book-service with Gradle

- Open a new terminal and navigate to `springboot-keycloak-mongodb-testcontainers` root folder

- Run the following command to start the application
  ```
  ./gradlew book-service:clean book-service:bootRun \
    --args='--server.port=9080 --spring.data.mongodb.username=bookuser --spring.data.mongodb.password=bookpass'
  ```
  
- The application's swagger URL is http://localhost:9080/swagger-ui.html

## Getting Access Token

1. Open a terminal and make sure you are in `springboot-keycloak-mongodb-testcontainers` root folder

1. Create an environment variable that contains the `Client Secret` generated by `Keycloak` to `book-service` at [Configure Keycloak](#configure-keycloak) step
   ```
   BOOK_SERVICE_CLIENT_SECRET=...
   ```

1. Run the commands below to get an access token for `ivan.franchin`
   ```
   ACCESS_TOKEN=$(./get-access-token.sh $BOOK_SERVICE_CLIENT_SECRET)
   echo $ACCESS_TOKEN
   ```

1. The access token has a default expiration time of `5 minutes`

## Test using cURL

1. In terminal, call the endpoint `GET /api/books`
   ```
   curl -i http://localhost:9080/api/books
   ```
   It should return:
   ```
   HTTP/1.1 200
   []
   ```

1. Try to call the endpoint `POST /api/books`, without access token
   ```
   curl -i -X POST http://localhost:9080/api/books \
     -H "Content-Type: application/json" \
     -d '{ "authorName": "ivan", "title": "java 8", "price": 10.5 }'
   ```
   It should return:
   ```
   HTTP/1.1 302
   ```

1. If you do not have the access token stored in `ACCESS_TOKEN` environment variable, get it by following the steps describe at [Getting Access Token](#getting-access-token)

1. Call the endpoint `POST /api/books`, now informing the access token
   ```
   curl -i -X POST http://localhost:9080/api/books \
     -H "Authorization: Bearer $ACCESS_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{ "authorName": "ivan", "title": "java 8", "price": 10.5 }'
   ```
   It should return something like
   ```
   HTTP/1.1 201
   { "id":"01d984be-26bc-49f5-a201-602293d62b82", "authorName":"ivan", "title":"java 8", "price":10.5 }
   ```

## Test using Swagger

1. Access http://localhost:9080/swagger-ui.html

1. Click `GET /api/books` to open it. Then, click `Try it out` button and, finally, click `Execute` button.

   It will return a http status code `200` and an empty list or a list with some books if you've already added them

1. Now, let's try to call a secured endpoint without authentication. Click `POST /api/books` to open it. Then, click `Try it out` button (you can use the default values) and, finally, click `Execute` button.

   It will return:
   ```
   Failed to fetch
   ```

1. Get the access token as explained at [Getting Access Token](#getting-access-token)

1. Copy the token generated and go back to `Swagger`

1. Click the `Authorize` button and paste the access token in the `Value` field. Then, click `Authorize` and, to finalize, click `Close`

1. Go to `POST /api/books`, click `Try it out` and, finally, click `Execute` button.

   It should return something like
   ```
   HTTP/1.1 201
   {
     "id": "5cf212c3-7902-4141-968b-82ae7a3443f1",
     "authorName": "Craig Walls",
     "title": "Spring Boot",
     "price": 10.5
   }
   ```

## Running book-service as a Docker Container

1. In a terminal, navigate to `springboot-keycloak-mongodb-testcontainers` root folder

1. Build Docker Image
   ```
   ./gradlew book-service:clean book-service:jibDockerBuild -x test -x integrationTest
   ```
   | Environment Variable | Description                                                       |
   | -------------------- | ----------------------------------------------------------------- |
   | `MONGODB_HOST`       | Specify host of the `Mongo` database to use (default `localhost`) |
   | `MONGODB_PORT`       | Specify port of the `Mongo` database to use (default `27017`)     |
   | `KEYCLOAK_HOST`      | Specify host of the `Keycloak` to use (default `localhost`)       |
   | `KEYCLOAK_PORT`      | Specify port of the `Keycloak` to use (default `8080`)            |
  
1. Run `book-service` docker container, joining it to docker-compose network
   ```
   docker run --rm --name book-service \
     -p 9080:8080 \
     -e MONGODB_HOST=mongodb \
     -e KEYCLOAK_HOST=keycloak \
     -e SPRING_DATA_MONGODB_USERNAME=bookuser \
     -e SPRING_DATA_MONGODB_PASSWORD=bookpass \
     --network=springboot-keycloak-mongodb-testcontainers_default \
     ivanfranchin/book-service:1.0.0
   ```

1. Open a new terminal and create an environment variable that contains the `Client Secret` generated by `Keycloak`
   ```
   BOOK_SERVICE_CLIENT_SECRET=...
   ```

1. In order to get the access token from `Keycloak`, run the following commands.
   > **Note:** the `keycloak` string is informed in the second argument of the script. It changes `localhost` host inside the script. This way, we won't have the error complaining about an invalid token due to an invalid token issuer.
   ```
   ACCESS_TOKEN=$(./get-access-token.sh $BOOK_SERVICE_CLIENT_SECRET keycloak)
   echo $ACCESS_TOKEN
   ```

1. Test using cURL or Swagger are the same as explained above

## Useful Links & Commands

- **MongoDB**

  List all books
  ```
  docker exec -it mongodb mongo -ubookuser -pbookpass --authenticationDatabase bookdb
  use bookdb
  db.books.find()
  ```
  > Type `exit` to get out of MongoDB shell

- **jwt.io**

  With [jwt.io](https://jwt.io) you can inform the JWT token received from `Keycloak` and the online tool decodes the token, showing its header and payload.

## Shutdown

- To stop `book-service`, go to the terminal where the application is running and press `Ctrl+C`
- To stop and remove docker-compose containers, networks and volumes, make sure you are in `springboot-keycloak-mongodb-testcontainers` and run
  ```
  docker-compose down -v
  ```
- To remove the Docker image created by this project, run
  ```
  docker rmi ivanfranchin/book-service:1.0.0
  ```

## Running Unit and Integration Tests

- In a terminal and inside `springboot-keycloak-mongodb-testcontainers` root folder, run the command below to run unit and integration tests
  ```
  ./gradlew book-service:clean book-service:assemble \
    book-service:cleanTest \
    book-service:test \
    book-service:integrationTest
  ```
  > **Note:** During integration tests, `Testcontainers` will start automatically `MongoDB` and `Keycloak` containers before the tests begin and shuts them down when the tests finish.

- From `springboot-keycloak-mongodb-testcontainers` root folder, **Unit Testing Report** can be found at
  ```
  book-service/build/reports/tests/test/index.html
  ```
  
- From `springboot-keycloak-mongodb-testcontainers` root folder, **Integration Testing Report** can be found at
  ```
  book-service/build/reports/tests/integrationTest/index.html
  ```

## Issues

- Disabled [`BookControllerTest`](https://github.com/ivangfr/springboot-keycloak-mongodb-testcontainers/blob/master/book-service/src/test/java/com/mycompany/bookservice/controller/BookControllerTest.java) because I am getting the exception below. The [`adapterConfig` parameter passed to `internalBuild` is `null`](https://github.com/keycloak/keycloak/blob/master/adapters/oidc/adapter-core/src/main/java/org/keycloak/adapters/KeycloakDeploymentBuilder.java#L57)
  ```
  java.lang.NullPointerException
  	at org.keycloak.adapters.KeycloakDeploymentBuilder.internalBuild(KeycloakDeploymentBuilder.java:57)
  	at org.keycloak.adapters.KeycloakDeploymentBuilder.build(KeycloakDeploymentBuilder.java:202)
  	at org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver.resolve(KeycloakSpringBootConfigResolver.java:39)
  	at org.keycloak.adapters.springsecurity.config.KeycloakSpringConfigResolverWrapper.resolve(KeycloakSpringConfigResolverWrapper.java:40)
  	at org.keycloak.adapters.AdapterDeploymentContext.resolveDeployment(AdapterDeploymentContext.java:89)
  ...
  ```

## References

- https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html
