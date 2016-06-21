Spring Cloud Vault Config MySQL Integration
===========================================

This example uses Spring Cloud Vault Config
usage with the MySQL integration.

Vault will obtain Username/Password for you so you'll be able to
use a generated login.

Make sure to have Vault initialized. Set the token in
`src/main/resources/bootstrap.properties` to the property
`spring.cloud.vault.token`.

Please initialize Vault and follow the MySQL setup guide at
https://www.vaultproject.io/docs/secrets/mysql/index.html.
You need a running MySQL instance and you need to set the
`spring.cloud.vault.mysql.role` property.

Vault runs with SSL enabled so make sure the application runs
in the current directory so it can find `work/keystore.jks`.