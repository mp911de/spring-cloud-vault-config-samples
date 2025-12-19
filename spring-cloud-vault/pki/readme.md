Spring Cloud Vault Config PKI Integration
===========================================

This example uses Spring Cloud Vault Config
usage with the PKI integration.

Property-based configuration registers a Vault-managed
SSL bundle requesting a certificate for the common name `localhost`
to configure TLS for the embedded web server.
Additionally, the configured `RestClient` uses Vault's CA certificate
to validate the server certificate during TLS handshake.

## Running the Example

You can start `CertificateOnDemandApplication` directly and
access the server via [https://localhost:8443](https://localhost:8443).
The application uses Vault within a Testcontainer so make sure to have
a running Docker environment.

**Expect an SSL alert since the used root certificate is not
a known/trusted CA but a self-generated certificate.**

## Notice

Services that are reachable through a common name should synchronize
during SSL certificate retrieval. Multiple certificates will create multiple
active public keys that might cause security alerts.
