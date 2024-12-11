#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
VAULT_BIN="${DIR}/../../../vault/vault"


echo "###########################################################################"
echo "# Preparing policies                                                      #"
echo "###########################################################################"

${VAULT_BIN} policy write read-secret ${DIR}/read-secret-policy.conf

echo "###########################################################################"
echo "# Restoring non-versioned K/V backend at secret/                          #"
echo "###########################################################################"

${VAULT_BIN} secrets disable secret
${VAULT_BIN} secrets enable -path secret -version 1 kv

echo "###########################################################################"
echo "# Mounting versioned K/V backend at versioned/                            #"
echo "###########################################################################"

${VAULT_BIN} secrets enable -path versioned -version 2 kv

nc -w 1 localhost 3306 > /dev/null

if [[ $? == 0 ]] ; then

    echo "###########################################################################"
    echo "# Setup MySQL integration                                                 #"
    echo "###########################################################################"

    echo "vault secrets enable mysql"
    ${VAULT_BIN} secrets enable mysql

    echo 'vault write mysql/config/connection connection_url="spring:vault@tcp(localhost:3306)/"'
    ${VAULT_BIN} write mysql/config/connection connection_url="spring:vault@tcp(localhost:3306)/"

    echo vault write mysql/roles/readonly sql="CREATE USER '{{name}}'@'%' IDENTIFIED BY '{{password}}';GRANT SELECT ON *.* TO '{{name}}'@'%';"
    ${VAULT_BIN} write mysql/roles/readonly sql="CREATE USER '{{name}}'@'%' IDENTIFIED BY '{{password}}';GRANT SELECT ON *.* TO '{{name}}'@'%';"
else
    echo "###########################################################################"
    echo "# MySQL not running, skip MySQL integration setup                         #"
    echo "###########################################################################"
fi

nc -w 1 localhost 8500 > /dev/null

if [[ $? == 0 ]] ; then

    echo "###########################################################################"
    echo "# Setup Consul integration                                                 #"
    echo "###########################################################################"

    echo "vault secrets enable consul"
    ${VAULT_BIN} secrets enable consul

    TOKEN_JSON=$(curl -d "{\"Name\": \"sample\", \"Type\": \"management\"}" \
            -H "X-Consul-Token: consul-master-token" \
            -X PUT \
            http://localhost:8500/v1/acl/create)

    TOKEN=$(echo ${TOKEN_JSON}| cut -c 8-43)

    echo "vault write consul/config/access address=localhost:8500 token=${TOKEN}"
    ${VAULT_BIN} write consul/config/access address=localhost:8500 token=${TOKEN}

    echo "vault write consul/roles/readonly policy=…"
    POLICY=$(echo -n "key \"\" { policy = \"read\" }" | base64)
    ${VAULT_BIN} write consul/roles/readonly policy=${POLICY}

else
    echo "###########################################################################"
    echo "# Consul not running, skip Consul integration setup                         #"
    echo "###########################################################################"
fi

echo "###########################################################################"
echo "# Setup static PKI example                                                #"
echo "###########################################################################"

echo "vault secrets enable pki"
${VAULT_BIN} secrets enable pki

echo "write pki/config/ca pem_bundle=-"
cat work/ca/certs/intermediate.cert.pem work/ca/private/intermediate.decrypted.key.pem | ${VAULT_BIN} write pki/config/ca pem_bundle=-

echo "vault write pki/roles/localhost-ssl-demo allowed_domains=localhost,example.com allow_localhost=true max_ttl=72h"
${VAULT_BIN} write pki/roles/localhost-ssl-demo allowed_domains=localhost,example.com allow_localhost=true max_ttl=72h

echo "###########################################################################"
echo "# Write test data to Vault                                                #"
echo "###########################################################################"

echo "vault kv put secret/my-spring-boot-app mykey=myvalue hello.world='Hello, World'"
${VAULT_BIN} kv put secret/my-spring-boot-app mykey=myvalue hello.world='Hello, World'

echo "vault kv put secret/my-spring-boot-app/cloud key_for_cloud_profile=value mykey=cloud"
${VAULT_BIN} kv put secret/my-spring-boot-app/cloud key_for_cloud_profile=value mykey=cloud

echo "vault kv put secret/my-spring-app database.username=myuser database.password=mypassword"
${VAULT_BIN} kv put secret/my-spring-app database.username=myuser database.password=mypassword

echo "###########################################################################"
echo "# Setup Transit Backend                                                   #"
echo "###########################################################################"

echo "vault secrets enable transit"
${VAULT_BIN} secrets enable transit

echo "vault write -f transit/keys/foo-key"
${VAULT_BIN} write -f transit/keys/foo-key


echo "###########################################################################"
echo "# Setup CloudFoundry example                                              #"
echo "###########################################################################"

echo "vault secrets enable -path=cf/20fffe9d-d8d1-4825-9977-1426840a13db/transit transit"
${VAULT_BIN} secrets enable -path=cf/20fffe9d-d8d1-4825-9977-1426840a13db/transit transit

echo "vault secrets enable -path=cf/20fffe9d-d8d1-4825-9977-1426840a13db/secret kv"
${VAULT_BIN} secrets enable -path=cf/20fffe9d-d8d1-4825-9977-1426840a13db/secret kv

echo "vault write cf/20fffe9d-d8d1-4825-9977-1426840a13db/secret/application app-key=hello-world-app index=1"
${VAULT_BIN} write cf/20fffe9d-d8d1-4825-9977-1426840a13db/secret/application app-key=hello-world-app index=1

# Space
echo "vault secrets enable -path=cf/d007583f-5617-4b02-a5a7-550648827cfa/secret kv"
${VAULT_BIN} secrets enable -path=cf/d007583f-5617-4b02-a5a7-550648827cfa/secret kv

echo "vault write cf/d007583f-5617-4b02-a5a7-550648827cfa/secret/my-cf-app space-key=hello-world-space index=2"
${VAULT_BIN} write cf/d007583f-5617-4b02-a5a7-550648827cfa/secret/my-cf-app space-key=hello-world-space index=2

# Org
echo "vault secrets enable -path=cf/1a558498-59ad-488c-b395-8b983aacb7da/secret kv"
${VAULT_BIN} secrets enable -path=cf/1a558498-59ad-488c-b395-8b983aacb7da/secret kv

echo "vault write cf/1a558498-59ad-488c-b395-8b983aacb7da/secret/my-cf-app org-key=hello-world-org index=3"
${VAULT_BIN} write cf/1a558498-59ad-488c-b395-8b983aacb7da/secret/my-cf-app org-key=hello-world-org index=3

echo "###########################################################################"
echo "# Setup TOTP Backend                                                      #"
echo "###########################################################################"

echo "vault secrets enable totp"
${VAULT_BIN} secrets enable totp
