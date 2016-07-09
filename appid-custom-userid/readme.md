Spring Cloud Vault Config with AppId authentication and custom UserId
=====================================================================

This example shows how to use Spring Cloud Vault Config
using AppId authentication.

AppId Authentication is enabled by setting `spring.cloud.vault.authentication=appid` and a
custom UserId mechanism class `example.appid.custom.CustomUserIdMechanism`.

## Running the Example

This example will read from Vault using the application
name `my-spring-boot-app` and the generic context name
`application` from the generic secret backend.
It will obtain a token by using AppId authentication
and a custom UserId mechanism that generates a static
UserId.

Make sure you initialized Vault using the
`src/test/bash/setup_examples.sh` script.

It will write data to Vault using:

```bash
$ vault write secret/my-spring-boot-app mykey=myvalue
$ vault write secret/my-spring-boot-app/cloud key_for_cloud_profile=value
$ vault auth-enable app-id
$ vault write auth/app-id/map/app-id/my-spring-boot-app value=root display_name=spring-boot-app
$ vault write auth/app-id/map/user-id/my-static-userid value=my-spring-boot-app
```

and it will prepare AppId authentication using:

```bash
$ vault auth-enable app-id
$ vault write auth/app-id/map/app-id/my-spring-boot-app value=root display_name=spring-boot-app
$ vault write auth/app-id/map/user-id/my-static-userid value=my-spring-boot-app
```