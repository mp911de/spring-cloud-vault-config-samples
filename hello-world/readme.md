Spring Cloud Vault Config Hello World
=====================================

This example explains basic Spring Cloud Vault Config
usage with Tokens.

## Running the Example

This example will read from Vault using the application
name `my-spring-boot-app` and the generic context name
`application` from the generic secret backend.

```
/v1/secret/my-spring-boot-app
/v1/secret/application
```

activating profiles will read from both resources
and append the profile name for each active profile

```
/v1/secret/my-spring-boot-app/cloud
/v1/secret/application/cloud
```

Make sure you initialized Vault using the
`src/test/bash/setup_examples.sh` script.

It will write data to Vault using:

```bash
$ vault write secret/my-spring-boot-app mykey=myvalue
$ vault write secret/my-spring-boot-app/cloud key_for_cloud_profile=value
```