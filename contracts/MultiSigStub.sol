pragma solidity ^0.4.18;

import "./DelegatedCall.sol";

/**
 * @title MultiSigStub  
 * @author Ricardo Guilherme Schmidt (Status Research & Development GmbH) 
 * Contract that delegates calls to a library to build a full MultiSigWallet that is cheap to create. 
 */
contract MultiSigStub is DelegatedCall {

    function MultiSigStub(address[] _owners, uint256 _required) {
        //bytes4 sig = bytes4(sha3("constructor(address[],uint256)"));
        bytes4 sig = 0x36756a23;
        uint argarraysize = (2 + _owners.length);
        uint argsize = (1 + argarraysize) * 32;
        uint size = 4 + argsize;
        bytes32 mData = _malloc(size);

        assembly {
            mstore(mData, sig)
            codecopy(add(mData, 0x4), sub(codesize, argsize), argsize)
        }
        _delegatecall(mData, size);
    }
    
    function()
        payable
        delegated
    {

    }

    /// @dev Returns the confirmation status of a transaction.
    /// @param transactionId Transaction ID.
    /// @return Confirmation status.
    function isConfirmed(uint transactionId)
        public
        constant
        delegated
        returns (bool)
    {

    }
    
    /*
    * Web3 call functions
    */

    /// @dev Returns number of confirmations of a transaction.
    /// @param transactionId Transaction ID.
    /// @return Number of confirmations.
    function getConfirmationCount(uint transactionId)
        public
        constant
        delegated
        returns (uint)
    {

    }

    /// @dev Returns total number of transactions after filters are applied.
    /// @param pending Include pending transactions.
    /// @param executed Include executed transactions.
    /// @return Total number of transactions after filters are applied.
    function getTransactionCount(bool pending, bool executed)
        public
        constant
        delegated
        returns (uint)
    {

    }

    /// @dev Returns list of owners.
    /// @return List of owner addresses.
    function getOwners()
        public
        constant
        delegated
        returns (address[])
    {

    }

    /// @dev Returns array with owner addresses, which confirmed transaction.
    /// @param transactionId Transaction ID.
    /// @return Returns array of owner addresses.
    function getConfirmations(uint transactionId)
        public
        constant
        delegated
        returns (address[] _confirmations)
    {

    }

    /// @dev Returns list of transaction IDs in defined range.
    /// @param from Index start position of transaction array.
    /// @param to Index end position of transaction array.
    /// @param pending Include pending transactions.
    /// @param executed Include executed transactions.
    /// @return Returns array of transaction IDs.
    function getTransactionIds(uint from, uint to, bool pending, bool executed) 
        public
        constant
        delegated
        returns (uint[] _transactionIds)
    {

    }

    function _getDelegatedContract()
        internal
        returns(address)
    {
        return 0xCaFFE810d0dF52E27DC580AD4a3C6283B0094291; //hardcoded multinetwork address   
    }
    
}