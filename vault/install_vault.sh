#!/bin/bash

VAULT_VER="0.5.2"
UNAME=$(uname -s |  tr '[:upper:]' '[:lower:]')
VAULT_ZIP="vault_${VAULT_VER}_${UNAME}_amd64.zip"
IGNORE_CERTS="${IGNORE_CERTS:-no}"

# cleanup
rm "vault_*"
rm "vault"
# install consul
if [[ "${IGNORE_CERTS}" == "no" ]] ; then
  echo "Downloading consul with certs verification"
  wget "https://releases.hashicorp.com/vault/${VAULT_VER}/${VAULT_ZIP}"
else
  echo "WARNING... Downloading consul WITHOUT certs verification"
  wget "https://releases.hashicorp.com/vault/${VAULT_VER}/${VAULT_ZIP}" --no-check-certificate
fi

if [[ $? != 0 ]] ; then
  echo "Cannot download vault"
  exit 1
fi

unzip ${VAULT_ZIP}
chmod a+x vault
# check
./vault --version