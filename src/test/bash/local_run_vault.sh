#!/bin/bash

echo "###########################################################################"
echo "# Start Vault on https://localhost:8200 and http://localhost:8201         #"
echo "###########################################################################"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
VAULT_BIN="${DIR}/../../../vault/vault"

${VAULT_BIN} server \
            -config=${DIR}/vault.conf \
            -dev \
            -dev-root-token-id="00000000-0000-0000-0000-000000000000" \
            -dev-listen-address="0.0.0.0:8201"

exit $?
