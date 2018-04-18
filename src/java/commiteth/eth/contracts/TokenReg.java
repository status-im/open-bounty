package commiteth.eth.contracts;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
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
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.3.1.
 */
public class TokenReg extends Contract {
    private static final String BINARY = "606060405260008054600160a060020a03191633600160a060020a0316178155600455341561002d57600080fd5b6112448061003c6000396000f3006060604052600436106100cf5763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663044215c681146100d457806313af4035146101ec57806366b42dcb1461020d57806369fe0e2d146102c05780637958533a146102d65780637b1a547c14610301578063891de9ed146103ab5780638da5cb5b1461049b5780639890220b146104ca5780639f181b5e146104dd578063a02b161e146104f0578063b72e717d14610506578063dd93890b14610581578063ddca3f431461059d575b600080fd5b34156100df57600080fd5b6100ea6004356105b0565b604051600160a060020a038087168252604082018590528216608082015260a060208201818152906060830190830187818151815260200191508051906020019080838360005b83811015610149578082015183820152602001610131565b50505050905090810190601f1680156101765780820380516001836020036101000a031916815260200191505b50838103825285818151815260200191508051906020019080838360005b838110156101ac578082015183820152602001610194565b50505050905090810190601f1680156101d95780820380516001836020036101000a031916815260200191505b5097505050505050505060405180910390f35b34156101f757600080fd5b61020b600160a060020a0360043516610763565b005b6102ac60048035600160a060020a03169060446024803590810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284378201915050505050509190803590602001909190803590602001908201803590602001908080601f0160208091040260200160405190810160405281815292919060208401838380828437509496506107d995505050505050565b604051901515815260200160405180910390f35b34156102cb57600080fd5b61020b6004356107f1565b34156102e157600080fd5b6102ef600435602435610811565b60405190815260200160405180910390f35b6102ac60048035600160a060020a03169060446024803590810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284378201915050505050509190803590602001909190803590602001908201803590602001908080601f01602080910402602001604051908101604052818152929190602084018383808284375094965050509235600160a060020a03169250610849915050565b34156103b657600080fd5b6103fc60046024813581810190830135806020601f82018190048102016040519081016040528181529291906020840183838082843750949650610b7f95505050505050565b604051858152600160a060020a038086166020830152604082018590528216608082015260a06060820181815290820184818151815260200191508051906020019080838360005b8381101561045c578082015183820152602001610444565b50505050905090810190601f1680156104895780820380516001836020036101000a031916815260200191505b50965050505050505060405180910390f35b34156104a657600080fd5b6104ae610ca7565b604051600160a060020a03909116815260200160405180910390f35b34156104d557600080fd5b61020b610cb6565b34156104e857600080fd5b6102ef610d10565b34156104fb57600080fd5b61020b600435610d17565b341561051157600080fd5b610525600160a060020a0360043516610f1f565b60405185815260408101849052600160a060020a038216608082015260a0602082018181529060608301908301878181518152602001915080519060200190808383600083811015610149578082015183820152602001610131565b341561058c57600080fd5b61020b600435602435604435610fe1565b34156105a857600080fd5b6102ef611095565b60006105ba61109b565b60006105c461109b565b6000806003878154811015156105d657fe5b906000526020600020906006020190508060000160009054906101000a9004600160a060020a03169550806001018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156106985780601f1061066d57610100808354040283529160200191610698565b820191906000526020600020905b81548152906001019060200180831161067b57829003601f168201915b5050505050945080600201549350806003018054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561073e5780601f106107135761010080835404028352916020019161073e565b820191906000526020600020905b81548152906001019060200180831161072157829003601f168201915b505050600490930154979996985094969095600160a060020a03909116945092505050565b60005433600160a060020a0390811691161461077e576107d6565b600054600160a060020a0380831691167f70aea8d848e8a90fb7661b227dc522eb6395c3dac71b63cb59edd5c9899b236460405160405180910390a360008054600160a060020a031916600160a060020a0383161790555b50565b60006107e88585858533610849565b95945050505050565b60005433600160a060020a0390811691161461080c576107d6565b600455565b600060038381548110151561082257fe5b60009182526020808320948352600691909102909301600501909252506040902054919050565b600060045434101561085a576107e8565b600160a060020a03861660009081526001602052604090205486901561087f57610b75565b85805160031461088e57610b73565b866002816040518082805190602001908083835b602083106108c15780518252601f1990920191602091820191016108a2565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020541561090057610b71565b600380546001810161091283826110ad565b9160005260206000209060060201600060a06040519081016040908152600160a060020a03808f168352602083018e90529082018c9052606082018b90528916608082015291905081518154600160a060020a031916600160a060020a03919091161781556020820151816001019080516109919291602001906110de565b50604082015181600201556060820151816003019080516109b69291602001906110de565b5060808201516004919091018054600160a060020a031916600160a060020a03928316179055600354908c1660009081526001602052604090819020829055909250600291508a90518082805190602001908083835b60208310610a2b5780518252601f199092019160209182019101610a0c565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205560035460001901886040518082805190602001908083835b60208310610a975780518252601f199092019160209182019101610a78565b6001836020036101000a038019825116818451161790925250505091909101925060409150505180910390207f25074d730da65a10e171fe5589d2182313ef00da38d23a9ae3b78923568bdf2d8b89604051600160a060020a038316815260406020820181815290820183818151815260200191508051906020019080838360005b83811015610b31578082015183820152602001610b19565b50505050905090810190601f168015610b5e5780820380516001836020036101000a031916815260200191505b50935050505060405180910390a3600193505b505b505b5095945050505050565b6000806000610b8c61109b565b60008060016002886040518082805190602001908083835b60208310610bc35780518252601f199092019160209182019101610ba4565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902054039550600386815481101515610c0957fe5b906000526020600020906006020190508060000160009054906101000a9004600160a060020a0316945080600201549350806003018054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561073e5780601f106107135761010080835404028352916020019161073e565b600054600160a060020a031681565b60005433600160a060020a03908116911614610cd157610d0e565b33600160a060020a03166108fc30600160a060020a0316319081150290604051600060405180830381858888f193505050501515610d0e57600080fd5b565b6003545b90565b60005433600160a060020a03908116911614610d32576107d6565b80600382815481101515610d4257fe5b90600052602060002090600602016001016040518082805460018160011615610100020316600290048015610dae5780601f10610d8c576101008083540402835291820191610dae565b820191906000526020600020905b815481529060010190602001808311610d9a575b505091505060405180910390207f96e76fa77fea85d8abeb7533fdb8288c214bb1dcf1f867c8f36a95f1f509c17560405160405180910390a360016000600383815481101515610dfa57fe5b60009182526020808320600690920290910154600160a060020a03168352820192909252604001812055600380546002919083908110610e3657fe5b90600052602060002090600602016001016040518082805460018160011615610100020316600290048015610ea25780601f10610e80576101008083540402835291820191610ea2565b820191906000526020600020905b815481529060010190602001808311610e8e575b50509283525050602001604051809103902060009055600381815481101515610ec757fe5b6000918252602082206006909102018054600160a060020a031916815590610ef2600183018261115c565b6002820160009055600382016000610f0a919061115c565b506004018054600160a060020a031916905550565b6000610f2961109b565b6000610f3361109b565b600160a060020a038516600090815260016020526040812054600380546000199092019650829187908110610f6457fe5b90600052602060002090600602019050806001018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156106985780601f1061066d57610100808354040283529160200191610698565b8233600160a060020a0316600382815481101515610ffb57fe5b6000918252602090912060046006909202010154600160a060020a0316146110225761108f565b8160038581548110151561103257fe5b60009182526020808320878452600560069093020191909101905260409081902091909155839085907f7991c63a749706fd298fc2387764d640be6e714307b6357b1d3c2ce35cba3b529085905190815260200160405180910390a35b50505050565b60045481565b60206040519081016040526000815290565b8154818355818115116110d9576006028160060283600052602060002091820191016110d991906111a0565b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061111f57805160ff191683800117855561114c565b8280016001018555821561114c579182015b8281111561114c578251825591602001919060010190611131565b506111589291506111fe565b5090565b50805460018160011615610100020316600290046000825580601f1061118257506107d6565b601f0160209004906000526020600020908101906107d691906111fe565b610d1491905b80821115611158578054600160a060020a031916815560006111cb600183018261115c565b60028201600090556003820160006111e3919061115c565b50600481018054600160a060020a03191690556006016111a6565b610d1491905b8082111561115857600081556001016112045600a165627a7a72305820c07b66e0f471d477346b2ba225458b0520214a638dd3b0fdab7dfe5eb7135b260029";

