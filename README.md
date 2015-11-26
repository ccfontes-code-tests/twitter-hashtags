# twitter-hashtags

## Requirements

- Leiningen 2.5.3 or higher

## Environment variables
You need to set these first:

    TWITTER_HANDLE
    OAUTH_APP_KEY
    OAUTH_APP_SECRET
    OAUTH_CONSUMER_KEY
    OAUTH_CONSUMER_SECRET

## Usage

    lein run

## Generate random statuses updates in user timeline

Type `lein run`, then:

    (in-ns 'twitter-hashtags.core)
    (gen-status-update 20) ; generates 20 statuses, or 10 if input is not provided

## Run tests

    lein repl

## Development

### Development
The command below will open a repl and continuously run the tests.
Type (reset) to reload all namespaces.

    lein repl