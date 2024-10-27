# springboot-keycloak-mongodb-testcontainers

The goals of this project are:

- Create a [`Spring Boot`](https://docs.spring.io/spring-boot/index.html) application that manages books, called `book-service`;
- Use [`Keycloak`](https://www.keycloak.org) as OpenID Connect Provider;
- Test using [`Testcontainers`](https://testcontainers.com/);
- Explore the utilities and annotations that `Spring Boot` provides when testing applications.

## Proof-of-Concepts & Articles

On [ivangfr.github.io](https://ivangfr.github.io), I have compiled my Proof-of-Concepts (PoCs) and articles. You can easily search for the technology you are interested in by using the filter. Who knows, perhaps I have already implemented a PoC or written an article about what you are looking for.

## Additional Readings

- \[**Medium**\] [**Implementing and Securing a Simple Spring Boot REST API with Keycloak**](https://medium.com/@ivangfr/how-to-secure-a-spring-boot-app-with-keycloak-5a931ee12c5a)
- \[**Medium**\] [**Implementing and Securing a Simple Spring Boot UI (Thymeleaf + RBAC) with Keycloak**](https://medium.com/@ivangfr/how-to-secure-a-simple-spring-boot-ui-thymeleaf-rbac-with-keycloak-ba9f30b9cb2b)
- \[**Medium**\] [**Implementing and Securing a Spring Boot GraphQL API with Keycloak**](https://medium.com/@ivangfr/implementing-and-securing-a-spring-boot-graphql-api-with-keycloak-c461c86e3972)
- \[**Medium**\] [**Building a Single Spring Boot App with Keycloak or Okta as IdP: Introduction**](https://medium.com/@ivangfr/building-a-single-spring-boot-app-with-keycloak-or-okta-as-idp-introduction-2814a4829aed)

## Application

- ### book-service
  
  `Spring Boot` Web application that manages books. [`MongoDB`](https://www.mongodb.com) is used as storage, and the application's sensitive endpoints (like create, update and delete books) are secured.
  
  ![book-service-swagger](documentation/book-service-swagger.jpeg)

## Prerequisites

- [`Java 21+`](https://www.oracle.com/java/technologies/downloads/#java21)
- [`Docker`](https://www.docker.com/)
- [`jq`](https://jqlang.github.io/jq/)

## Start Environment

Open a terminal and, inside `springboot-keycloak-mongodb-testcontainers` root folder, run the script below
```
./init-environment.sh
```

## Configure Keycloak

There are two ways: running a script or using `Keycloak` website

### Running Script

- In a terminal, make sure you are in `springboot-keycloak-mongodb-testcontainers` root folder

- Run the following script to configure `Keycloak` for `book-service` application
  ```
  ./init-keycloak.sh
  ```

  This script creates:
  - `company-services` realm;
  - `book-service` client;
  - `manage_books` client role;
  - user with _username_ `ivan.franchin` and _password_ `123` and with the role `manage_books` assigned.

- The `book-service` client secret (`BOOK_SERVICE_CLIENT_SECRET`) is shown at the end of the execution. It will be used in the next step

- You can check the configuration in `Keycloak` by accessing http://localhost:8080. The credentials are `admin/admin`. 

### Using Keycloak Website

#### Login

- Access http://localhost:8080

- Login with the credentials
  ```
  Username: admin
  Password: admin
  ```

#### Create a new Realm

- On the left menu, click the dropdown button that contains `Keycloak` and then, click `Create Realm` button
- Set `company-services` to the `Realm name` field and click `Create` button

### Disable Required Action Verify Profile

- On the left menu, click `Authentication`
- Select `Required actions` tab
- Disable `Verify Profile`

#### Create a new Client

- On the left menu, click `Clients`
- Click `Create client` button
- In `General Settings`
  - Set `book-service` to `Client ID`
  - Click `Next` button
- In `Capability config`
  - Enable `Client authentication` toggle switch
  - Click `Next` button
- In `Login settings` tab
  - Set `http://localhost:9080/*` to `Valid redirect URIs`
  - Click `Save` button
- In `Credentials` tab, you can find the secret generated for `book-service`
- In `Roles` tab
  - Click `Create role` button
  - Set `manage_books` to `Role Name`
  - Click `Save` button

#### Create a new User

- On the left menu, click `Users`
- Click `Create new user` button
- Set `ivan.franchin` to `Username` field
- Click `Create`
- In `Credentials` tab
  - Click `Set password` button
  - Set the value `123` to `Password` and `Password confirmation`
  - Disable the `Temporary` field toggle switch
  - Click `Save` button
  - Confirm by clicking `Save password` button
- In `Role Mappings` tab
  - Click `Assign role` button
  - Click `Filter by realm roles` dropdown button and select `Filter by clients`
  - Select `[book-service] manage_books` name and click `Assign` button

## Running book-service with Gradle

- Open a new terminal and navigate to `springboot-keycloak-mongodb-testcontainers` root folder

- Run the following command to start the application
  ```
  ./gradlew book-service:clean book-service:bootRun --args='--server.port=9080'
  ```

## Running book-service as a Docker Container

- In a terminal, navigate to `springboot-keycloak-mongodb-testcontainers` root folder

- Build Docker Image
  ```
  ./docker-build.sh
  ```
  | Environment Variable | Description                                                       |
  |----------------------|-------------------------------------------------------------------|
  | `MONGODB_HOST`       | Specify host of the `Mongo` database to use (default `localhost`) |
  | `MONGODB_PORT`       | Specify port of the `Mongo` database to use (default `27017`)     |
  | `KEYCLOAK_HOST`      | Specify host of the `Keycloak` to use (default `localhost`)       |
  | `KEYCLOAK_PORT`      | Specify port of the `Keycloak` to use (default `8080`)            |

- Run `book-service` docker container, joining it to project Docker network
  ```
  docker run --rm --name book-service \
    -p 9080:8080 \
    -e MONGODB_HOST=mongodb \
    -e KEYCLOAK_HOST=keycloak \
    --network=springboot-keycloak-mongodb-testcontainers-net \
    ivanfranchin/book-service:1.0.0
  ```

## Getting Access Token

- In a terminal, create an environment variable that contains the `Client Secret` generated by `Keycloak` to `book-service` at [Configure Keycloak](#configure-keycloak) step
  ```
  BOOK_SERVICE_CLIENT_SECRET=...
  ```

- **When running book-service with Gradle**

  Run the commands below to get an access token for `ivan.franchin`
  ```
  ACCESS_TOKEN=$(curl -s -X POST \
    "http://localhost:8080/realms/company-services/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=ivan.franchin" \
    -d "password=123" \
    -d "grant_type=password" \
    -d "client_secret=$BOOK_SERVICE_CLIENT_SECRET" \
    -d "client_id=book-service" | jq -r .access_token)
  echo $ACCESS_TOKEN
  ```

- **When running book-service as a Docker Container**

  Run the commands below to get an access token for `ivan.franchin`
  ```
  ACCESS_TOKEN=$(
    docker run -t --rm -e CLIENT_SECRET=$BOOK_SERVICE_CLIENT_SECRET --network springboot-keycloak-mongodb-testcontainers-net alpine/curl:latest sh -c '
      curl -s -X POST http://keycloak:8080/realms/company-services/protocol/openid-connect/token \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "username=ivan.franchin" \
        -d "password=123" \
        -d "grant_type=password" \
        -d "client_secret=$CLIENT_SECRET" \
        -d "client_id=book-service"' | jq -r .access_token)
  echo $ACCESS_TOKEN
  ```
  > **Note**: We are running a alpine/curl Docker container and joining it to the project Docker network. By informing `"keycloak:8080"` host/port we won't have the error complaining about an invalid token due to an invalid token issuer.

- In [jwt.io](https://jwt.io), you can decode and verify the `JWT` access token

## Test using cURL

- In terminal, call the endpoint `GET /api/books`
  ```
  curl -i http://localhost:9080/api/books
  ```
  It should return:
  ```
  HTTP/1.1 200
  []
  ```

- Try to call the endpoint `POST /api/books`, without access token
  ```
  curl -i -X POST http://localhost:9080/api/books \
    -H "Content-Type: application/json" \
    -d '{"authorName": "Ivan Franchin", "title": "Java 8", "price": 10.5}'
  ```
  It should return:
  ```
  HTTP/1.1 401
  ```

- Get the Access Token as explained on section [Getting Access Token](#getting-access-token)

- Call the endpoint `POST /api/books`, now informing the access token
  ```
  curl -i -X POST http://localhost:9080/api/books \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"authorName": "Ivan Franchin", "title": "Java 8", "price": 10.5}'
  ```
  It should return something like
  ```
  HTTP/1.1 201
  {"id":"612f4f9438e39e473c4d098b", "authorName":"Ivan Franchin", "title":"Java 8", "price":10.5}
  ```

## Test using Swagger

- Access http://localhost:9080/swagger-ui.html

- Click `GET /api/books` to open it. Then, click `Try it out` button and, finally, click `Execute` button.

  It will return a http status code `200` and an empty list or a list with some books if you've already added them

- Now, let's try to call a secured endpoint without authentication. Click `POST /api/books` to open it. Then, click `Try it out` button (you can use the default values) and, finally, click `Execute` button.

  It will return
  ```
  Code: 401
  Details: Error: response status is 401
  ```

- Get the Access Token as explained on section [Getting Access Token](#getting-access-token)

- Copy the token generated and go back to `Swagger`

- Click the `Authorize` button and paste the access token in the `Value` field. Then, click `Authorize` and, to finalize, click `Close`

- Go to `POST /api/books`, click `Try it out` and, finally, click `Execute` button.

  It should return something like
  ```
  HTTP/1.1 201
  {
    "id": "612f502f38e39e473c4d098c",
    "authorName": "Ivan Franchin",
    "title": "SpringBoot",
    "price": 10.5
  }
  ```

## Useful Links & Commands

- **MongoDB**

  List books
  ```
  docker exec -it mongodb mongosh bookdb
  db.books.find()
  ```
  > Type `exit` to get out of MongoDB shell

## Shutdown

- To stop `book-service`, go to the terminal where the application is running and press `Ctrl+C`;
- To stop the Docker containers started using `./init-environment.sh` script, make sure you are in `springboot-keycloak-mongodb-testcontainers` and run the script below:
  ```
  ./shutdown-environment.sh
  ```

## Cleanup

To remove the Docker image created by this project, go to a terminal and, inside `springboot-keycloak-mongodb-testcontainers` root folder, run the following script
```
./remove-docker-images.sh
```

## Running Unit and Integration Tests

- In a terminal and inside `springboot-keycloak-mongodb-testcontainers` root folder, run the command below to run unit and integration tests
  ```
  ./gradlew book-service:clean book-service:assemble \
    book-service:cleanTest \
    book-service:test \
    book-service:integrationTest
  ```
  > **Note**: During integration tests, `Testcontainers` will start automatically `MongoDB` and `Keycloak` containers before the tests begin and shuts them down when the tests finish.

- From `springboot-keycloak-mongodb-testcontainers` root folder, **Unit Testing Report** can be found at
  ```
  book-service/build/reports/tests/test/index.html
  ```
  
- From `springboot-keycloak-mongodb-testcontainers` root folder, **Integration Testing Report** can be found at
  ```
  book-service/build/reports/tests/integrationTest/index.html
  ```
