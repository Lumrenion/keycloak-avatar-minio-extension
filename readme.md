# Disclaimer
The original project does not seem to be supported anymore. That is why I 
created this fork and changed it so that it works for the recent Keycloak 
version, which is 12.0.4. I also bumped all dependencies and Java JDK to 
version 16.

The plugin was designed to be used in a [Docker](https://www.docker.com/) 
environment. That is why I decided to make it configurable via environment
variables rather than the Keycloak XML configuration.

The code was changed so that it >works<, which means I removed usage of the
Service Provider Interface (SPI), which I did not manage to get working.

# Simple extension for managing avatar images in Keycloak

This PoC extension exposes a custom realm resource to manage user avatars.
The avatar images are stored in a [minio](https://www.minio.io/) backend.

Avatar images can either be uploaded via the account page of the custom
account theme `account-avatar` or via the `AvatarResource` custom realm resource.

<img src="keycloak-avatar-demo.png" alt="Keycloak Avatar Demo">

## Start the minio backend

Have a look at this example docker-compose.yml
```yaml
version: '3'

services:
  keycloak:
    image: jboss/keycloak
    container_name: keycloak
    restart: unless-stopped
    environment:
      - "MINIO_SERVER_URL=http://avatarsdb:9000"
      - MINIO_ACCESS_KEY=${YOUR_ACCESS_KEY}
      - MINIO_SECRET_KEY=${YOUR_SECRET_KEY}
    networks:
      - keycloak
    volumes:
      - ./keycloak/extensions:/opt/jboss/keycloak/standalone/deployments
    
  avatarsdb:
    image: minio/minio
    command: server /data
    restart: unless-stopped
    environment:
      - MINIO_ACCESS_KEY=${YOUR_ACCESS_KEY}
      - MINIO_SECRET_KEY=${YOUR_SECRET_KEY}
    networks:
      - keycloak
    volumes:
      - ./keycloak/avatarsdb/data:/data
      - ./keycloak/avatarsdb/config:/root/.minio


networks:
  keycloak:
    driver: bridge
```
Replace `${YOUR_ACCESS_KEY}` and `${YOUR_SECRET_KEY}` and the MinIO backend 
should be accessible by Keycloak. You might want to add more environment
variables to the keycloak service, like the admin user and password as well as 
the database settings.

## Build the example
```
mvn clean verify
```

## Deploy the example
Copy both files `avatar-minio-extension-bundle/target/avatar-minio-extension-bundle-1.0.1.0-SNAPSHOT.ear` 
and `avatar-minio-extension-module/target/avatar-minio-extension-module-1.0.1.0-SNAPSHOT.ear` 
to `./keycloak/extensions` or, if you are not using the above docker configuration, 
to `standalone/deployments/` in your Keycloak root directory.

## Uploading an avatar image via account theme
Configure the `avatar-account` theme as account theme in the realm settings.

Open the account page for a user and select and upload an image.

## Uploading an avatar image via CURL

**Disclaimer: Uploading an avatar image via CURL is untested but should work just like in the original project.**

Retrieve access token
```
KC_USERNAME=tester
KC_PASSWORD=test
KC_CLIENT=admin-cli
KC_CLIENT_SECRET=""
KC_REALM=avatar-demo
KC_URL=http://localhost:8080/auth
KC_RESPONSE=$( \
   curl -k \
        -d "username=$KC_USERNAME" \
        -d "password=$KC_PASSWORD" \
        -d 'grant_type=password' \
        -d "client_id=$KC_CLIENT" \
        -d "client_secret=$KC_CLIENT_SECRET" \
        "$KC_URL/realms/$KC_REALM/protocol/openid-connect/token" \
    | jq .
)

KC_ACCESS_TOKEN=$(echo $KC_RESPONSE| jq -r .access_token)
KC_ID_TOKEN=$(echo $KC_RESPONSE| jq -r .id_token)
KC_REFRESH_TOKEN=$(echo $KC_RESPONSE| jq -r .refresh_token)
```

## Upload avatar image via account page

Goto account page and click on 'Choose File' and click save.

## Retrieve avatar image
 
```
 curl -v \
   -H "Authorization: Bearer $KC_ACCESS_TOKEN" \
   http://localhost:8080/auth/realms/$KC_REALM/avatar-provider/avatar \
   -o output.png
```
If you are signed in and want to retrieve your own avatar image, you can just 
visit this URL in the browser which is signed in: http://localhost:8080/auth/realms/${KC_REALM}/avatar-provider/avatar

You can retrieve the avatar image of any keycloak user by adding a GET parameter 
`user=${USER_UUID}`, e.g. http://localhost:8080/auth/realms/${KC_REALM}/avatar-provider/avatar?user=${USER_UUID}.

This can be done without being signed in, means the avatar images are generally public, with a secret ID for each user 
(security by obscurity).

## Add Mapper for avatar url
If you want to send the avatar URL to a SSO client, this can be done by using 
a JavaScript Mapper, like so:
```javascript
/**
 * Available variables:
 * user - the current user
 * realm - the current realm
 * clientSession - the current clientSession
 * userSession - the current userSession
 * keycloakSession - the current keycloakSession
 * @see https://stackoverflow.com/questions/52518298/how-to-create-a-script-mapper-in-keycloak
 */

var baseUri = keycloakSession.getContext().getUri().getBaseUri().toString();
var realmName = realm.getName();
var userId = user.getId();
exports = baseUri + "realms/" + realmName + "/avatar-provider/?user=" + userId;
```