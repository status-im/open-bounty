pragma solidity ^0.4.15;

import "../token/ERC20Token.sol";

contract MultiSigTokenWallet {

    uint public transactionCount;
    address[] public owners;
    address[] public tokens;
    mapping (address => bool) public isOwner;
    mapping (address => bool) public isWatched;
    mapping (uint => Transaction) public transactions;
    mapping (uint => mapping (address => bool)) public confirmations;
    
    uint public required;
    uint public nonce;

    struct Transaction {
        address destination;
        uint value;
        bytes data;
        bool executed;
    }

    uint constant public MAX_OWNER_COUNT = 50;

    event Confirmation(address indexed _sender, uint indexed _transactionId);
    event Revocation(address indexed _sender, uint indexed _transactionId);
    event Submission(uint indexed _transactionId);
    event Execution(uint indexed _transactionId);
    event ExecutionFailure(uint indexed _transactionId);
    event Deposit(address indexed _sender, uint _value);
    event Watching(address _token, bool _enabled);
    event OwnerAddition(address indexed _owner);
    event OwnerRemoval(address indexed _owner);
    event RequirementChange(uint _required);
    
    modifier onlyWallet() {
        require (msg.sender == address(this));
        _;
    }

    modifier ownerDoesNotExist(address owner) {
        require (!isOwner[owner]);
        _;
    }

    modifier ownerExists(address owner) {
        require (isOwner[owner]);
        _;
    }

    modifier transactionExists(uint transactionId) {
        require (transactions[transactionId].destination != 0);
        _;
    }

    modifier confirmed(uint transactionId, address owner) {
        require (confirmations[transactionId][owner]);
        _;
    }

    modifier notConfirmed(uint transactionId, address owner) {
        require(!confirmations[transactionId][owner]);
        _;
    }

    modifier notExecuted(uint transactionId) {
        require (!transactions[transactionId].executed);
        _;
    }

    modifier notNull(address _address) {
        require (_address != 0);
        _;
    }

    modifier validRequirement(uint ownerCount, uint _required) {
        require (ownerCount <= MAX_OWNER_COUNT && _required <= ownerCount && _required != 0 && ownerCount != 0);
        _;
    }

    /// @dev Fallback function allows to deposit ether.
    function()
        public
        payable
    {
        if (msg.value > 0)
            require(owners.length > 0); //prevents kernel misdeposit
            emit Deposit(msg.sender, msg.value);
    }

    /**
    * Public functions
    * 
    **/
    /// @dev Contract constructor sets initial owners and required number of confirmations.
    /// @param _owners List of initial owners.
    /// @param _required Number of required confirmations.
    function MultiSigTokenWallet(address[] _owners, uint _required)
        public
        validRequirement(_owners.length, _required)
    {
        uint len = _owners.length;
        for (uint i = 0; i < len; i++) {
            require(!isOwner[_owners[i]] && _owners[i] != 0);
            isOwner[_owners[i]] = true;
        }
        owners = _owners;
        required = _required;
    }

    /**
    * @notice deposit a ERC20 token. The amount of deposit is the allowance set to this contract.
    * @param _token the token contract address
    * @param _data might be used by child implementations
    **/ 
    function depositToken(address _token, bytes _data) 
        public 
    {
        require(_data.length == 0);
        require(isWatched[_token]); //prevent call to untrusted contracts
        ERC20Token token = ERC20Token(_token);
        uint amount = token.allowance(msg.sender, this);
        require(token.transferFrom(msg.sender, this, amount));
    }
        
    /**
    * @notice watches for balance in a token contract
    * @param _tokenAddr the token contract address
    **/   
    function watch(address _tokenAddr)
        public
        ownerExists(msg.sender) 
    {   
        if (!isWatched[_tokenAddr]) {
            emit Watching(_tokenAddr, true);
            tokens.push(_tokenAddr);
        }
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
        require(_data.length == 0); //we dont use _data
        require(isWatched[_token]);
        if (_amount > 0) {
            ERC20Token token = ERC20Token(_token);
            token.transferFrom(_from, address(this), _amount);
        }     
    }
    

    /// @dev Allows to add a new owner. Transaction has to be sent by wallet.
    /// @param owner Address of new owner.
    function addOwner(address owner)
        public
        onlyWallet
        ownerDoesNotExist(owner)
        notNull(owner)
        validRequirement(owners.length + 1, required)
    {
        isOwner[owner] = true;
        owners.push(owner);
        emit OwnerAddition(owner);
    }

    /// @dev Allows to remove an owner. Transaction has to be sent by wallet.
    /// @param owner Address of owner.
    function removeOwner(address owner)
        public
        onlyWallet
        ownerExists(owner)
    {
        isOwner[owner] = false;
        uint _len = owners.length - 1;
        for (uint i = 0; i < _len; i++) {
            if (owners[i] == owner) {
                owners[i] = owners[owners.length - 1];
                break;
            }
        }
        owners.length -= 1;
        if (required > owners.length)
            changeRequirement(owners.length);
        emit OwnerRemoval(owner);
    }

    /// @dev Allows to replace an owner with a new owner. Transaction has to be sent by wallet.
    /// @param owner Address of owner to be replaced.
    /// @param owner Address of new owner.
    function replaceOwner(address owner, address newOwner)
        public
        onlyWallet
        ownerExists(owner)
        ownerDoesNotExist(newOwner)
    {
        for (uint i = 0; i < owners.length; i++) {
            if (owners[i] == owner) {
                owners[i] = newOwner;
                break;
            }
        }
        isOwner[owner] = false;
        isOwner[newOwner] = true;
        emit OwnerRemoval(owner);
        emit OwnerAddition(newOwner);
    }

    /// @dev Allows to change the number of required confirmations. Transaction has to be sent by wallet.
    /// @param _required Number of required confirmations.
    function changeRequirement(uint _required)
        public
        onlyWallet
        validRequirement(owners.length, _required)
    {
        required = _required;
        emit RequirementChange(_required);
    }

    /// @dev Allows an owner to submit and confirm a transaction.
    /// @param destination Transaction target address.
    /// @param value Transaction ether value.
    /// @param data Transaction data payload.
    /// @return Returns transaction ID.
    function submitTransaction(address destination, uint value, bytes data)
        public
        returns (uint transactionId)
    {
        transactionId = addTransaction(destination, value, data);
        confirmTransaction(transactionId);
    }

    /// @dev Allows an owner to confirm a transaction.
    /// @param transactionId Transaction ID.
    function confirmTransaction(uint transactionId)
        public
        ownerExists(msg.sender)
        transactionExists(transactionId)
        notConfirmed(transactionId, msg.sender)
    {
        confirmations[transactionId][msg.sender] = true;
        emit Confirmation(msg.sender, transactionId);
        executeTransaction(transactionId);
    }

    /// @dev Allows an owner to revoke a confirmation for a transaction.
    /// @param transactionId Transaction ID.
    function revokeConfirmation(uint transactionId)
        public
        ownerExists(msg.sender)
        confirmed(transactionId, msg.sender)
        notExecuted(transactionId)
    {
        confirmations[transactionId][msg.sender] = false;
        emit Revocation(msg.sender, transactionId);
    }

    /// @dev Allows anyone to execute a confirmed transaction.
    /// @param transactionId Transaction ID.
    function executeTransaction(uint transactionId)
        public
        notExecuted(transactionId)
    {
        if (isConfirmed(transactionId)) {
            Transaction storage txx = transactions[transactionId];
            txx.executed = true;
            if (txx.destination.call.value(txx.value)(txx.data)) {
                emit Execution(transactionId);
            } else {
                emit ExecutionFailure(transactionId);
                txx.executed = false;
            }
        }
    }

    /**
    * @notice withdraw all watched tokens and ether to `_dest`
    * @param _dest the address of receiver
    **/    
    function withdraw(address _dest) 
        public
        notNull(_dest)
        onlyWallet
    {
        uint len = tokens.length;
        for (uint i = 0; i < len; i++) {
            address _tokenAddr = tokens[i];
            uint _amount = ERC20Token(_tokenAddr).balanceOf(address(this));
            if (_amount > 0) {
                ERC20Token(_tokenAddr).transfer(_dest, _amount);
            }
        }
        _dest.transfer(address(this).balance);
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

    /// @dev Returns the confirmation status of a transaction.
    /// @param transactionId Transaction ID.
    /// @return Confirmation status.
    function isConfirmed(uint transactionId)
        public
        constant
        returns (bool)
    {
        uint count = 0;
        for (uint i = 0; i < owners.length; i++) {
            if (confirmations[transactionId][owners[i]])
                count += 1;
            if (count == required)
                return true;
        }
    }

    /*
    * Internal functions
    */
    /// @dev Adds a new transaction to the transaction mapping, if transaction does not exist yet.
    /// @param destination Transaction target address.
    /// @param value Transaction ether value.
    /// @param data Transaction data payload.
    /// @return Returns transaction ID.
    function addTransaction(address destination, uint value, bytes data)
        internal
        notNull(destination)
        returns (uint transactionId)
    {
        transactionId = transactionCount;
        transactions[transactionId] = Transaction({
            destination: destination,
            value: value,
            data: data,
            executed: false
        });
        transactionCount += 1;
        emit Submission(transactionId);
    }
    
    /*
    * Web3 call functions
    */
    /// @dev Returns number of confirmations of a transaction.
    /// @param transactionId Transaction ID.
    /// @return Number of confirmations.
    function getConfirmationCount(uint transactionId)
        public
        constant
        returns (uint count)
    {
        for (uint i = 0; i < owners.length; i++) {
            if (confirmations[transactionId][owners[i]])
                count += 1;
        }
    }

    /// @dev Returns total number of transactions after filters are applied.
    /// @param pending Include pending transactions.
    /// @param executed Include executed transactions.
    /// @return Total number of transactions after filters are applied.
    function getTransactionCount(bool pending, bool executed)
        public
        constant
        returns (uint count)
    {
        for (uint i = 0; i < transactionCount; i++) {
            if (pending && !transactions[i].executed || executed && transactions[i].executed)
                count += 1;
        }
    }

    /// @dev Returns list of owners.
    /// @return List of owner addresses.
    function getOwners()
        public
        constant
        returns (address[])
    {
        return owners;
    }

    /// @dev Returns list of tokens.
    /// @return List of token addresses.
    function getTokenList()
        public
        constant
        returns (address[])
    {
        return tokens;
    }

    /// @dev Returns array with owner addresses, which confirmed transaction.
    /// @param transactionId Transaction ID.
    /// @return Returns array of owner addresses.
    function getConfirmations(uint transactionId)
        public
        constant
        returns (address[] _confirmations)
    {
        address[] memory confirmationsTemp = new address[](owners.length);
        uint count = 0;
        uint i;
        for (i = 0; i < owners.length; i++) {
            if (confirmations[transactionId][owners[i]]) {
                confirmationsTemp[count] = owners[i];
                count += 1;
            }
        }
        _confirmations = new address[](count);
        for (i = 0; i < count; i++) {
            _confirmations[i] = confirmationsTemp[i];
        }
    }

    /// @dev Returns list of transaction IDs in defined range.
    /// @param from Index start position of transaction array.
    /// @param to Index end position of transaction array.
    /// @param pending Include pending transactions.
    /// @param executed Include executed transactions.
    /// @return Returns array of transaction IDs.
    function getTransactionIds(uint from, uint to, bool pending, bool executed)
        public
        constant
        returns (uint[] _transactionIds)
    {
        uint[] memory transactionIdsTemp = new uint[](transactionCount);
        uint count = 0;
        uint i;
        for (i = 0; i < transactionCount; i++) {
            if (pending && !transactions[i].executed || executed && transactions[i].executed) {
                transactionIdsTemp[count] = i;
                count += 1;
            }
        }
        _transactionIds = new uint[](to - from);
        for (i = from; i < to; i++) {
            _transactionIds[i - from] = transactionIdsTemp[i];
        }
    }

}
