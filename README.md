# Kinesis Multilang

This project wraps Amazons Kinesis Multilang daemon as a Gradle project. 
I eventually want to add some code to make using a properties file unnecessary,
but that's down the line.

## Why?
I was working on a way to use Amazon's Kinesis Client Library for an Elxiir 
project and found that many implementations using the MultiLangDaemon were
re-implementing code to fetch dependencies (or worse, bundle jars in the source tree)
and start the daemon.  While polishing an existing Elixir implementation
I found the code unnecessary and wanted a way for my employers operations 
team to upgrade and deploy the java dependencies in a sane way because we don't run
Gradle or Maven in production and wanted to have something easily shippable.
We already knew how to build and run Gradle apps so I wrote a simple `build.gradle` to do that.

## Usage
Checkout the code and run the following in the repo root. It should handle downloaded dependencies:
```
./gradlew installDist
```
Edit `sample.properties` and set the `executableName` to the absolute path of a script 
that follows the multilang daemon API. Run the project with: 

```
./build/install/kinesis-multilang-app/bin/kinesis-multilang-app sample.properties
``` 
And you should see some aws messages and the multilang daemon logging. the `sample.properties` 
was pulled from the AWS repo and should be pretty straightforward. You'll want to ship your own
version obviously for your deployment scenarios

