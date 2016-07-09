#!/bin/bash

echo "###########################################################################"
echo "# Setup static AppId authentication                                       #"
echo "###########################################################################"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
VAULT_BIN="${DIR}/../../../vault/vault"

echo "vault auth-enable app-id"
${VAULT_BIN} auth-enable app-id

echo "vault write auth/app-id/map/app-id/my-spring-boot-app value=root display_name=spring-boot-app"
${VAULT_BIN} write auth/app-id/map/app-id/my-spring-boot-app value=root display_name=spring-boot-app

echo "vault write auth/app-id/map/user-id/my-static-userid value=my-spring-boot-app"
${VAULT_BIN} write auth/app-id/map/user-id/my-static-userid value=my-spring-boot-app


nc -w 1 localhost 3306 > /dev/null

if [[ $? == 0 ]] ; then

    echo "###########################################################################"
    echo "# Setup MySQL integration                                                 #"
    echo "###########################################################################"

    echo "vault mount mysql"
    ${VAULT_BIN} mount mysql

    echo 'vault write mysql/config/connection connection_url="spring:vault@tcp(localhost:3306)/"'
    ${VAULT_BIN} write mysql/config/connection connection_url="spring:vault@tcp(localhost:3306)/"

    echo vault write mysql/roles/readonly sql="CREATE USER '{{name}}'@'%' IDENTIFIED BY '{{password}}';GRANT SELECT ON *.* TO '{{name}}'@'%';"
    ${VAULT_BIN} write mysql/roles/readonly sql="CREATE USER '{{name}}'@'%' IDENTIFIED BY '{{password}}';GRANT SELECT ON *.* TO '{{name}}'@'%';"
else
    echo "###########################################################################"
    echo "# MySQL not running, skip MySQL integration setup                         #"
    echo "###########################################################################"
fi


echo "###########################################################################"
echo "# Write test data to Vault                                                #"
echo "###########################################################################"

echo "vault write secret/my-spring-boot-app mykey=myvalue"
${VAULT_BIN} write secret/my-spring-boot-app mykey=myvalue

echo "vault write secret/my-spring-boot-app/cloud key_for_cloud_profile=value"
${VAULT_BIN} write secret/my-spring-boot-app/cloud key_for_cloud_profile=value

