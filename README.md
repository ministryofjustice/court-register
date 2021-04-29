# court-register

[![CircleCI](https://circleci.com/gh/ministryofjustice/court-register/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/court-register)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://court-register-dev.hmpps.service.justice.gov.uk/swagger-ui.html)

Self-contained fat-jar micro-service to publish court information

### Pre-requisite

`Docker` Even when running the tests docker is used by the integration test to load `localstack` (for AWS services). The build will automatically download and run `localstack` on your behalf.

### Building

```./gradlew build```

### Running

`localstack` is used to emulate the AWS SNS service. When running the integration test this will be started automatically. If you want the tests to use an already running version of `locastack` run the tests with the environment `AWS_PROVIDER=localstack`. This has the benefit of running the test quicker without the overhead of starting the `localstack` container.

Any commands in `localstack/setup-sns.sh` will be run when `localstack` starts, so this should contain commands to create the appropriate queues.

Running all services locally:
```bash
TMPDIR=/private$TMPDIR docker-compose up 
```
Queues and topics will automatically be created when the `localstack` container starts.

Running all services except this application (hence allowing you to run this in the IDE)

```bash
TMPDIR=/private$TMPDIR docker-compose up --scale court-register=0 
```

Check the docker-compose file for sample environment variables to run the application locally.

Or to just run `localstack` which is useful when running against an a non-local test system

```bash
TMPDIR=/private$TMPDIR docker-compose up localstack 
```

In all of the above the application should use the host network to communicate with `localstack` since AWS Client will try to read messages from localhost rather than the `localstack` network.
### Experimenting with messages


### Testing

Note that the integration tests currently use TestContainers to start localstack and so you do not need to start localstack manually.

If you DO wish to run localstack manually (as is done in the Circle build) then you must:
* start localstack with command `TMPDIR=/private$TMPDIR docker-compose up localstack`
* run the tests with command `AWS_PROVIDER=localstack ./gradlew check`

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


