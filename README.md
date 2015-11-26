# twitter-hashtags

## Requirements

- Leiningen 2.5.3 or higher

## Environment variables
You need to set these first:

    OAUTH_APP_KEY
    OAUTH_APP_SECRET
    OAUTH_CONSUMER_KEY
    OAUTH_CONSUMER_SECRET

## Run the report

    lein run

## Generate bulk random status updates in user timeline

Type `lein run`, then:

    (in-ns 'twitter-hashtags.core)
    (gen-status-update 20) ; generates 20 statuses, or 10 if input is not provided

## Run the tests

    lein repl

## Development

### Development
The command below will open a repl and continuously run the tests.
Type (reset) to reload all namespaces.

    lein repl