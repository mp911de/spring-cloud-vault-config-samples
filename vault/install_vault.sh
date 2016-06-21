#!/bin/bash

###########################################################################
# Download and Install Vault                                              #
# This script is prepared for caching of the download directory           #
###########################################################################

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
VAULT_VER="0.6.0"
UNAME=$(uname -s |  tr '[:upper:]' '[:lower:]')
VAULT_ZIP="vault_${VAULT_VER}_${UNAME}_amd64.zip"
IGNORE_CERTS="${IGNORE_CERTS:-no}"

# cleanup
mkdir -p ${DIR}/../download

if [[ ! -f "${DIR}/../download/${VAULT_ZIP}" ]] ; then
    cd ${DIR}/../download
    # install Vault
    if [[ "${IGNORE_CERTS}" == "no" ]] ; then
      echo "Downloading Vault with certs verification"
      wget "https://releases.hashicorp.com/vault/${VAULT_VER}/${VAULT_ZIP}"
    else
      echo "WARNING... Downloading Vault WITHOUT certs verification"
      wget "https://releases.hashicorp.com/vault/${VAULT_VER}/${VAULT_ZIP}" --no-check-certificate
    fi

    if [[ $? != 0 ]] ; then
      echo "Cannot download Vault"
      exit 1
    fi
    cd ..
fi

cd ${DIR}

if [[ -f vault ]] ; then
  rm vault
fi

unzip ${DIR}/../download/${VAULT_ZIP}
chmod a+x vault

# check
./vault --version