pragma solidity ^0.4.15;

/**
 * @title MultiSigStub  
 * Contact that delegates calls to a library to build a full MultiSigWallet that is cheap to create. 
 */
contract MultiSigStub {

    function MultiSigStub(address[] _owners, uint256 _required) {
        //bytes4 sig = bytes4(sha3("Constructor(address[],uint256)"));
        bytes4 sig = 0xe0c4e63b;
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
        address target = 0x370A93cd1DC15875fF02aa0b952D44Bb3dD905E5; //will be replaced by correct value
        m_result = _malloc(32);
        bool failed;

        assembly {
            failed := iszero(delegatecall(sub(gas, 10000), target, m_data, size, m_result, 0x20))
        }

        assert(!failed);
    }

}
 