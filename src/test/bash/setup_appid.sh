#!/bin/bash

if [[ "" == "$1" ]] ; then
    echo "Usage: $0 HEX-ENCODED-SHA-256-MAC-ADDRESS"
    exit 1  
fi

echo "###########################################################################"
echo "# Setup AppId authentucation for a provided UserId                        #"
echo "###########################################################################"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
VAULT_BIN="${DIR}/../../../vault/vault"

echo "vault auth-enable app-id"
${VAULT_BIN} auth-enable app-id

echo "vault write auth/app-id/map/app-id/my-spring-boot-app value=root display_name=spring-boot-app"
${VAULT_BIN} write auth/app-id/map/app-id/my-spring-boot-app value=root display_name=spring-boot-app

echo "vault write auth/app-id/map/user-id/$1 value=my-spring-boot-app"
${VAULT_BIN} write auth/app-id/map/user-id/$1 value=my-spring-boot-app

