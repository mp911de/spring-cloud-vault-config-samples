Spring Cloud Vault Config Hello World
=====================================

This example explains basic Spring Cloud Vault Config
usage with Tokens.

Make sure to have Vault initialized. Set the token in
`src/main/resources/bootstrap.properties` to the property `spring.cloud.vault.token`.

Vault runs with SSL enabled so make sure the application runs
in the current directory so it can find `work/keystore.jks`.