# twitter-hashtags
Frequency of hashtag usages on a twitter account.

## Requirements

- Leiningen 2.4.0 or higher

## Environment variables
You need to set the environment variables below: 

    TWITTER_HANDLE
    OAUTH_APP_KEY
    OAUTH_APP_SECRET
    OAUTH_CONSUMER_KEY
    OAUTH_CONSUMER_SECRET

using the keys and secrets of your app at [apps.twitter.com](https://apps.twitter.com).

## Run the report

    lein run

## Generate bulk random status updates in user timeline
Create timeline status updates while the report is bootstrapping.

Type `lein run`, then:

```clojure
(in-ns 'twitter-hashtags.core)
(rand-status-update)     ; creates 1 status update
(rand-status-updates 20) ; creates 20 statuses updates, or 1 if input is not provided
```

## Run the tests

    lein repl

## TODO

- resume streaming on failure

## Development
The command below will open a repl and continuously run the tests everytime
a source or test file is saved.

    lein repl

Type (refresh) to reload all namespaces.
