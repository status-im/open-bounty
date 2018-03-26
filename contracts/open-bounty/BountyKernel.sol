pragma solidity ^0.4.17;

import "../deploy/InstanceStorage.sol";
import "./MultiSigBountyWallet.sol";

/**
 * @notice Bounty Instance Kernel using MultiSigBountyWallet
 */
contract BountyKernel is InstanceStorage, MultiSigBountyWallet {

    function BountyKernel(address[] _dummyOwners) MultiSigWallet(_dummyOwners,1) public {
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
    function initBounty(address _pivot, address _repoOwner) external {
        require(owners.length == 0 && required == 0);
        
        require(!isOwner[_pivot]);
        require(_pivot != address(0));
        isOwner[_pivot] = true;
        owners.push(_pivot);

        require(!isOwner[_repoOwner]);
        require(_repoOwner != address(0));
        isOwner[_repoOwner] = true;
        owners.push(_repoOwner);

        required = 2;
    }

}
