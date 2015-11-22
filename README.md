# twitter-hashtags

## Requirements

- Leiningen 2.5.3 or higher

## Release
Will compile ClojureScript and Garden files for production.

    lein dist

## Environment variables
You need to set these first:

    TWITTER_HANDLE
    OAUTH_APP_KEY
    OAUTH_APP_SECRET
    OAUTH_CONSUMER_KEY
    OAUTH_CONSUMER_SECRET

## Usage

    lein run

Then in the browser type: `http://localhost:3000`

## Run tests
Will run tests with a proper app state. There's no independent setup/teardown
yet for tests.

    lein repl

## Development

### Development with Clojure
The command below will open a repl, start the app and continuously run the tests.

    lein repl

It also supports reloading all the namespaces and restart the app with a clean
initial state with `(reset)`.

### Development with ClojureScript
To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min