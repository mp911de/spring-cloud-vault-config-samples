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
