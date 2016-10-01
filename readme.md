Spring Cloud Vault Examples
============================

This repository contains examples using Spring Cloud Vault.

## Setup

Open a console in the examples root directory and execute
the following commands to setup Vault:

```bash
$ src/test/bash/create_certificates.sh # Create SSL certificates
$ src/test/bash/install_vault.sh # Download and unzip vault
$ src/test/bash/local_run_vault.sh &
```

Vault is now running in dev-mode listening on
https://localhost:8200 and http://localhost:8201.

Vault requires some configuration before you can run the examples.

```bash
$ source src/test/bash/env.sh
$ src/test/bash/setup_examples.sh
```

The root token is set to `00000000-0000-0000-0000-000000000000`
and Vault is running in dev mode.

You can use Vault now from the console or run the examples. Check
out the example-specific readme's for further instructions/requirements.

Vault runs with SSL enabled so make sure the application runs
in the current directory so it can find `work/keystore.jks`.

## Using Vault from the Command Line

You can use a shortcut to setup the token and Vault CLI environment by sourcing
`vault/env.sh`

```bash
$ source src/test/bash/env.sh # Set Vault address, Token and CA path
```

## Write Data to Vault

Writes the key-value pair `mykey=value` to the path `secret/my-spring-boot-app`.

```
$ ./vault write secret/my-spring-boot-app mykey=value
```
