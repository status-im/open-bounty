pragma solidity ^0.4.17;

import "../deploy/InstanceStorage.sol";
import "./MultiSigTokenWallet.sol";

/**
 *
 */
contract BountyKernel is InstanceStorage, MultiSigTokenWallet {


    function BountyKernel(address[] _dummyOwners) MultiSigTokenWallet(_dummyOwners,1) public {
        //remove ownership of Kernel 
        uint len = _dummyOwners.length;
        for (uint i = 0; i < len; i++) {
            delete isOwner[_dummyOwners[i]];
        }
        delete owners;
        //keep required > 0 to prevent initialization
        required = 1;
    }

    
    /// @dev Instance constructor sets initial owners and required number of confirmations.
    /// @param _owners List of initial owners.
    /// @param _required Number of required confirmations.
    function initBounty(address[] _owners, uint _required) external {
        require(owners.length == 0 && required == 0);
        uint len = _owners.length;
        for (uint i = 0; i < len; i++) {
            require(!isOwner[_owners[i]] && _owners[i] != 0);
            isOwner[_owners[i]] = true;
        }
        owners = _owners;
        required = _required;
    }

}
