# court-register

[![CircleCI](https://circleci.com/gh/ministryofjustice/court-register/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/court-register)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://court-register-dev.hmpps.service.justice.gov.uk/swagger-ui.html)

Self-contained fat-jar micro-service to publish court information

## Pre-requisite

`Docker` Even when running the tests docker is used to start `localstack` (for AWS services) and a Postgres database. The build will automatically download and run these containers on your behalf.

## Building

```./gradlew build```

## Running

Various methods to run the application locally are detailed below.

Once up the application should be available on port 8080 - see the health page at http://localhost:80800/health.  

Also try http://localhost:8080/swagger-ui.html to see the API specification.

### Running all services locally

You can run the following command to bring up the application and its dependencies in docker containers:

```bash
./gradlew clean
TMPDIR=/private$TMPDIR docker-compose up 
```

Run command `docker ps` to see which containers are running.

Note that this method of starting the application can be very slow as it involves building both the app and a docker image, but it's handy because it works straight out of the box.

### Running in Intellij

First start all dependencies with the command:

```bash
TMPDIR=/private$TMPDIR docker-compose up --scale court-register=0 
```

The dependencies will run in docker containers - but not the main application.  Use command `docker ps` to see the running containers.

To start the application in Intellij run main class `CourtRegisterApplication` including active profiles `postgres` and `localstack` in the run configuration . 

### Running from the command line

First start all dependencies with the command:

```bash
TMPDIR=/private$TMPDIR docker-compose up --scale court-register=0 
```

The dependencies will run in docker containers - but not the main application.  Use command `docker ps` to see the running containers.

To start the application run command

```bash
./gradlew bootRun -Plocalstack --args='--spring.profiles.active=localstack,postgres'
```

### Authorisation

The query endpoints are not secured and can be called without an auth token.

The update endpoints are secured against the hmpps-auth service which should be running in docker on port 8090.  You will need a client token containing the roles specified in the endpoint definitions in the `resource` package, e.g. role `ROLE_MAINTAIN_REF_DATA` with scope `write`.  A working client has been configured in the auth server for client id `hmpps-registers-ui-client` and client secret `clientsecret`.

## Testing

### Localstack

`localstack` is used to emulate the AWS SNS and SQS services. When running the integration tests localstack is started automatically by TestContainers. 

If you wish to run localstack manually (as is done in the Circle build) then you must:
* start localstack with command `TMPDIR=/private$TMPDIR docker-compose up localstack`
* run the tests with command `AWS_PROVIDER=localstack ./gradlew check`

Any commands in `localstack/setup-sns.sh` will be run when `localstack` starts, so this should contain commands to create the appropriate queues.

### Test Database

The tests run against a Postgres database, not H2, so that we can test Postgres specific functionality such as the text search.

#### Test Data

There is some canned data loaded by Flyway when the Spring context is loaded.  This data is production-like in that it was the initial data set when the service went live.

The rules for manipulating test data are:
* Feel free to rely on the canned data for tests
* If you need to create additional data for a test then delete it afterwards
* DO NOT amend the canned data in any test - other tests may rely on it and there is currently no mechanism in place to reset the data
* If you need to amend data in a test, create it first and then delete it afterwards
* If you need to delete data in a test, create it first

#### External Postgres Instance - local

You can run the tests against an external Postgres database by starting it with:

`docker-compose up court-register-db`

Once running you will not need to restart the container as the tests should behave themselves and reset the data.

#### External Postgres Instance - CircleCI

An external Postgres instance is started during the Circle build and the tests run against that instance.

#### Testcontainers Postgres Instance

If there is no external Postgres database running then the test suite will attempt to use Testcontainers to start a Postgres instance and run the tests against that.

Note that this is slightly slower than running your own external Postgres instance - but not much.

### Architecture

Understanding the architecture makes live support easier:

The service publish to a number of hmpps events

* COURT_REGISTER_INSERT
* COURT_REGISTER_UDPATE

## Run Book

Check Application Insights to show any errors, while the traces will indicate what messages have been received

```bigquery
exceptions
| where cloud_RoleName == "court-register"
| order by timestamp desc 

```

```bigquery
traces
| where cloud_RoleName == "court-register"
| order by timestamp desc 
```

Secrets for the AWS credentials are stored in namespace `hmpps-registers-dev-prod` under `hmpps-domain-events-topic` for the SNS


