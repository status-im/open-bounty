pragma solidity ^0.4.18;

import "./ERC20Token.sol";

/// @title Test token contract - Allows testing of token transfers with multisig wallet.
contract TestToken is ERC20Token {

    event Transfer(address indexed from, address indexed to, uint256 value);
    event Approval(address indexed owner, address indexed spender, uint256 value);

    mapping (address => uint256) public balanceOf;
    mapping (address => mapping (address => uint256)) allowed;
    uint256 public totalSupply;

    string public name;
    string public symbol;
    uint8 public decimals;

    function TestToken(string _name, string _symbol, uint8 _decimals) public {
        require(bytes(_name).length > 0);
        require(bytes(_symbol).length > 0);
        name = _name;
        symbol = _symbol;
        decimals = _decimals;
    }

    function issueTokens(address _to, uint256 _value)
        public
    {
        balanceOf[_to] += _value;
        totalSupply += _value;
    }

    function transfer(address _to, uint256 _value)
        public
        returns (bool success)
    {
        return transfer(msg.sender, _to, _value);
    }

    function transferFrom(address _from, address _to, uint256 _value)
        public
        returns (bool success)
    {
        require(allowed[_from][msg.sender] >= _value);
        allowed[_from][msg.sender] -= _value;
        return transfer(_from, _to, _value);
    }

    function approve(address _spender, uint256 _value)
        public
        returns (bool success)
    {
        allowed[msg.sender][_spender] = _value;
        Approval(msg.sender, _spender, _value);
        return true;
    }

    function allowance(address _owner, address _spender)
        constant
        public
        returns (uint256 remaining)
    {
        return allowed[_owner][_spender];
    }

    function transfer(address _from, address _to, uint256 _value) private returns(bool) {
        require(balanceOf[_from] >= _value);
        Transfer(_from, _to, _value);
        balanceOf[_from] -= _value;
        balanceOf[_to] += _value;
        return true;
    }
}
