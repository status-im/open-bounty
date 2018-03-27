package commiteth.eth.contracts;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

/**
 * Auto generated code.<br>
 * <strong>Do not modify!</strong><br>
 * Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>, or {@link org.web3j.codegen.SolidityFunctionWrapperGenerator} to update.
 *
 * <p>Generated with web3j version 2.3.0.
 */
public final class TokenReg extends Contract {
    private static final String BINARY = "606060405260008054600160a060020a03191633600160a060020a0316178155600455341561002d57600080fd5b5b6114ae8061003d6000396000f300606060405236156100cd5763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663044215c681146100d257806313af4035146101ec57806366b42dcb1461020d57806369fe0e2d146102c05780637958533a146102d85780637b1a547c14610303578063891de9ed146103c15780638da5cb5b146104b25780639890220b146104e15780639f181b5e146104f6578063a02b161e1461051b578063b72e717d14610533578063dd93890b14610654578063ddca3f4314610672575b600080fd5b34156100dd57600080fd5b6100e8600435610697565b604051600160a060020a038087168252604082018590528216608082015260a060208201818152906060830190830187818151815260200191508051906020019080838360005b838110156101485780820151818401525b60200161012f565b50505050905090810190601f1680156101755780820380516001836020036101000a031916815260200191505b50838103825285818151815260200191508051906020019080838360005b838110156101ac5780820151818401525b602001610193565b50505050905090810190601f1680156101d95780820380516001836020036101000a031916815260200191505b5097505050505050505060405180910390f35b34156101f757600080fd5b61020b600160a060020a0360043516610846565b005b6102ac60048035600160a060020a03169060446024803590810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284378201915050505050509190803590602001909190803590602001908201803590602001908080601f0160208091040260200160405190810160405281815292919060208401838380828437509496506108bd95505050505050565b604051901515815260200160405180910390f35b34156102cb57600080fd5b61020b6004356108d7565b005b34156102e357600080fd5b6102f16004356024356108fc565b60405190815260200160405180910390f35b6102ac60048035600160a060020a03169060446024803590810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284378201915050505050509190803590602001909190803590602001908201803590602001908080601f01602080910402602001604051908101604052818152929190602084018383808284375094965050509235600160a060020a0316925061093b915050565b604051901515815260200160405180910390f35b34156103cc57600080fd5b61041260046024813581810190830135806020601f82018190048102016040519081016040528181529291906020840183838082843750949650610c7895505050505050565b604051858152600160a060020a038086166020830152604082018590528216608082015260a06060820181815290820184818151815260200191508051906020019080838360005b838110156104735780820151818401525b60200161045a565b50505050905090810190601f1680156104a05780820380516001836020036101000a031916815260200191505b50965050505050505060405180910390f35b34156104bd57600080fd5b6104c5610dea565b604051600160a060020a03909116815260200160405180910390f35b34156104ec57600080fd5b61020b610df9565b005b341561050157600080fd5b6102f1610e55565b60405190815260200160405180910390f35b341561052657600080fd5b61020b600435610e5c565b005b341561053e57600080fd5b610552600160a060020a0360043516611075565b60405185815260408101849052600160a060020a038216608082015260a060208201818152906060830190830187818151815260200191508051906020019080838360005b838110156101485780820151818401525b60200161012f565b50505050905090810190601f1680156101755780820380516001836020036101000a031916815260200191505b50838103825285818151815260200191508051906020019080838360005b838110156101ac5780820151818401525b602001610193565b50505050905090810190601f1680156101d95780820380516001836020036101000a031916815260200191505b5097505050505050505060405180910390f35b341561065f57600080fd5b61020b60043560243560443561122f565b005b341561067d57600080fd5b6102f16112eb565b60405190815260200160405180910390f35b60006106a16112f1565b60006106ab6112f1565b6000806003878154811015156106bd57fe5b906000526020600020906006020160005b50805460018083018054600160a060020a0390931699509293506002600019918316156101000291909101909116046020601f8201819004810201604051908101604052809291908181526020018280546001816001161561010002031660029004801561077d5780601f106107525761010080835404028352916020019161077d565b820191906000526020600020905b81548152906001019060200180831161076057829003601f168201915b5050505050945080600201549350806003018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156108235780601f106107f857610100808354040283529160200191610823565b820191906000526020600020905b81548152906001019060200180831161080657829003601f168201915b50505050600483015491945050600160a060020a031691505b5091939590929450565b60005433600160a060020a03908116911614610861576108b9565b600054600160a060020a0380831691167f70aea8d848e8a90fb7661b227dc522eb6395c3dac71b63cb59edd5c9899b236460405160405180910390a360008054600160a060020a031916600160a060020a0383161790555b5b50565b60006108cc858585853361093b565b90505b949350505050565b60005433600160a060020a039081169116146108f2576108b9565b60048190555b5b50565b600060038381548110151561090d57fe5b906000526020600020906006020160005b506000838152600591909101602052604090205490505b92915050565b600060045434101561094c57610c6f565b600160a060020a03861660009081526001602052604090205486901561097157610c6d565b85805160031461098057610c6b565b866002816040518082805190602001908083835b602083106109b457805182525b601f199092019160209182019101610994565b6001836020036101000a03801982511681845116808217855250505050505090500191505090815260200160405190819003902054156109f357610c68565b6003805460018101610a058382611303565b916000526020600020906006020160005b60a06040519081016040908152600160a060020a03808f168352602083018e90529082018c9052606082018b90528916608082015291905081518154600160a060020a031916600160a060020a0391909116178155602082015181600101908051610a85929160200190611335565b5060408201518160020155606082015181600301908051610aaa929160200190611335565b5060808201516004919091018054600160a060020a031916600160a060020a03928316179055600354908c1660009081526001602052604090819020829055909250600291508a90518082805190602001908083835b60208310610b2057805182525b601f199092019160209182019101610b00565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205560035460001901886040518082805190602001908083835b60208310610b8d57805182525b601f199092019160209182019101610b6d565b6001836020036101000a038019825116818451161790925250505091909101925060409150505180910390207f25074d730da65a10e171fe5589d2182313ef00da38d23a9ae3b78923568bdf2d8b89604051600160a060020a038316815260406020820181815290820183818151815260200191508051906020019080838360005b83811015610c285780820151818401525b602001610c0f565b50505050905090810190601f168015610c555780820380516001836020036101000a031916815260200191505b50935050505060405180910390a3600193505b5b505b505b505b95945050505050565b6000806000610c856112f1565b60008060016002886040518082805190602001908083835b60208310610cbd57805182525b601f199092019160209182019101610c9d565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902054039550600386815481101515610d0357fe5b906000526020600020906006020160005b508054600280830154600384018054600160a060020a03909416995090975092935060001960018316156101000201909116046020601f820181900481020160405190810160405280929190818152602001828054600181600116156101000203166002900480156108235780601f106107f857610100808354040283529160200191610823565b820191906000526020600020905b81548152906001019060200180831161080657829003601f168201915b50505050600483015491945050600160a060020a031691505b5091939590929450565b600054600160a060020a031681565b60005433600160a060020a03908116911614610e1457610e51565b33600160a060020a03166108fc30600160a060020a0316319081150290604051600060405180830381858888f193505050501515610e5157600080fd5b5b5b565b6003545b90565b60005433600160a060020a03908116911614610e77576108b9565b80600382815481101515610e8757fe5b906000526020600020906006020160005b506001016040518082805460018160011615610100020316600290048015610ef75780601f10610ed5576101008083540402835291820191610ef7565b820191906000526020600020905b815481529060010190602001808311610ee3575b505091505060405180910390207f96e76fa77fea85d8abeb7533fdb8288c214bb1dcf1f867c8f36a95f1f509c17560405160405180910390a360016000600383815481101515610f4357fe5b906000526020600020906006020160005b5054600160a060020a031681526020810191909152604001600090812055600380546002919083908110610f8457fe5b906000526020600020906006020160005b506001016040518082805460018160011615610100020316600290048015610ff45780601f10610fd2576101008083540402835291820191610ff4565b820191906000526020600020905b815481529060010190602001808311610fe0575b5050928352505060200160405180910390206000905560038181548110151561101957fe5b906000526020600020906006020160005b8154600160a060020a03191682556110466001830160006113b4565b600282016000905560038201600061105e91906113b4565b506004018054600160a060020a03191690555b5b50565b600061107f6112f1565b60006110896112f1565b600160a060020a0385166000908152600160205260408120546003805460001990920196508291879081106110ba57fe5b906000526020600020906006020160005b509050806001018054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561077d5780601f106107525761010080835404028352916020019161077d565b820191906000526020600020905b81548152906001019060200180831161076057829003601f168201915b5050505050945080600201549350806003018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156108235780601f106107f857610100808354040283529160200191610823565b820191906000526020600020905b81548152906001019060200180831161080657829003601f168201915b50505050600483015491945050600160a060020a031691505b5091939590929450565b8233600160a060020a031660038281548110151561124957fe5b906000526020600020906006020160005b5060040154600160a060020a031614611272576112e4565b8160038581548110151561128257fe5b906000526020600020906006020160005b50600085815260059190910160205260409081902091909155839085907f7991c63a749706fd298fc2387764d640be6e714307b6357b1d3c2ce35cba3b529085905190815260200160405180910390a35b5b50505050565b60045481565b60206040519081016040526000815290565b81548183558181151161132f5760060281600602836000526020600020918201910161132f91906113fc565b5b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061137657805160ff19168380011785556113a3565b828001600101855582156113a3579182015b828111156113a3578251825591602001919060010190611388565b5b506113b0929150611461565b5090565b50805460018160011615610100020316600290046000825580601f106113da57506108b9565b601f0160209004906000526020600020908101906108b99190611461565b5b50565b610e5991905b808211156113b0578054600160a060020a0319168155600061142760018301826113b4565b600282016000905560038201600061143f91906113b4565b50600481018054600160a060020a0319169055600601611402565b5090565b90565b610e5991905b808211156113b05760008155600101611467565b5090565b905600a165627a7a72305820dc82243a03a763551f18919f3f3b18deb414598a49e3885231566104cf03730d0029";

