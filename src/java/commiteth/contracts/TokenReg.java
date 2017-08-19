package commiteth.contracts;

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
    private static final String BINARY = "606060405260008054600160a060020a03191633178155670de0b6b3a764000060045561127d90819061003190396000f3606060405236156100ae5760e060020a6000350463044215c681146100b057806313af40351461019d57806366b42dcb146101c057806369fe0e2d1461025c5780637958533a1461027f5780637b1a547c146102d3578063891de9ed1461037a5780638da5cb5b146105165780639890220b146105285780639f181b5e14610546578063a02b161e1461055c578063b72e717d1461057f578063dd93890b14610678578063ddca3f43146106c7575b005b6106d0600435604080516020818101835260008083528351918201909352828152600380548492918391829190889081101561000257509052604080517fc2575a0e9e593c00f959f8c92f12db2869c3395a3b0502d05e2516446f71f85c6006890290810180547fc2575a0e9e593c00f959f8c92f12db2869c3395a3b0502d05e2516446f71f85b929092018054602060026001861615610100026000190190951694909404601f8101859004850286018501909652858552600160a060020a031699509390918301828280156109a55780601f1061097a576101008083540402835291602001916109a5565b6100ae600435600054600160a060020a03908116339190911614610a6157610aad565b60408051602060248035600481810135601f81018590048502860185019096528585526107c4958135959194604494929390920191819084018382808284375050604080516020606435808b0135601f8101839004830284018301909452838352979998359897608497509195506024919091019350909150819084018382808284375094965050505050505060006103718585858533610363565b6100ae600435600054600160a060020a03908116339190911614610ab057610aad565b61054a600435602435600060036000508381548110156100025750509081527fc2575a0e9e593c00f959f8c92f12db2869c3395a3b0502d05e2516446f71f860600690920291909101602052604090205490565b60408051602060248035600481810135601f81018590048502860185019096528585526107c4958135959194604494929390920191819084018382808284375050604080516020606435808b0135601f8101839004830284018301909452838352979998359897608497509195506024919091019350909150819084018382808284375094965050933593505050505b600454600090341015610ab5575b95945050505050565b6040805160206004803580820135601f81018490048402850184019095528484526107d89491936024939092918401919081908401838280828437509496505050505050506000600060006020604051908101604052806000815260200150600060006001600260005088604051808280519060200190808383829060006004602084601f0104600302600f01f1509050019150509081526020016040518091039020600050540395508550600360005086815481101561000257506040805191909252600687027fc2575a0e9e593c00f959f8c92f12db2869c3395a3b0502d05e2516446f71f85b810180547fc2575a0e9e593c00f959f8c92f12db2869c3395a3b0502d05e2516446f71f85d8301547fc2575a0e9e593c00f959f8c92f12db2869c3395a3b0502d05e2516446f71f85e9093018054602060026001831615610100026000190190921691909104601f8101829004820287018201909752868652600160a060020a03929092169950929750909390830182828015610a3f5780601f10610a1457610100808354040283529160200191610a3f565b610874600054600160a060020a031681565b6100ae600054600160a060020a03908116339190911614610f5c575b565b6003545b60408051918252519081900360200190f35b6100ae600435600054600160a060020a03908116339190911614610f8a57610aad565b6108916004356040805160208181018352600080835283518083018552818152600160a060020a038616825260019092529283205460038054600019929092019492918391829187908110156100025750905260408051600687027fc2575a0e9e593c00f959f8c92f12db2869c3395a3b0502d05e2516446f71f85c81018054602060026001831615610100026000190190921691909104601f81018290048202850182019095528484527fc2575a0e9e593c00f959f8c92f12db2869c3395a3b0502d05e2516446f71f85b929092019390918301828280156109a55780601f1061097a576101008083540402835291602001916109a5565b6100ae6004356024356044358233600160a060020a0316600360005082815481101561000257906000526020600020906006020160005060040154600160a060020a03161461120b5750611278565b61054a60045481565b6040518086600160a060020a03168152602001806020018581526020018060200184600160a060020a031681526020018381038352878181518152602001915080519060200190808383829060006004602084601f0104600302600f01f150905090810190601f1680156107585780820380516001836020036101000a031916815260200191505b508381038252858181518152602001915080519060200190808383829060006004602084601f0104600302600f01f150905090810190601f1680156107b15780820380516001836020036101000a031916815260200191505b5097505050505050505060405180910390f35b604080519115158252519081900360200190f35b6040518086815260200185600160a060020a031681526020018481526020018060200183600160a060020a031681526020018281038252848181518152602001915080519060200190808383829060006004602084601f0104600302600f01f150905090810190601f1680156108625780820380516001836020036101000a031916815260200191505b50965050505050505060405180910390f35b60408051600160a060020a03929092168252519081900360200190f35b60405180868152602001806020018581526020018060200184600160a060020a031681526020018381038352878181518152602001915080519060200190808383829060006004602084601f0104600302600f01f150905090810190601f1680156107585780820380516001836020036101000a03191681526020019150508381038252858181518152602001915080519060200190808383829060006004602084601f0104600302600f01f150905090810190601f1680156107b15780820380516001836020036101000a031916815260200191505097505050505050505060405180910390f35b820191906000526020600020905b81548152906001019060200180831161098857829003601f168201915b50506040805160028088015460038901805460206001821615610100026000190190911693909304601f8101849004840285018401909552848452979c509a50909594509092508401905082828015610a3f5780601f10610a1457610100808354040283529160200191610a3f565b820191906000526020600020905b815481529060010190602001808311610a2257829003601f168201915b50505050600483015491945050600160a060020a031691505091939590929450565b60405160008054600160a060020a03848116939116917f70aea8d848e8a90fb7661b227dc522eb6395c3dac71b63cb59edd5c9899b236491a360008054600160a060020a031916821790555b50565b600455565b600160a060020a038616600090815260016020526040812054879114610adb5750610371565b85518690600314610aed575050610371565b86600260005081604051808280519060200190808383829060006004602084601f0104600302600f01f1509050019150509081526020016040518091039020600050546000141515610b4157505050610371565b60038054600181018083558281838015829011610b7757600602816006028360005260206000209182019101610b779190610c55565b50505091909060005260206000209060060201600060a0604051908101604052808d81526020018c81526020018b81526020018a815260200189815260200150909190915060008201518160000160006101000a815481600160a060020a03021916908302179055506020820151816001016000509080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10610d1857805160ff19168380011785555b50610d48929150610ce2565b5050600481018054600160a060020a03191690556006015b80821115610cf6578054600160a060020a031916815560018181018054600080835592600290821615610100026000190190911604601f819010610cc857505b506000600280840182905560038401805492815591600181161561010002600019011604601f819010610cfa5750610c3d565b601f016020900490600052602060002090810190610c9591905b80821115610cf65760008155600101610ce2565b5090565b601f016020900490600052602060002090810190610c3d9190610ce2565b82800160010185558215610c31579182015b82811115610c31578251826000505591602001919060010190610d2a565b5050604082015181600201600050556060820151816003016000509080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10610db457805160ff19168380011785555b50610de4929150610ce2565b82800160010185558215610da8579182015b82811115610da8578251826000505591602001919060010190610dc6565b505060049081018054608090930151600160a060020a03199093169290921790915560038054600160a060020a038d166000908152600160209081526040808320849055518e519396506002958f95919485948785019491938493879385938893909291601f86010402600f01f15090500191505090815260200160405180910390206000508190555060016003600050805490500388604051808280519060200190808383829060006004602084601f0104600302600f01f15090500191505060405180910390207f25074d730da65a10e171fe5589d2182313ef00da38d23a9ae3b78923568bdf2d8b896040518083600160a060020a03168152602001806020018281038252838181518152602001915080519060200190808383829060006004602084601f0104600302600f01f150905090810190601f168015610f3f5780820380516001836020036101000a031916815260200191505b50935050505060405180910390a350600198975050505050505050565b604051600160a060020a03338116916000913016319082818181858883f19350505050151561054457610002565b80600360005082815481101561000257506000526040517fc2575a0e9e593c00f959f8c92f12db2869c3395a3b0502d05e2516446f71f85c60068302018054909190819083906002600182161561010002600019019091160480156110265780601f10611004576101008083540402835291820191611026565b820191906000526020600020905b815481529060010190602001808311611012575b505060405190819003812092507f96e76fa77fea85d8abeb7533fdb8288c214bb1dcf1f867c8f36a95f1f509c1759150600090a360038054600191600091849081101561000257906000526020600020906006020160005054600160a060020a03168152602081019190915260400160009081205560038054600291908390811015610002579060005260206000209060060201600050600101600050604051808280546001816001161561010002031660029004801561111e5780601f106110fc57610100808354040283529182019161111e565b820191906000526020600020905b81548152906001019060200180831161110a575b505092835250506040519081900360200190206000905560038054829081101561000257906000526020600020906006020160008154600160a060020a031916825560018281018054600082559091600290821615610100026000190190911604601f8190106111cf57505b506000600280840182905560038401805492815591600181161561010002600019011604601f8190106111ed57505b50506004018054600160a060020a031916905550565b601f01602090049060005260206000209081019061118a9190610ce2565b601f0160209004906000526020600020908101906111b99190610ce2565b816003600050858154811015610002576000918252602080832087845260069290920290910160050181526040918290209290925580518481529051859287927f7991c63a749706fd298fc2387764d640be6e714307b6357b1d3c2ce35cba3b52929081900390910190a3505b50505056";

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

    public Future<TransactionReceipt> register(Address _addr, Utf8String _tla, Uint256 _base, Utf8String _name) {
        Function function = new Function("register", Arrays.<Type>asList(_addr, _tla, _base, _name), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
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

    public Future<TransactionReceipt> registerAs(Address _addr, Utf8String _tla, Uint256 _base, Utf8String _name, Address _owner) {
        Function function = new Function("registerAs", Arrays.<Type>asList(_addr, _tla, _base, _name, _owner), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
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
