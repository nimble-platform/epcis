### NIMBLE EPCIS Repository ###
> Note: This Repo is forked from [URL](https://github.com/JaewookByun/epcis).
> This Repo is part of NIMBLE Project (https://www.nimble-project.org).

### Introduction ###
> EPC Information Service (EPCIS) REST APIs enables to capture and query standardized event/master data ratified by GS1.

### Setup ###

> Run `mvn clean package` in the folder, where the file pom.xml locates in, for building the application.

### Run Locally ###

> Run `mvn spring-boot:run' 

### Docker ###

> Run `mvn docker:build` for building docker image for the application.

### Run with Docker ###

> Run 'docker run -d -e EPCIS_DB_URL="mongodb://localhost:27017/" nimbleplatform/epcis:0.0.4' for running the docker image. In default, the local mongoDB "mongodb://localhost:27017/" is used for managing epcis data. When another MongoDB is in use, the enviornment variable EPCIS_DB_HOST_URL should be updated. 

### Quick Test ###
> When it works properly, http://localhost:8080/greeting will return a greeting string.

### Test Application ###
> Set up your base-url and accessToken in application.yml file test section.
```
test:
  base-url: ${EPCIS_TEST_BASE_URL:http://localhost:8080}
  accessToken: ${EPCIS_TEST_TOKEN:token1}
```
To run the test you need IDEA like Eclipse or IntelliJ.





