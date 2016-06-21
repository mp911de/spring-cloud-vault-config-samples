Spring Cloud Vault Config with AppId authentication
===================================================

This example shows how to use Spring Cloud Vault Config
using AppId authentication.

AppId Authentication is enabled by setting `spring.cloud.vault.authentication=appid` and uses
the Mac-Address (the SHA-256 of it) as UserId.

Setting `spring.cloud.vault.app-id.network-interface=en0` allows control over
the network interface that is used to obtain the Mac-Address.

Vault runs with SSL enabled so make sure the application runs
in the current directory so it can find `work/keystore.jks`.