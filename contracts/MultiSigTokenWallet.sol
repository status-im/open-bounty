pragma solidity ^0.4.18;

import "./MultiSigWallet.sol";
import "./ERC20.sol";

/**
 * @title MultiSigTokenWallet
 * @author Ricardo Guilherme Schmidt (Status Research & Development GmbH) 
 * MultiSigWallet that supports withdrawing all ERC20 tokens at once.
 */
contract MultiSigTokenWallet is MultiSigWallet {

    event TokenDeposit(address indexed token, address indexed sender, uint value);

    /**
    * Public functions
    * 
    **/
    /**
     * @dev only call parent constructor
     */
    function MultiSigTokenWallet(address[] _owners, uint _required) 
        MultiSigWallet(_owners,_required) 
        public 
    {
        //does nothing
    }
    /**
    * @notice deposit a ERC20 token. The amount of deposit is the allowance set to this contract.
    * @param _token the token contract address
    * @param _data might be used by child implementations
    **/ 
    function depositToken(address _token, bytes _data) 
        public 
    {
        address sender = msg.sender;
        uint amount = ERC20(_token).allowance(sender, this);
        deposit(sender, amount, _token, _data);
    }
        
    /**
    * @notice deposit a ERC20 token. The amount of deposit is the allowance set to this contract.
    * @param _token the token contract address
    * @param _data might be used by child implementations
    **/ 
    function deposit(address _from, uint256 _amount, address _token, bytes _data) 
        public 
    {
        if (_from == address(this))
            return;
        bool result = ERC20(_token).transferFrom(_from, this, _amount);
        require(result);
        TokenDeposit(_token, _from, _amount);
    }

    /**
    * @notice ERC23 Token fallback
    * @param _from address incoming token
    * @param _amount incoming amount
    **/    
    function tokenFallback(
        address _from,
        uint _amount,
        bytes _data
    ) 
        public 
        returns (bool)
    {
        TokenDeposit(msg.sender, _from, _amount);
        return true;
    }
        
    /** 
    * @notice Called MiniMeToken approvesAndCall to this contract, calls deposit.
    * @param _from address incoming token
    * @param _amount incoming amount
    * @param _token the token contract address
    * @param _data (might be used by child classes)
    */ 
    function receiveApproval(
        address _from,
        uint256 _amount,
        address _token,
        bytes _data
    )
        public
    {
        deposit(_from, _amount, _token, _data);
    }
    

    /**
    * @dev withdraw all tokens in list and ether to `_dest`
    * @param _dest the address of receiver
    * @param _tokenList the list of tokens to withdraw all balance
    **/    
    function withdrawEverything(address _dest, address[] _tokenList) 
        public
        notNull(_dest)
        onlyWallet
    {
        withdrawAllTokens(_dest, _tokenList);
        _dest.transfer(this.balance);
    }

    /**
    * @dev withdraw all listed tokens balances to `_dest`
    * @param _dest the address of receiver
    * @param _tokenList the list of tokens to withdraw all balance
    **/    
    function withdrawAllTokens(address _dest, address[] _tokenList) 
        public 
        notNull(_dest)
        onlyWallet
    {
        uint len = _tokenList.length;
        for (uint i = 0;i < len; i++) {
            address _tokenAddr = _tokenList[i];
            uint _amount = ERC20(_tokenAddr).balanceOf(address(this));
            if (_amount > 0) {
                ERC20(_tokenAddr).transfer(_dest, _amount);
            }
        }
    }

}