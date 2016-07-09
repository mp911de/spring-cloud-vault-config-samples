Spring Cloud Vault Config with AppId authentication
===================================================

This example shows how to use Spring Cloud Vault Config
using AppId authentication.

AppId Authentication is enabled by setting `spring.cloud.vault.authentication=appid` and uses
the Mac-Address (the SHA-256 of it) as UserId.

Setting `spring.cloud.vault.app-id.network-interface=en0` allows control over
the network interface that is used to obtain the Mac-Address.

## Running the Example

Now it' time to set up AppId authentication. AppId authentication
can use IP Address, Mac Address, static or custom determined UserId's.
This example uses the Hex-encoded SHA-256 of the Mac-Address as UserId.

Prerequisite: Identify a connected network interface and store it
in `bootstrap.properties`, `spring.cloud.vault.app-id.network-interface`.

```
$ src/test/bash/mac_address_sha256.sh en0 # Prints the hex-encoded SHA-256
$ src/test/bash/setup_appid.sh 2dab8c4b8a8cc2f0a191aa4a18f5cc457dbadd07111e23e86f180e84f58b56bf
```
