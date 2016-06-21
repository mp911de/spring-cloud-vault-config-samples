Spring Cloud Vault Config with AppId authentication and custom UserId
=====================================================================

This example shows how to use Spring Cloud Vault Config
using AppId authentication.

AppId Authentication is enabled by setting `spring.cloud.vault.authentication=appid` and a
custom UserId mechanism class `example.appid.custom.CustomUserIdMechanism`.

Vault runs with SSL enabled so make sure the application runs
in the current directory so it can find `work/keystore.jks`.