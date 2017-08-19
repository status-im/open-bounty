#!/bin/bash -eu

SOLC=$(which solc)
WEB3J=$(which web3j)

# compile contracts
for f in contracts/*.sol; do
    $SOLC $f --bin --abi --optimize -o resources/contracts
done

# generate java classes
for f in resources/contracts/*.bin; do
    abi=$(echo $f|sed s/\.bin/.abi/)
    $WEB3J solidity generate $f $abi -o src/java -p commiteth.contracts
done
