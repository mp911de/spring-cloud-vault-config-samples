Spring Cloud Vault Samples
============================


This repository contains examples using Spring Cloud Vault.

## Setup

Open a console in the `vault` directory:

```bash
$ ./install_vault.sh # Download and unzip vault
$ ./create_certificates.sh # Create SSL certificates
$ ./start-vault.sh 
```

Vault is now running. Open another console in the `vault` directory

```bash
$ source source env.sh # Set vault address
$ ./vault init -key-shares=5 -key-threshold=2
```

Vault will display the key shares and the root key. Please note that these values are random
and change upon every init. Please read the instructions carefully when using Vault with real data
otherwise you'll loose your data.
```
Key 1: 99eb89735688ad7a29bb1ff27383bd1005a22a62c97f14357ea4f5f98c1d2c8c01
Key 2: 0c5605b16905794a302603bbeb8f6c8ad5ecf7e877f0e29084f838eba931b86902
Key 3: 7f3d88067c7e355acea4fe756a8b23fc6cd6bc671d7cb0f3d2cc8ae543dc3dc303
Key 4: 3d37062e1704ca2a02073b29c097d5a56e7056e710f515c16b40b9cfe3698bb804
Key 5: 4e5c8b99027f863afc85c6e741939ad3d74a1d687a7947a23d740bc109840e1205
Initial Root Token: 9a63de21-8af7-311a-9a5a-151b6a0d4795

Vault initialized with 5 keys and a key threshold of 2. Please
securely distribute the above keys. When the Vault is re-sealed,
restarted, or stopped, you must provide at least 2 of these keys
to unseal it again.

Vault does not store the master key. Without at least 2 keys,
your Vault will remain permanently sealed.
```

Now unseal Vault and export the root token in the `VAULT_TOKEN` env variable
```bash
$ source source env.sh # Set vault address
$ ./vault unseal 99eb89735688ad7a29bb1ff27383bd1005a22a62c97f14357ea4f5f98c1d2c8c01
$ ./vault unseal 0c5605b16905794a302603bbeb8f6c8ad5ecf7e877f0e29084f838eba931b86902
$ export VAULT_TOKEN=9a63de21-8af7-311a-9a5a-151b6a0d4795
```

## Write Data to Vault

Writes the key-value pair `mykey=value` to the path `secret/my-spring-boot-app`.

```
$ ./vault write secret/my-spring-boot-app mykey=value
```

## AppId Authentication

Now it' time to set up AppId authentication. AppId authentication can use IP Address, Mac Address, static or custom determined UserId's. This example uses the Hex-encoded SHA-256 of the Mac-Address as UserId.

Prerequisite: Identify a connected network interface and store it in `bootstrap.properties`, `spring.cloud.vault.app-id.network-interface`.

```
$ ./mac-address-sha256.sh en0 # Prints the hex-encoded SHA-256
$ ./setup-appid.sh 2dab8c4b8a8cc2f0a191aa4a18f5cc457dbadd07111e23e86f180e84f58b56bf
```

