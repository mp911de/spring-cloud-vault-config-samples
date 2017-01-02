Spring Cloud Vault Config PKI Integration
===========================================

This example uses Spring Cloud Vault Config
usage with the PKI integration.

`VaultPkiConfiguration` will obtain a SSL certificate
from Vault and configure Spring Boot's embedded Web-Server
accordingly.

## Running the Example

The example setup script `src/test/bash/setup_examples.sh`
will mount and configure the `pki` backend.

You can start `CertificateOnDemandApplication` directly and
access the server via [https://localhost:8443](https://localhost:8443). 

**Expect an SSL alert since the used root certificate is not
a known/trusted CA but a self-generated certificate.**

## Notice

Services that are reachable through a common name should synchronize
during SSL certificate retrieval. Multiple certificates will create multiple
active public keys that might cause security alerts.
