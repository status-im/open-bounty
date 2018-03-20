#!/bin/bash -eu

SOLC=$(which solc)
WEB3J=$(which web3j)

rm -f resources/contracts/*.{abi,bin}

# compile contracts
for f in contracts/{TokenReg,MultiSigTokenWallet*}.sol; do
    $SOLC $f --overwrite --bin --abi --optimize -o resources/contracts
done

# generate java classes
for f in resources/contracts/*.bin; do
    abi=$(echo $f|sed s/\.bin/.abi/)
    $WEB3J solidity generate $f $abi -o src/java -p commiteth.eth.contracts
done
