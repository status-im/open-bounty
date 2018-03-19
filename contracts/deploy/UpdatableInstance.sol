pragma solidity ^0.4.17;

import "./Instance.sol";


/**
 * @title UpdatableInstance
 * @author Ricardo Guilherme Schmidt (Status Research & Development GmbH) 
 * @dev Contract that can be updated by a call from itself.
 */
contract UpdatableInstance is Instance {

    event InstanceUpdated(address oldKernel, address newKernel);

    function UpdatableInstance(address _kernel) 
        Instance(_kernel) 
        public
    {

    }
    
    function updateUpdatableInstance(address _kernel) external {
        require(msg.sender == address(this));
        InstanceUpdated(kernel, _kernel);
        kernel = _kernel;
    }

}