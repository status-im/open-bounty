pragma solidity ^0.4.12;

/**
 * @title DelegatedCall
 * @author Ricardo Guilherme Schmidt (Status Research & Development GmbH) 
 * Abstract contract that delegates all calls to contract returned by abstract function `_getDelegatedContract`
 */
contract DelegatedCall {

    /**
     * @dev delegates the call of this function
     */
    modifier delegated 
    {
        uint inSize = msg.data.length;
        bytes32 inDataPtr = _malloc(inSize);

        assembly {
            calldatacopy(inDataPtr, 0x0, inSize)
        }

        uint256 outSize;
        bytes32 outDataPtr;

        _delegatecall(inDataPtr, inSize);
        _;
        assembly {
            return(0, outSize)
        }
    }

    /**
     * @dev defines the address for delegation of calls
     */
    function _getDelegatedContract()
        internal
        returns(address);

    /**
     * @dev allocates memory to a pointer
     */
    function _malloc(uint size) 
        internal 
        returns(bytes32 ptr) 
    {
        assembly {
            ptr := mload(0x40)
            mstore(0x40, add(ptr, size))
        }
    }

    /**
     * @dev delegates the data in pointer 
     */
    function _delegatecall(bytes32 inDataPtr, uint inSize) 
        internal 
        returns(bytes32 outDataPtr, uint256 outSize) 
    {
        outSize = 0x20;
        address target = _getDelegatedContract();
        outDataPtr = _malloc(outSize);
        bool failed;

        assembly {
            failed := iszero(delegatecall(sub(gas, 10000), target, inDataPtr, inSize, outDataPtr, outSize))
        }

        assert(!failed);
    }

}