    protected TokenReg(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected TokenReg(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<RegisteredEventResponse> getRegisteredEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Registered", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}));
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<RegisteredEventResponse> responses = new ArrayList<RegisteredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RegisteredEventResponse typedResponse = new RegisteredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.tla = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.addr = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.name = (String) eventValues.getNonIndexedValues().get(1).getValue();
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
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                RegisteredEventResponse typedResponse = new RegisteredEventResponse();
                typedResponse.log = log;
                typedResponse.tla = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.addr = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.name = (String) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public List<UnregisteredEventResponse> getUnregisteredEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Unregistered", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<UnregisteredEventResponse> responses = new ArrayList<UnregisteredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            UnregisteredEventResponse typedResponse = new UnregisteredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.tla = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
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
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                UnregisteredEventResponse typedResponse = new UnregisteredEventResponse();
                typedResponse.log = log;
                typedResponse.tla = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public List<MetaChangedEventResponse> getMetaChangedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("MetaChanged", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<MetaChangedEventResponse> responses = new ArrayList<MetaChangedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            MetaChangedEventResponse typedResponse = new MetaChangedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.key = (byte[]) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.value = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
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
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                MetaChangedEventResponse typedResponse = new MetaChangedEventResponse();
                typedResponse.log = log;
                typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.key = (byte[]) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.value = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public List<NewOwnerEventResponse> getNewOwnerEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("NewOwner", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList());
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<NewOwnerEventResponse> responses = new ArrayList<NewOwnerEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            NewOwnerEventResponse typedResponse = new NewOwnerEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.old = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.current = (String) eventValues.getIndexedValues().get(1).getValue();
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
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                NewOwnerEventResponse typedResponse = new NewOwnerEventResponse();
                typedResponse.log = log;
                typedResponse.old = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.current = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public RemoteCall<Tuple5<String, String, BigInteger, String, String>> token(BigInteger _id) {
        final Function function = new Function("token", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_id)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}));
        return new RemoteCall<Tuple5<String, String, BigInteger, String, String>>(
                new Callable<Tuple5<String, String, BigInteger, String, String>>() {
                    @Override
                    public Tuple5<String, String, BigInteger, String, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<String, String, BigInteger, String, String>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (String) results.get(3).getValue(), 
                                (String) results.get(4).getValue());
                    }
                });
    }

    public RemoteCall<TransactionReceipt> setOwner(String _new) {
        final Function function = new Function(
                "setOwner", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_new)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> register(String _addr, String _tla, BigInteger _base, String _name, BigInteger weiValue) {
        final Function function = new Function(
                "register", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_addr), 
                new org.web3j.abi.datatypes.Utf8String(_tla), 
                new org.web3j.abi.datatypes.generated.Uint256(_base), 
                new org.web3j.abi.datatypes.Utf8String(_name)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteCall<TransactionReceipt> setFee(BigInteger _fee) {
        final Function function = new Function(
                "setFee", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_fee)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<byte[]> meta(BigInteger _id, byte[] _key) {
        final Function function = new Function("meta", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_id), 
                new org.web3j.abi.datatypes.generated.Bytes32(_key)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<TransactionReceipt> registerAs(String _addr, String _tla, BigInteger _base, String _name, String _owner, BigInteger weiValue) {
        final Function function = new Function(
                "registerAs", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_addr), 
                new org.web3j.abi.datatypes.Utf8String(_tla), 
                new org.web3j.abi.datatypes.generated.Uint256(_base), 
                new org.web3j.abi.datatypes.Utf8String(_name), 
                new org.web3j.abi.datatypes.Address(_owner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteCall<Tuple5<BigInteger, String, BigInteger, String, String>> fromTLA(String _tla) {
        final Function function = new Function("fromTLA", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(_tla)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}));
        return new RemoteCall<Tuple5<BigInteger, String, BigInteger, String, String>>(
                new Callable<Tuple5<BigInteger, String, BigInteger, String, String>>() {
                    @Override
                    public Tuple5<BigInteger, String, BigInteger, String, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<BigInteger, String, BigInteger, String, String>(
                                (BigInteger) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (String) results.get(3).getValue(), 
                                (String) results.get(4).getValue());
                    }
                });
    }

    public RemoteCall<String> owner() {
        final Function function = new Function("owner", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> drain() {
        final Function function = new Function(
                "drain", 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> tokenCount() {
        final Function function = new Function("tokenCount", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> unregister(BigInteger _id) {
        final Function function = new Function(
                "unregister", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_id)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Tuple5<BigInteger, String, BigInteger, String, String>> fromAddress(String _addr) {
        final Function function = new Function("fromAddress", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_addr)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}));
        return new RemoteCall<Tuple5<BigInteger, String, BigInteger, String, String>>(
                new Callable<Tuple5<BigInteger, String, BigInteger, String, String>>() {
                    @Override
                    public Tuple5<BigInteger, String, BigInteger, String, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<BigInteger, String, BigInteger, String, String>(
                                (BigInteger) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (String) results.get(3).getValue(), 
                                (String) results.get(4).getValue());
                    }
                });
    }

    public RemoteCall<TransactionReceipt> setMeta(BigInteger _id, byte[] _key, byte[] _value) {
        final Function function = new Function(
                "setMeta", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_id), 
                new org.web3j.abi.datatypes.generated.Bytes32(_key), 
                new org.web3j.abi.datatypes.generated.Bytes32(_value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> fee() {
        final Function function = new Function("fee", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public static RemoteCall<TokenReg> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(TokenReg.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<TokenReg> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(TokenReg.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static TokenReg load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TokenReg(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static TokenReg load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TokenReg(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class RegisteredEventResponse {
        public Log log;

        public byte[] tla;

        public BigInteger id;

        public String addr;

        public String name;
    }

    public static class UnregisteredEventResponse {
        public Log log;

        public byte[] tla;

        public BigInteger id;
    }

    public static class MetaChangedEventResponse {
        public Log log;

        public BigInteger id;

        public byte[] key;

        public byte[] value;
    }

    public static class NewOwnerEventResponse {
        public Log log;

        public String old;

        public String current;
    }
}
