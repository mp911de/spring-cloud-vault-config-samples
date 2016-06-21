#!/bin/bash

###########################################################################
# Start Vault on localhost:8200                                           #
###########################################################################

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd ${DIR}/..
./vault/vault server -config=${DIR}/vault.conf

exit $?
