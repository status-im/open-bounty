pragma solidity ^0.4.15;

import "./MultiSigWallet.sol";
import "./ERC20.sol";

contract MultiSigTokenWallet is MultiSigWallet {

    address[] public tokens;
    mapping (address => uint) watchedPos;
    mapping (address => address[]) public userList;

    event TokenDeposit(address indexed token, address indexed sender, uint value);

    /**
    * Public functions
    * 
    **/
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
        _deposited(_from, _amount, _token, _data);

    }

    /**
    * @notice watches for balance in a token contract
    * @param _tokenAddr the token contract address
    **/   
    function watch(address _tokenAddr) 
        public
        ownerExists(msg.sender) 
    {
        require(watchedPos[_tokenAddr] == 0);
        require(ERC20(_tokenAddr).balanceOf(address(this)) > 0);
        tokens.push(_tokenAddr);
        watchedPos[_tokenAddr] = tokens.length;
    }

    /**
    * @notice watches for balance in a token contract
    * @param _tokenAddr the token contract address
    **/   
    function unwatch(address _tokenAddr) 
        public
        ownerExists(msg.sender) 
    {
        require(watchedPos[_tokenAddr] > 0);
        tokens[watchedPos[_tokenAddr] - 1] = tokens[tokens.length - 1];
        delete watchedPos[_tokenAddr];
        tokens.length--;
    }

    /**
    * @notice set token list that will be used at `withdrawEverything(msg.sender)` and `withdrawAllTokens(msg.sender)` function when sending to `msg.sender`.
    **/
    function setMyTokenList(address[] _tokenList) 
        public
    {
        userList[msg.sender] = _tokenList;
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
        _deposited(_from, _amount, msg.sender, _data);
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
    * @dev withdraw all watched tokens balances and ether to `_dest`
    * @param _dest the address of receiver
    **/    
    function withdrawEverything(address _dest) 
        public
        notNull(_dest)
        onlyWallet
    {
        address[] memory _tokenList;
        if (userList[_dest].length > 0) {
            _tokenList = userList[_dest];
        } else {
            _tokenList = tokens;
        }
        withdrawAllTokens(_dest, _tokenList);
        _dest.transfer(this.balance);
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
                ERC20(_tokenAddr).call(bytes4(keccak256("transfer(address,uint256)")), _dest, _amount);
            }
        }
    }

    /**
    * @dev register the deposit
    **/
    function _deposited(address _from,  uint _amount, address _tokenAddr, bytes) 
        internal 
    {
        TokenDeposit(_tokenAddr, _from, _amount);
        if (watchedPos[_tokenAddr] > 0) {
            tokens.push(_tokenAddr);  
            watchedPos[_tokenAddr] = tokens.length;
        }
    }
    
    /*
    * Web3 call functions
    */
    /// @dev Returns list of tokens.
    /// @return List of token addresses.
    function getTokenList()
        public
        constant
        returns (address[])
    {
        return tokens;
    }

}