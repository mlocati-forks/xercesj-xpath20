# Releasing on Maven Central

## Once

1. Create an account on the [Central Portal](https://central.sonatype.com), and verify the namespace of the groupId (see [Namespace](https://central.sonatype.org/register/namespace/)).
2. Create a GPG key, and publish it on a key server, for example:
   ```
   gpg --keyserver keyserver.ubuntu.com --send-keys <key id>
   ```
3. To upload with the API, generate a user token in the Central Portal (View Account, Generate User Token).

## Every release

1. Build the bundle from a git checkout of the released commit (or with `SOURCE_DATE_EPOCH` set to its date), with Java 8 and in the UTC time zone, like the CI does, so that the jars are the same as the ones built by the CI (compare the `.sha512` files in `build/dist` with the `jars` artifact of the CI):
   ```
   ANT_OPTS=-Duser.timezone=UTC ant maven-bundle -Dversion=<version> -Dmaven.groupId=<groupId> -Dmaven.artifactId=<artifactId>
   ```
   gpg signs the files with its default key: add `-Dgpg.keyname=<key id>` to use another one.
2. Upload `build/dist/<artifactId>-<version>-bundle.zip`, in the Central Portal (Publish Component) or with the [Publisher API](https://central.sonatype.org/publish/publish-portal-api/):
   ```
   curl --request POST --header "Authorization: Bearer <base64 of username:password of the token>" --form bundle=@build/dist/<artifactId>-<version>-bundle.zip https://central.sonatype.com/api/v1/publisher/upload
   ```
3. When the Central Portal has validated the deployment, publish it there.
4. Tag the released commit.
