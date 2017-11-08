pragma solidity ^0.4.15;

import "./MultiSigWallet.sol";
import "./ERC20.sol";

contract MultiSigTokenWallet is MultiSigWallet {

    address[] public tokens;
    mapping (address => uint) public tokenBalances;
    mapping (address => address[]) public userList;
    uint public nonce;

    event TokenDeposit(address _token, address indexed _sender, uint _value);

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
        uint _nonce = nonce;
        bool result = ERC20(_token).transferFrom(_from, this, _amount);
        assert(result);
        //ERC23 not executed _deposited tokenFallback by
        if (nonce == _nonce) {
            _deposited(_from, _amount, _token, _data);
        }
    }
    /**
    * @notice watches for balance in a token contract
    * @param _tokenAddr the token contract address
    **/   
    function watch(address _tokenAddr) 
        ownerExists(msg.sender) 
    {
        uint oldBal = tokenBalances[_tokenAddr];
        uint newBal = ERC20(_tokenAddr).balanceOf(this);
        if (newBal > oldBal) {
            _deposited(0x0, newBal-oldBal, _tokenAddr, new bytes(0));
        }
    }

    function setMyTokenList(address[] _tokenList) 
        public
    {
        userList[msg.sender] = _tokenList;
    }

    function setTokenList(address[] _tokenList) 
        onlyWallet
    {
        tokens = _tokenList;
    }
    
    /**
    * @notice ERC23 Token fallback
    * @param _from address incoming token
    * @param _amount incoming amount
    **/    
    function tokenFallback(address _from, uint _amount, bytes _data) 
        public 
    {
        _deposited(_from, _amount, msg.sender, _data);
    }
        
    /** 
    * @notice Called MiniMeToken approvesAndCall to this contract, calls deposit.
    * @param _from address incoming token
    * @param _amount incoming amount
    * @param _token the token contract address
    * @param _data (might be used by child classes)
    */ 
    function receiveApproval(address _from, uint256 _amount, address _token, bytes _data) {
        deposit(_from, _amount, _token, _data);
    }
    
    /**
    * @dev gives full ownership of this wallet to `_dest` removing older owners from wallet
    * @param _dest the address of new controller
    **/    
    function releaseWallet(address _dest)
        public
        notNull(_dest)
        ownerDoesNotExist(_dest)
        onlyWallet
    {
        address[] memory _owners = owners;
        uint numOwners = _owners.length;
        addOwner(_dest);
        for (uint i = 0; i < numOwners; i++) {
            removeOwner(_owners[i]);
        }
    }

    /**
    * @dev withdraw all recognized tokens balances and ether to `_dest`
    * @param _dest the address of receiver
    **/    
    function withdrawEverything(address _dest) 
        public
        notNull(_dest)
        onlyWallet
    {
        withdrawAllTokens(_dest);
        _dest.transfer(this.balance);
    }

    /**
    * @dev withdraw all recognized tokens balances to `_dest`
    * @param _dest the address of receiver
    **/    
    function withdrawAllTokens(address _dest) 
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
        uint len = _tokenList.length;
        for (uint i = 0;i < len; i++) {
            address _tokenAddr = _tokenList[i];
            uint _amount = tokenBalances[_tokenAddr];
            if (_amount > 0) {
                delete tokenBalances[_tokenAddr];
                ERC20(_tokenAddr).transfer(_dest, _amount);
            }
        }
    }

    /**
    * @dev withdraw `_tokenAddr` `_amount` to `_dest`
    * @param _tokenAddr the address of the token
    * @param _dest the address of receiver
    * @param _amount the number of tokens to send
    **/
    function withdrawToken(address _tokenAddr, address _dest, uint _amount)
        public
        notNull(_dest)
        onlyWallet 
    {
        require(_amount > 0);
        uint _balance = tokenBalances[_tokenAddr];
        require(_amount <= _balance);
        tokenBalances[_tokenAddr] = _balance - _amount;
        bool result = ERC20(_tokenAddr).transfer(_dest, _amount);
        assert(result);
    }
    
    /**
    * @dev register the deposit
    **/
    function _deposited(address _from,  uint _amount, address _tokenAddr, bytes) 
        internal 
    {
        TokenDeposit(_tokenAddr,_from,_amount);
        nonce++;
        if (tokenBalances[_tokenAddr] == 0) {
            tokens.push(_tokenAddr);  
            tokenBalances[_tokenAddr] = ERC20(_tokenAddr).balanceOf(this);
        } else {
            tokenBalances[_tokenAddr] += _amount;
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
