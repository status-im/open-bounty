pragma solidity ^0.4.17;

import "../common/Controlled.sol";

contract Factory is Controlled {

    event NewKernel(address newKernel, bytes infohash);

    struct Version {
        uint256 blockNumber;
        uint256 timestamp;
        address kernel;
        bytes infohash;
    }
    mapping (address => uint256) versionMap;

    Version[] versionLog;
    uint256 latestUpdate;
    address latestKernel;

    function Factory(address _kernel, bytes _infohash)
        public 
    {
        _setKernel(_kernel, _infohash);
    }

    function setKernel(address _kernel, bytes _infohash)
        external 
        onlyController
    {
        _setKernel(_kernel, _infohash);
    }

    function getVersion(uint256 index) public view
        returns(uint256 blockNumber,
                uint256 timestamp,
                address kernel,
                bytes infohash)
    {
        return (versionLog[index].blockNumber, 
                versionLog[index].timestamp, 
                versionLog[index].kernel, 
                versionLog[index].infohash);
    }

    function _setKernel(address _kernel, bytes _infohash) 
        internal
    {
        require(_kernel != latestKernel);
        versionMap[_kernel] = versionLog.length;
        versionLog.push(Version({blockNumber: block.number, timestamp: block.timestamp, kernel: _kernel, infohash: _infohash}));
        latestUpdate = block.timestamp;
        latestKernel = _kernel;
        NewKernel(_kernel, _infohash);
    }

}