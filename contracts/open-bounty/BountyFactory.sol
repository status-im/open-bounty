pragma solidity ^0.4.17;

import "../deploy/Factory.sol";
import "../deploy/UpdatableInstance.sol";
import "./BountyKernel.sol";


contract BountyFactory is Factory {

    event BountyCreated(address indexed pivot, address indexed repoOwner, address instance, address[] tokens, address kernel);

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

    function createBounty(address _pivot, address _repoOwner, address[] _trustTokens) 
        public 
    {
        BountyKernel instance = BountyKernel(new UpdatableInstance(address(latestKernel)));
        instance.initBounty(_pivot, _repoOwner);
        uint len = _trustTokens.length;
        for (uint i = 0; i < len; i++) {
            instance.trustToken(_trustTokens[i]);
        }
        
        emit BountyCreated(_pivot, _repoOwner, address(instance), _trustTokens, address(latestKernel));
    }

}
