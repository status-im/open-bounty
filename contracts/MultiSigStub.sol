pragma solidity ^0.4.11;

/**
 * @title MultiSigStub  
 * Contact that delegates calls to a library to build a full MultiSigWallet that is cheap to create. 
 */
contract MultiSigStub {
    mapping (uint => Transaction) public transactions;
    mapping (uint => mapping (address => bool)) public confirmations;
    address[] public owners;
   
    struct Transaction {
        address destination;
        uint value;
        bytes data;
        bool executed;
    }

    function MultiSigStub(address[] _owners, uint _required) {
        bytes4 sig = bytes4(sha3("Constructor(address[],uint256)"));
        uint argarraysize = (2 + _owners.length);
        uint argsize = (1 + argarraysize) * 32;
        uint size = 4 + argsize;
        bytes32 m_data = _malloc(size);

        assembly {
            mstore(m_data, sig)
            codecopy(add(m_data, 0x4), sub(codesize, argsize), argsize)
        }
        _delegatecall(m_data, size);
    }

    function()
        payable
    {
        uint size = msg.data.length;
        bytes32 m_data = _malloc(size);

        assembly {
            calldatacopy(m_data, 0x0, size)
        }

        bytes32 m_result = _delegatecall(m_data, size);

        assembly {
            return(m_result, 0x20)
        }
    }

    function _malloc(uint size) 
        private 
        returns(bytes32 m_data) 
    {
        assembly {
            m_data := mload(0x40)
            mstore(0x40, add(m_data, size))
        }
    }

    function _delegatecall(bytes32 m_data, uint size) 
        private 
        returns(bytes32 m_result) 
    {
        address target = 0xcafecafecafecafecafecafecafecafecafecafe; //will be replaced by correct value
        m_result = _malloc(32);
        bool failed;

        assembly {
            failed := iszero(delegatecall(sub(gas, 10000), target, m_data, size, m_result, 0x20))
        }

        assert(!failed);
    }

    /// @dev Returns list of owners.
    /// @return List of owner addresses.
    function getOwners()
        public
        constant
        returns (address[])
    {
        return owners;
    }

    /// @dev Returns array with owner addresses, which confirmed transaction.
    /// @param transactionId Transaction ID.
    /// @return Returns array of owner addresses.
    function getConfirmations(uint transactionId)
        public
        constant
        returns (address[] _confirmations)
    {
        address[] memory confirmationsTemp = new address[](owners.length);
        uint count = 0;
        uint i;
        for (i=0; i<owners.length; i++)
            if (confirmations[transactionId][owners[i]]) {
                confirmationsTemp[count] = owners[i];
                count += 1;
            }
        _confirmations = new address[](count);
        for (i=0; i<count; i++)
            _confirmations[i] = confirmationsTemp[i];
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
        returns (uint[] _transactionIds)
    {
        uint[] memory transactionIdsTemp = new uint[](transactionCount);
        uint count = 0;
        uint i;
        for (i=0; i<transactionCount; i++)
            if (   pending && !transactions[i].executed
                || executed && transactions[i].executed)
            {
                transactionIdsTemp[count] = i;
                count += 1;
            }
        _transactionIds = new uint[](to - from);
        for (i=from; i<to; i++)
            _transactionIds[i - from] = transactionIdsTemp[i];
    }

}
 