    private TokenReg(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    private TokenReg(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<RegisteredEventResponse> getRegisteredEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Registered", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<RegisteredEventResponse> responses = new ArrayList<RegisteredEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            RegisteredEventResponse typedResponse = new RegisteredEventResponse();
            typedResponse.tla = (Utf8String) eventValues.getIndexedValues().get(0);
            typedResponse.id = (Uint256) eventValues.getIndexedValues().get(1);
            typedResponse.addr = (Address) eventValues.getNonIndexedValues().get(0);
            typedResponse.name = (Utf8String) eventValues.getNonIndexedValues().get(1);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<RegisteredEventResponse> registeredEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Registered", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, RegisteredEventResponse>() {
            @Override
            public RegisteredEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                RegisteredEventResponse typedResponse = new RegisteredEventResponse();
                typedResponse.tla = (Utf8String) eventValues.getIndexedValues().get(0);
                typedResponse.id = (Uint256) eventValues.getIndexedValues().get(1);
                typedResponse.addr = (Address) eventValues.getNonIndexedValues().get(0);
                typedResponse.name = (Utf8String) eventValues.getNonIndexedValues().get(1);
                return typedResponse;
            }
        });
    }

    public List<UnregisteredEventResponse> getUnregisteredEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Unregistered", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<UnregisteredEventResponse> responses = new ArrayList<UnregisteredEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            UnregisteredEventResponse typedResponse = new UnregisteredEventResponse();
            typedResponse.tla = (Utf8String) eventValues.getIndexedValues().get(0);
            typedResponse.id = (Uint256) eventValues.getIndexedValues().get(1);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<UnregisteredEventResponse> unregisteredEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Unregistered", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, UnregisteredEventResponse>() {
            @Override
            public UnregisteredEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                UnregisteredEventResponse typedResponse = new UnregisteredEventResponse();
                typedResponse.tla = (Utf8String) eventValues.getIndexedValues().get(0);
                typedResponse.id = (Uint256) eventValues.getIndexedValues().get(1);
                return typedResponse;
            }
        });
    }

    public List<MetaChangedEventResponse> getMetaChangedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("MetaChanged", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<MetaChangedEventResponse> responses = new ArrayList<MetaChangedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            MetaChangedEventResponse typedResponse = new MetaChangedEventResponse();
            typedResponse.id = (Uint256) eventValues.getIndexedValues().get(0);
            typedResponse.key = (Bytes32) eventValues.getIndexedValues().get(1);
            typedResponse.value = (Bytes32) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<MetaChangedEventResponse> metaChangedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("MetaChanged", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, MetaChangedEventResponse>() {
            @Override
            public MetaChangedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                MetaChangedEventResponse typedResponse = new MetaChangedEventResponse();
                typedResponse.id = (Uint256) eventValues.getIndexedValues().get(0);
                typedResponse.key = (Bytes32) eventValues.getIndexedValues().get(1);
                typedResponse.value = (Bytes32) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public List<NewOwnerEventResponse> getNewOwnerEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("NewOwner", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList());
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<NewOwnerEventResponse> responses = new ArrayList<NewOwnerEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            NewOwnerEventResponse typedResponse = new NewOwnerEventResponse();
            typedResponse.old = (Address) eventValues.getIndexedValues().get(0);
            typedResponse.current = (Address) eventValues.getIndexedValues().get(1);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<NewOwnerEventResponse> newOwnerEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("NewOwner", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, NewOwnerEventResponse>() {
            @Override
            public NewOwnerEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                NewOwnerEventResponse typedResponse = new NewOwnerEventResponse();
                typedResponse.old = (Address) eventValues.getIndexedValues().get(0);
                typedResponse.current = (Address) eventValues.getIndexedValues().get(1);
                return typedResponse;
            }
        });
    }

    public Future<List<Type>> token(Uint256 _id) {
        Function function = new Function("token", 
                Arrays.<Type>asList(_id), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> setOwner(Address _new) {
        Function function = new Function("setOwner", Arrays.<Type>asList(_new), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> register(Address _addr, Utf8String _tla, Uint256 _base, Utf8String _name, BigInteger weiValue) {
        Function function = new Function("register", Arrays.<Type>asList(_addr, _tla, _base, _name), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function, weiValue);
    }

    public Future<TransactionReceipt> setFee(Uint256 _fee) {
        Function function = new Function("setFee", Arrays.<Type>asList(_fee), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Bytes32> meta(Uint256 _id, Bytes32 _key) {
        Function function = new Function("meta", 
                Arrays.<Type>asList(_id, _key), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> registerAs(Address _addr, Utf8String _tla, Uint256 _base, Utf8String _name, Address _owner, BigInteger weiValue) {
        Function function = new Function("registerAs", Arrays.<Type>asList(_addr, _tla, _base, _name, _owner), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function, weiValue);
    }

    public Future<List<Type>> fromTLA(Utf8String _tla) {
        Function function = new Function("fromTLA", 
                Arrays.<Type>asList(_tla), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<Address> owner() {
        Function function = new Function("owner", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> drain() {
        Function function = new Function("drain", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Uint256> tokenCount() {
        Function function = new Function("tokenCount", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> unregister(Uint256 _id) {
        Function function = new Function("unregister", Arrays.<Type>asList(_id), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<List<Type>> fromAddress(Address _addr) {
        Function function = new Function("fromAddress", 
                Arrays.<Type>asList(_addr), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> setMeta(Uint256 _id, Bytes32 _key, Bytes32 _value) {
        Function function = new Function("setMeta", Arrays.<Type>asList(_id, _key, _value), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Uint256> fee() {
        Function function = new Function("fee", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public static Future<TokenReg> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(TokenReg.class, web3j, credentials, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static Future<TokenReg> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(TokenReg.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static TokenReg load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TokenReg(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static TokenReg load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TokenReg(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class RegisteredEventResponse {
        public Utf8String tla;

        public Uint256 id;

        public Address addr;

        public Utf8String name;
    }

    public static class UnregisteredEventResponse {
        public Utf8String tla;

        public Uint256 id;
    }

    public static class MetaChangedEventResponse {
        public Uint256 id;

        public Bytes32 key;

        public Bytes32 value;
    }

    public static class NewOwnerEventResponse {
        public Address old;

        public Address current;
    }
}
