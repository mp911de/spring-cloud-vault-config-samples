#!/bin/bash

echo "###########################################################################"
echo "# Start Vault on http://localhost:8500                                    #"
echo "###########################################################################"

BASEDIR=`dirname $0`/../../..
CONSUL_BIN="${BASEDIR}/consul/consul"

mkdir -p ${BASEDIR}/consul/config
mkdir -p ${BASEDIR}/consul/data

${CONSUL_BIN} agent -server \
            -bootstrap-expect 1 \
            -data-dir ${BASEDIR}/consul/data \
            -config-file=${BASEDIR}/src/test/bash/consul.json

exit $?
