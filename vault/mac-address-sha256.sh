#!/bin/bash

if [[ "" == "$1" ]] ; then
    echo "Usage: $0 NETWORK_DEVICE_NAME"
    exit 1  
fi

echo "ifconfig | grep $1 -A 3 | grep ether | tr [:lower:] [:upper:] | cut -f 2 -d' ' | tr -d '\n\r:' | sha256sum"
ifconfig | grep $1 -A 3 | grep ether | tr [:lower:] [:upper:] | cut -f 2 -d' ' | tr -d '\n\r:' | sha256sum
