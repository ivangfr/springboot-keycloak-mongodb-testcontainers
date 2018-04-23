# springboot-testing-mongodb-keycloak

## Goal

The goals of this project are:

1. Create a REST API application to manage books, `book-service`. The application will have its endpoints related to add/update/delete books secured.
2. Use `Keycloak` as authentication and authorization server;
3. Explore the utilities and annotations that Spring Boot provides when testing applications.

## Start Environment

### Docker Compose

1. Open one terminal

2. Inside `/springboot-testing-mongodb-keycloak/dev` folder run
```
docker-compose up
```

### Keycloak

1. Access the link
```
http://localhost:8181
```

2. Login with the credentials
```
Username: admin
Password: admin
```

3. Create a new Realm
- Go to top-left corner and hover the mouse over `Master` realm. A blue button `Add realm` will appear. Click on it.
- On `Name` field, write `company-services`. Click on `Create`.

4. Create a new Client
- Click on `Clients` menu on the left.
- Click `Create` button.
- On `Client ID` field type `book-service`.
- Click on `Save`.
- On `Settings` tab, set the `Access Type` to `confidential`
- Still on `Settings` tab, set the `Valid Redirect URIs` to `http://localhost:8080/*`
- Click on `Save`.
- Go to `Credentials` tab. Copy the value on `Secret` field. It will be used 1) on `springboot-testing-mongodb-keycloak` start and 2) to get the access token.

5. Create a new Role
- Click on `Roles` menu on the left.
- Click `Add Role` button.
- On `Role Name` type `manage_books`.
- Click on `Save`.

6. Create a new User
- Click on `Users` menu on the left.
- Click on `Add User` button.
- On `Username` field set `ivan.franchin`
- Click on `Save`
- Go to `Credentials` tab
- Set to `New Password` and `Password Confirmation` the value `123`
- Turn off the `Temporary` field
- Click on `Reset password`
- Confirm the pop up clicking on `Change Password`
- Go to `Role Mappings` tab and add the role `manage_books` to `ivan.franchin`.

**Done!** That is all the configuration needed on Keycloak. 

### Spring Boot Application

1. Open a new terminal

2. Start `springboot-testing-mongodb-keycloak` application

In `springboot-testing-mongodb-keycloak` root folder, run:
```
gradle clean build
java -jar build/libs/springboot-testing-mongodb-keycloak-0.0.1-SNAPSHOT.jar -Dclient_secret=<keycloak_generated_client_secret> 
```

## Test using cUrl

1. Open a new terminal

2. Call the endpoint `GET /api/books` using the cURL command bellow.
```
curl -i 'http://localhost:8080/api/books'
```
It will return:
```
Code: 200
Response Body: []
```

3. Try to call the endpoint `POST /api/books` using the cURL command bellow.
``` 
curl -i -X POST 'http://localhost:8080/api/books' \
  -H "Content-Type: application/json" \
  -d '{ "authorName": "ivan", "title": "java 8", "price": 10.5 }'
```
It will return:
```
Code: 302
```

4. Run the commands bellow to get an access token for `ivan.franchin` user.
```
BOOKSERVICE_CLIENT_SECRET=81a1cd01-796a-4a61-8d41-5893bf4a30b3

MY_ACCESS_TOKEN=$(curl -s -X POST \
  "http://localhost:8181/auth/realms/company-services/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=ivan.franchin" \
  -d "password=123" \
  -d "grant_type=password" \
  -d "client_secret=$BOOKSERVICE_CLIENT_SECRET" \
  -d "client_id=book-service" | jq -r .access_token)
```

6. Call the endpoint `POST /api/books` using the cURL command bellow.
```
curl -i -X POST 'http://localhost:8080/api/books' \
  -H "Authorization: Bearer $MY_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "authorName": "ivan", "title": "java 8", "price": 10.5 }'
```
It will return:
```
Code: 201
Response Body: {"id":"01d984be-26bc-49f5-a201-602293d62b82","title":"java 8","authorName":"ivan","price":10.5}
```

## Running unit and integration testing (TODO)

1. In order to run unit and integration testing type
```
gradle test integrationTest
```

2. From `springboot-testing-mongodb-keycloak` root folder, unit testing report can be found in
```
/build/reports/tests/test/index.html
```

3. From `springboot-testing-mongodb-keycloak` root folder, integration testing report can be found in
```
/build/reports/tests/integrationTest/index.html
```

## More about testing Spring Boot Applications

https://github.com/ivangfr/springboot-testing-mysql#more-about-testing-spring-boot-applications