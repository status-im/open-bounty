#!/bin/bash -eu

function print_dependency_message
{
    echo "error: " $1 "must already be installed!"   
}

SOLC=$(which solc) || print_dependency_message "solc"

WEB3J=$(which web3j) || print_dependency_message "web3"

rm -f resources/contracts/*.{abi,bin}

# compile contracts
for f in {TokenReg,MultiSigTokenWallet*}.sol; do
    $SOLC $f --overwrite --bin --abi --optimize -o resources/contracts
done

# generate java classes
for f in resources/contracts/*.bin; do
    abi=$(echo $f|sed s/\.bin/.abi/)
    $WEB3J solidity generate $f $abi -o src/java -p commiteth.eth.contracts
done
