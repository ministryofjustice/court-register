# court-register

[![CircleCI](https://circleci.com/gh/ministryofjustice/court-register/tree/master.svg?style=svg)](https://circleci.com/gh/ministryofjustice/court-register)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://court-register-dev.hmpps.service.justice.gov.uk/swagger-ui.html)

Self-contained fat-jar micro-service to publish court information
 
### Building

```bash
./gradlew build
```

### Running

```bash
./gradlew bootRun
```

#### Health

- `/health/ping`: will respond with status `UP` to all requests.  This should be used by dependent systems to check connectivity to court-register,
rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
by court-register health monitoring (e.g. pager duty) and not other systems who wish to find out the state of court-register.
- `/info`: provides information about the version of deployed application.
