Vote counter
============

## Development
Please remember to update [application.conf.template](application.conf.template) when adding new configurable properties.

## Deployment

### Configuration
1. Create a keystore under `src/main/resources` with an RSA key, note down the keystore password you pick.
    ```
    keytool -genkey -alias '*' -keystore src/main/resources/keystore.jks -keyalg RSA -keysize 2048 -sigalg "SHA1withRSA"
    ```

2. Create an `application.conf` file based on [the template](application.conf.template).

    **NOTE:** if you pick a name other than `application.conf` for your config file, be sure to update the [Dockerfile](Dockerfile) accordingly if you plan on using the [Docker](https://www.docker.com/) image.

3. Set the `vote-counter.web.keystore-password` to the keystore password you chose in step 1.

### Running

1. Build the fat JAR:
    ```
    sbt assembly
    ```

2. Run the JAR manually or use the Docker image. The server listens on `8080` for HTTP and on `8090` for HTTPS requests.

#### Running manually
```
java -cp java -cp target/scala-2.11/vote-counter-assembly-1.0.jar -Dconfig.file=application.conf com.softwaremill.votecounter.web.VoteCounterWeb
```

#### Running using the [Docker](https://www.docker.com/) image
1. Build the image:
    ```
    docker build -t softwaremill/vote-counter .
    ```

2. Run the image in detached mode with port forwarding enabled:
    ```
    docker run -d -P softwaremill/vote-counter
    ```
