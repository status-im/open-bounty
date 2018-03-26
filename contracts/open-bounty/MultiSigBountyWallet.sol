pragma solidity ^0.4.17;

import "../common/MultiSigWallet.sol";
import "../token/ERC20Token.sol";

contract MultiSigBountyWallet is MultiSigWallet {

    address[] public tokens;
    mapping (address => bool) public isTrusted;

    event TrustingToken(address _token, bool _enabled);

    /**
     * @notice deposit a ERC20 token through allowance.
     *         The amount of deposit is the allowance set to this contract.
     * @param _token the token contract address
     **/ 
    function depositToken(address _token) 
        public 
    {
        require(isTrusted[_token]); //prevent call to untrusted contracts
        ERC20Token token = ERC20Token(_token);
        uint amount = token.allowance(msg.sender, this);
        require(token.transferFrom(msg.sender, this, amount));
    }
    
    /** 
    * @notice Called MiniMeToken approvesAndCall to this contract, calls deposit.
    * @param _from address incoming token
    * @param _amount incoming amount
    * @param _token the token contract address
    * @param _data 
    */ 
    function receiveApproval(
        address _from,
        uint256 _amount,
        address _token,
        bytes _data
    ) 
        public
    {
        require(_data.length == 0); //we dont use _data
        require(isTrusted[_token]); //only consider for trusted tokens
        if (_amount > 0) {
            ERC20Token token = ERC20Token(_token);
            token.transferFrom(_from, address(this), _amount);
        }
    }
            
    
    /**
     * @notice trustes for balance in a token contract
     * @param _tokenAddr the token contract address
     **/   
    function trustToken(address _tokenAddr)
        public
        ownerExists(msg.sender) 
    {   
        if (!isTrusted[_tokenAddr]) {
            isTrusted[_tokenAddr] = true;
            tokens.push(_tokenAddr);
            emit TrustingToken(_tokenAddr, true);
        }
    }

    /**
     * Only wallet
     */

    /**
     * @notice withdraw all trusted tokens and ether to `_dest`
     * @param _destination the address of receiver
     **/    
    function withdrawTrusted(address _destination) 
        public
        notNull(_destination)
        onlyWallet
    {
        uint len = tokens.length;
        for (uint i = 0; i < len; i++) {
            address _tokenAddr = tokens[i];
            uint _amount = ERC20Token(_tokenAddr).balanceOf(address(this));
            if (_amount > 0) {
                ERC20Token(_tokenAddr).transfer(_destination, _amount);
            }
        }
        _destination.transfer(address(this).balance);
    }

    /**
     * @notice withdraw all trusted tokens and ether to `_dest`
     * @param _destinations the address of receivers
     * @param _shares amount of participation each address have
     **/    
    function withdrawTrustedMultiple(address[] _destinations, uint256[] _shares) 
        public
        onlyWallet
    {
        withdrawAdvanced(_destinations, _shares, tokens, true);
    }

    /**
     * @notice withdraw to multiple destinations multiple defined tokens
     * @param _destinations the address of receivers
     * @param _shares how much of shares each reciever get of the total tokens/ether owned by this wallet
     * @param _tokens what tokens would be withdrawn
     * @param _withdrawEther ether will be withdrawn
     **/    
    function withdrawAdvanced(address[] _destinations, uint256[] _shares, address[] _tokens, bool _withdrawEther) 
        public
        onlyWallet
    {
        //calculate total shares 
        uint len = _destinations.length;
        require(len > 0);
        require(_shares.length == len);
        uint shareTotal;
        for (uint i = 0; i < len; i++) {
            shareTotal += _shares[i];
        }
        
        //withdraw each token
        uint lenj = _tokens.length;
        if (lenj > 0) {
            uint balanceTotal;
            for (uint j = 0; j < lenj; j++) {
                ERC20Token token = ERC20Token(tokens[j]);
                balanceTotal = token.balanceOf(address(this));
                if (balanceTotal > 0) {
                    for (i = 0; i < len; i++) {
                        token.transfer(_destinations[i], (_shares[i] * balanceTotal) / shareTotal);
                    }
                }
            }
        }
        
        if (_withdrawEther) {
            balanceTotal = address(this).balance;
            if (balanceTotal > 0) {
                for (i = 0; i < len; i++) {
                    _destinations[i].transfer((_shares[i]*balanceTotal)/shareTotal);
                }
            }
        }
    }
        
    /// @dev Returns list of tokens.
    /// @return List of token addresses.
    function getTrustedTokenList()
        public
        constant
        returns (address[])
    {
        return tokens;
    }

}
