pragma solidity ^0.4.17;

import "../deploy/Factory.sol";
import "../deploy/UpdatableInstance.sol";
import "./BountyKernel.sol";


contract BountyFactory is Factory {

    event IdentityCreated(address instance);

    function BountyFactory(bytes _infohash) 
        public
        Factory(initializeFirstVersion(), _infohash)
    {
        
    }

    function initializeFirstVersion() private returns (address a){
        address[] memory dummyOwners = new address[](1);
        dummyOwners[0] = address(this);
        a = address(new BountyKernel(dummyOwners));
    }

    function createBounty(address _pivot, address _repoOwner) 
        public 
    {
        BountyKernel instance = BountyKernel(new UpdatableInstance(address(latestKernel)));
        address[] memory multisigOwners = new address[](2);
        multisigOwners[0] = _pivot;
        multisigOwners[1] = _repoOwner;
        instance.initBounty(multisigOwners, 2);
        IdentityCreated(address(instance));
    }

}
