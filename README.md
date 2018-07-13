# Wecky - User Notification
Notifies the user about a changed website using AWS SES.

## Dependencies
* Gets triggered by `S3` bucket `wecky-screens` for new objects. 
* Queries `DynamoDB` for `Website`s and `User`s for the given object. 
* Notifies users using `SES` for mail delivery. 

Region `eu-central-1` is hardcoded for now.

## Build
Building is straightforward, using Maven:
```bash
mvn package
```
The shade plugin is already attached to the package phase, so the resulting JAR can be uploaded to AWS.
Alternatively you can use the `buildspec.yml` file to build the project in AWS CodeStar.

## Test
[Localstack](https://github.com/localstack/localstack) is used during tests, to spin up local docker containers with mocked AWS Services. 
If you want to skip the end-2-end tests you can run the tests using
````bash
mvn package -DargLine="-Dci-server=true"
````  

## Running locally
Using [sam-cli](https://docs.aws.amazon.com/lambda/latest/dg/sam-cli-requirements.html) and [localstack](https://github.com/localstack/localstack) you can run the lambda function locally.

You need to set the environment variable `DYNDB_LOCAL` to point to the local DynamoDB that gets started by localstack.

:information_source:
Environment variables need to be set in the `template.yml` file, since the Lambda Function is run in a docker container.
See `template_sam.yml` for an example.

Then run `sam local invoke "TriggerNotification" -e event.json`
