#!/usr/bin/env bash
set -e
export TERM=ansi
export AWS_ACCESS_KEY_ID=foobar
export AWS_SECRET_ACCESS_KEY=foobar
export AWS_DEFAULT_REGION=eu-west-2
export PAGER=

aws --endpoint-url=http://localhost:4575 sns create-topic --name hmpps_domain_events
aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name hmpps_audit_queue

echo All Ready
