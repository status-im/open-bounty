pragma solidity ^0.4.11;

import "./MultiSigStub.sol";

contract MultiSigFactory {
    
    event Create(address indexed caller, address createdContract);

    function create(address[] owners, uint required) returns (MultisigStub stub){
        stub = new MultiSigStub(owners, required);
        Create(msg.sender, stub);
    }
}