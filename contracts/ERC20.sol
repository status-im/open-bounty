pragma solidity ^0.4.18;

contract ERC20 {
    uint256 public totalSupply;
    function balanceOf(address who) public constant returns (uint256);
    function allowance(address owner, address spender) public constant returns (uint256);
    function transfer(address to, uint256 value) public returns (bool ok);
    function transferFrom(address from, address to, uint256 value) public returns (bool ok);
    function approve(address spender, uint256 value) public returns (bool ok);
    event Transfer(address indexed from, address indexed to, uint256 value);
    event Approval(address indexed owner, address indexed spender, uint256 value);
}