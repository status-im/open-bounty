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
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple4;
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
public class MultiSigTokenWallet extends Contract {
    private static final String BINARY = "6060604052341561000f57600080fd5b6124068061001e6000396000f3006060604052600436106101be5763ffffffff60e060020a60003504166301e336678114610207578063025e7c271461022f578063173825d91461026157806320ea8d8614610280578063273cbaa0146102965780632f54bf6e146102fc5780633411c81c1461032f578063342b7e711461035157806336756a23146103a05780634f383934146103f15780634f64b2be146104405780635143a9fe14610456578063523fba7f146104c257806354741525146104f357806358e2cd76146105105780637065cb481461052f578063784547a71461054e5780638b51d13f146105645780638f4ffcb11461057a5780639ace38c2146105e6578063a0e67e2b14610694578063a878aee6146106a7578063a8abe69a146106c6578063affed0e0146106e9578063b5dc40c3146106fc578063b77bf60014610712578063b97fd9e114610725578063ba51a6df14610744578063c01a8c841461075a578063c0ee0b8a14610770578063c6427474146107d5578063cd4995231461083a578063d74f8edd1461085c578063dc8452cd1461086f578063e20056e614610882578063e3004b57146108a7578063ee22610b14610906578063f750aaa61461091c575b60003411156102055733600160a060020a03167fe1fffcc4923d04b559f4d29a8bfc6cda04eb5b0d3c460751c2402c5c5cc9109c3460405190815260200160405180910390a25b005b341561021257600080fd5b610205600160a060020a036004358116906024351660443561093b565b341561023a57600080fd5b610245600435610a3c565b604051600160a060020a03909116815260200160405180910390f35b341561026c57600080fd5b610205600160a060020a0360043516610a64565b341561028b57600080fd5b610205600435610bee565b34156102a157600080fd5b6102a9610ccc565b60405160208082528190810183818151815260200191508051906020019060200280838360005b838110156102e85780820151838201526020016102d0565b505050509050019250505060405180910390f35b341561030757600080fd5b61031b600160a060020a0360043516610d35565b604051901515815260200160405180910390f35b341561033a57600080fd5b61031b600435600160a060020a0360243516610d4a565b341561035c57600080fd5b6102056004602481358181019083013580602081810201604051908101604052809392919081815260200183836020028082843750949650610d6a95505050505050565b34156103ab57600080fd5b61020560046024813581810190830135806020818102016040519081016040528093929190818152602001838360200280828437509496505093359350610da192505050565b34156103fc57600080fd5b6102056004602481358181019083013580602081810201604051908101604052809392919081815260200183836020028082843750949650610edd95505050505050565b341561044b57600080fd5b610245600435610f06565b341561046157600080fd5b61020560048035600160a060020a0390811691602480359260443516919060849060643590810190830135806020601f82018190048102016040519081016040528181529291906020840183838082843750949650610f1495505050505050565b34156104cd57600080fd5b6104e1600160a060020a0360043516610fd3565b60405190815260200160405180910390f35b34156104fe57600080fd5b6104e160043515156024351515610fe5565b341561051b57600080fd5b610205600160a060020a0360043516611051565b341561053a57600080fd5b610205600160a060020a0360043516611136565b341561055957600080fd5b61031b600435611262565b341561056f57600080fd5b6104e16004356112e6565b341561058557600080fd5b61020560048035600160a060020a0390811691602480359260443516919060849060643590810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284375094965061135595505050505050565b34156105f157600080fd5b6105fc600435611361565b604051600160a060020a038516815260208101849052811515606082015260806040820181815290820184818151815260200191508051906020019080838360005b8381101561065657808201518382015260200161063e565b50505050905090810190601f1680156106835780820380516001836020036101000a031916815260200191505b509550505050505060405180910390f35b341561069f57600080fd5b6102a961143f565b34156106b257600080fd5b610205600160a060020a03600435166114a5565b34156106d157600080fd5b6102a9600435602435604435151560643515156116cb565b34156106f457600080fd5b6104e16117f3565b341561070757600080fd5b6102a96004356117f9565b341561071d57600080fd5b6104e161195b565b341561073057600080fd5b610205600160a060020a0360043516611961565b341561074f57600080fd5b610205600435611a6a565b341561076557600080fd5b610205600435611afd565b341561077b57600080fd5b61020560048035600160a060020a03169060248035919060649060443590810190830135806020601f82018190048102016040519081016040528181529291906020840183838082843750949650611bea95505050505050565b34156107e057600080fd5b6104e160048035600160a060020a03169060248035919060649060443590810190830135806020601f82018190048102016040519081016040528181529291906020840183838082843750949650611bfb95505050505050565b341561084557600080fd5b610245600160a060020a0360043516602435611c1a565b341561086757600080fd5b6104e1611c51565b341561087a57600080fd5b6104e1611c56565b341561088d57600080fd5b610205600160a060020a0360043581169060243516611c5c565b34156108b257600080fd5b61020560048035600160a060020a03169060446024803590810190830135806020601f82018190048102016040519081016040528181529291906020840183838082843750949650611dfd95505050505050565b341561091157600080fd5b610205600435611e7b565b341561092757600080fd5b610205600160a060020a0360043516611fd2565b60008083600160a060020a038116151561095457600080fd5b30600160a060020a031633600160a060020a031614151561097457600080fd5b6000841161098157600080fd5b600160a060020a0386166000908152600560205260409020549250828411156109a957600080fd5b600160a060020a03861660008181526005602052604090819020868603905563a9059cbb90879087905160e060020a63ffffffff8516028152600160a060020a0390921660048301526024820152604401602060405180830381600087803b1515610a1357600080fd5b5af11515610a2057600080fd5b5050506040518051925050811515610a3457fe5b505050505050565b6000805482908110610a4a57fe5b600091825260209091200154600160a060020a0316905081565b60008030600160a060020a031633600160a060020a0316141515610a8757600080fd5b600160a060020a038316600090815260066020526040902054839060ff161515610ab057600080fd5b600160a060020a0384166000908152600660205260408120805460ff19169055805460001901935091505b82821015610b865783600160a060020a0316600083815481101515610afc57fe5b600091825260209091200154600160a060020a03161415610b7b57600080546000198101908110610b2957fe5b60009182526020822001548154600160a060020a03909116919084908110610b4d57fe5b60009182526020909120018054600160a060020a031916600160a060020a0392909216919091179055610b86565b600190910190610adb565b600080546000190190610b999082612289565b506000546008541115610bb257600054610bb290611a6a565b83600160a060020a03167f8001553a916ef2f495d26a907cc54d96ed840d7bda71e73194bf5a9df7a76b9060405160405180910390a250505050565b33600160a060020a03811660009081526006602052604090205460ff161515610c1657600080fd5b600082815260036020908152604080832033600160a060020a038116855292529091205483919060ff161515610c4b57600080fd5b600084815260026020526040902060030154849060ff1615610c6c57600080fd5b6000858152600360209081526040808320600160a060020a033316808552925291829020805460ff1916905586917ff6a317157440607f36269043eb55f1287a5a19ba2216afeab88cd46cbcfb88e9905160405180910390a35050505050565b610cd46122ad565b6001805480602002602001604051908101604052809291908181526020018280548015610d2a57602002820191906000526020600020905b8154600160a060020a03168152600190910190602001808311610d0c575b505050505090505b90565b60066020526000908152604090205460ff1681565b600360209081526000928352604080842090915290825290205460ff1681565b30600160a060020a031633600160a060020a0316141515610d8a57600080fd5b6001818051610d9d9291602001906122bf565b5050565b600082518260328211158015610db75750818111155b8015610dc257508015155b8015610dcd57508115155b1515610dd857600080fd5b600054158015610de85750600854155b1515610df357600080fd5b600092505b8451831015610ebd5760066000868581518110610e1157fe5b90602001906020020151600160a060020a0316815260208101919091526040016000205460ff16158015610e625750848381518110610e4c57fe5b90602001906020020151600160a060020a031615155b1515610e6d57600080fd5b600160066000878681518110610e7f57fe5b90602001906020020151600160a060020a031681526020810191909152604001600020805460ff191691151591909117905560019290920191610df8565b6000858051610ed09291602001906122bf565b5050506008919091555050565b600160a060020a0333166000908152600760205260409020818051610d9d9291602001906122bf565b6001805482908110610a4a57fe5b60008030600160a060020a031686600160a060020a03161415610f3657610a34565b600954915083600160a060020a03166323b872dd87308860405160e060020a63ffffffff8616028152600160a060020a0393841660048201529190921660248201526044810191909152606401602060405180830381600087803b1515610f9c57600080fd5b5af11515610fa957600080fd5b5050506040518051915050801515610fbd57fe5b816009541415610a3457610a348686868661204e565b60056020526000908152604090205481565b6000805b60045481101561104a57838015611012575060008181526002602052604090206003015460ff16155b806110365750828015611036575060008181526002602052604090206003015460ff165b15611042576001820191505b600101610fe9565b5092915050565b33600160a060020a0381166000908152600660205260408120549091829160ff16151561107d57600080fd5b600160a060020a038416600081815260056020526040908190205494506370a082319030905160e060020a63ffffffff8416028152600160a060020a039091166004820152602401602060405180830381600087803b15156110de57600080fd5b5af115156110eb57600080fd5b5050506040518051925050828211156111305761113060008484038660006040518059106111165750595b818152601f19601f8301168101602001604052905061204e565b50505050565b30600160a060020a031633600160a060020a031614151561115657600080fd5b600160a060020a038116600090815260066020526040902054819060ff161561117e57600080fd5b81600160a060020a038116151561119457600080fd5b600080549050600101600854603282111580156111b15750818111155b80156111bc57508015155b80156111c757508115155b15156111d257600080fd5b600160a060020a0385166000908152600660205260408120805460ff1916600190811790915581549081016112078382612289565b5060009182526020909120018054600160a060020a031916600160a060020a0387169081179091557ff39e6e1eb0edcf53c221607b54b00cd28f3196fed0a24994dc308b8f611b682d60405160405180910390a25050505050565b600080805b6000548110156112df576000848152600360205260408120815490919081908490811061129057fe5b6000918252602080832090910154600160a060020a0316835282019290925260400190205460ff16156112c4576001820191505b6008548214156112d757600192506112df565b600101611267565b5050919050565b6000805b60005481101561134f576000838152600360205260408120815490919081908490811061131357fe5b6000918252602080832090910154600160a060020a0316835282019290925260400190205460ff1615611347576001820191505b6001016112ea565b50919050565b61113084848484610f14565b60026020528060005260406000206000915090508060000160009054906101000a9004600160a060020a031690806001015490806002018054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561142c5780601f106114015761010080835404028352916020019161142c565b820191906000526020600020905b81548152906001019060200180831161140f57829003601f168201915b5050506003909301549192505060ff1684565b6114476122ad565b6000805480602002602001604051908101604052809291908181526020018280548015610d2a57602002820191906000526020600020908154600160a060020a03168152600190910190602001808311610d0c575050505050905090565b6114ad6122ad565b600080808085600160a060020a03811615156114c857600080fd5b30600160a060020a031633600160a060020a03161415156114e857600080fd5b600160a060020a038716600090815260076020526040812054111561158c576007600088600160a060020a0316600160a060020a0316815260200190815260200160002080548060200260200160405190810160405280929190818152602001828054801561158057602002820191906000526020600020905b8154600160a060020a03168152600190910190602001808311611562575b505050505095506115ea565b60018054806020026020016040519081016040528092919081815260200182805480156115e257602002820191906000526020600020905b8154600160a060020a031681526001909101906020018083116115c4575b505050505095505b85519450600093505b848410156116c25785848151811061160757fe5b90602001906020020151600160a060020a0381166000908152600560205260408120549194509092508211156116b757600160a060020a0383166000818152600560205260408082209190915563a9059cbb90899085905160e060020a63ffffffff8516028152600160a060020a0390921660048301526024820152604401602060405180830381600087803b151561169f57600080fd5b5af115156116ac57600080fd5b505050604051805150505b6001909301926115f3565b50505050505050565b6116d36122ad565b6116db6122ad565b6000806004546040518059106116ee5750595b9080825280602002602001820160405250925060009150600090505b60045481101561178357858015611733575060008181526002602052604090206003015460ff16155b806117575750848015611757575060008181526002602052604090206003015460ff165b1561177b578083838151811061176957fe5b60209081029091010152600191909101905b60010161170a565b8787036040518059106117935750595b908082528060200260200182016040525093508790505b868110156117e8578281815181106117be57fe5b9060200190602002015184898303815181106117d657fe5b602090810290910101526001016117aa565b505050949350505050565b60095481565b6118016122ad565b6118096122ad565b60008054819060405180591061181c5750595b9080825280602002602001820160405250925060009150600090505b6000548110156118e4576000858152600360205260408120815490919081908490811061186157fe5b6000918252602080832090910154600160a060020a0316835282019290925260400190205460ff16156118dc57600080548290811061189c57fe5b600091825260209091200154600160a060020a03168383815181106118bd57fe5b600160a060020a03909216602092830290910190910152600191909101905b600101611838565b816040518059106118f25750595b90808252806020026020018201604052509350600090505b818110156119535782818151811061191e57fe5b9060200190602002015184828151811061193457fe5b600160a060020a0390921660209283029091019091015260010161190a565b505050919050565b60045481565b6119696122ad565b60008083600160a060020a038116151561198257600080fd5b600160a060020a038516600090815260066020526040902054859060ff16156119aa57600080fd5b30600160a060020a031633600160a060020a03161415156119ca57600080fd5b6000805480602002602001604051908101604052809291908181526020018280548015611a2057602002820191906000526020600020905b8154600160a060020a03168152600190910190602001808311611a02575b5050505050945084519350611a3486611136565b600092505b83831015610a3457611a5f858481518110611a5057fe5b90602001906020020151610a64565b600190920191611a39565b30600160a060020a031633600160a060020a0316141515611a8a57600080fd5b6000548160328211801590611a9f5750818111155b8015611aaa57508015155b8015611ab557508115155b1515611ac057600080fd5b60088390557fa3f1ee9126a074d9326c682f561767f710e927faa811f7a99829d49dc421797a8360405190815260200160405180910390a1505050565b33600160a060020a03811660009081526006602052604090205460ff161515611b2557600080fd5b6000828152600260205260409020548290600160a060020a03161515611b4a57600080fd5b600083815260036020908152604080832033600160a060020a038116855292529091205484919060ff1615611b7e57600080fd5b6000858152600360209081526040808320600160a060020a033316808552925291829020805460ff1916600117905586917f4a504a94899432a9846e1aa406dceb1bcfd538bb839071d49d1e5e23f5be30ef905160405180910390a3611be385611e7b565b5050505050565b611bf68383338461204e565b505050565b6000611c08848484612198565b9050611c1381611afd565b9392505050565b600760205281600052604060002081815481101515611c3557fe5b600091825260209091200154600160a060020a03169150829050565b603281565b60085481565b600030600160a060020a031633600160a060020a0316141515611c7e57600080fd5b600160a060020a038316600090815260066020526040902054839060ff161515611ca757600080fd5b600160a060020a038316600090815260066020526040902054839060ff1615611ccf57600080fd5b600092505b600054831015611d5b5784600160a060020a0316600084815481101515611cf757fe5b600091825260209091200154600160a060020a03161415611d505783600084815481101515611d2257fe5b60009182526020909120018054600160a060020a031916600160a060020a0392909216919091179055611d5b565b600190920191611cd4565b600160a060020a03808616600081815260066020526040808220805460ff199081169091559388168252908190208054909316600117909255907f8001553a916ef2f495d26a907cc54d96ed840d7bda71e73194bf5a9df7a76b90905160405180910390a283600160a060020a03167ff39e6e1eb0edcf53c221607b54b00cd28f3196fed0a24994dc308b8f611b682d60405160405180910390a25050505050565b336000600160a060020a03841663dd62ed3e833060405160e060020a63ffffffff8516028152600160a060020a03928316600482015291166024820152604401602060405180830381600087803b1515611e5657600080fd5b5af11515611e6357600080fd5b50505060405180519050905061113082828686610f14565b600081815260026020526040812060030154829060ff1615611e9c57600080fd5b611ea583611262565b15611bf65760008381526002602081905260409182902060038101805460ff19166001908117909155815490820154919550600160a060020a0316929091850190518082805460018160011615610100020316600290048015611f495780601f10611f1e57610100808354040283529160200191611f49565b820191906000526020600020905b815481529060010190602001808311611f2c57829003601f168201915b505091505060006040518083038185875af19250505015611f9657827f33e13ecb54c3076d8e8bb8c2881800a4d972b792045ffae98fdf46df365fed7560405160405180910390a2611bf6565b827f526441bb6c1aba3c9a4a6ca1d6545da9c2333c8c48343ef398eb858d72b7923660405160405180910390a250600301805460ff1916905550565b80600160a060020a0381161515611fe857600080fd5b30600160a060020a031633600160a060020a031614151561200857600080fd5b612011826114a5565b81600160a060020a03166108fc30600160a060020a0316319081150290604051600060405180830381858888f193505050501515610d9d57600080fd5b83600160a060020a03167f98c09d9949722bae4bd0d988d4050091c3ae7ec6d51d3c6bbfe4233593944e9e8385604051600160a060020a03909216825260208201526040908101905180910390a2600980546001019055600160a060020a038216600090815260056020526040902054151561217557600180548082016120d58382612289565b5060009182526020909120018054600160a060020a031916600160a060020a0384169081179091556370a082313060405160e060020a63ffffffff8416028152600160a060020a039091166004820152602401602060405180830381600087803b151561214157600080fd5b5af1151561214e57600080fd5b5050506040518051600160a060020a03841660009081526005602052604090205550611130565b50600160a060020a03166000908152600560205260409020805491909101905550565b600083600160a060020a03811615156121b057600080fd5b600454915060806040519081016040908152600160a060020a0387168252602080830187905281830186905260006060840181905285815260029091522081518154600160a060020a031916600160a060020a03919091161781556020820151816001015560408201518160020190805161222f929160200190612326565b506060820151600391909101805460ff191691151591909117905550600480546001019055817fc0ba8fe4b176c1714197d43b9cc6bcf797a4a7461c5fe8d0ef6e184ae7601e5160405160405180910390a2509392505050565b815481835581811511611bf657600083815260209020611bf691810190830161239c565b60206040519081016040526000815290565b828054828255906000526020600020908101928215612316579160200282015b828111156123165782518254600160a060020a031916600160a060020a0391909116178255602092909201916001909101906122df565b506123229291506123b6565b5090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061236757805160ff1916838001178555612394565b82800160010185558215612394579182015b82811115612394578251825591602001919060010190612379565b506123229291505b610d3291905b8082111561232257600081556001016123a2565b610d3291905b80821115612322578054600160a060020a03191681556001016123bc5600a165627a7a72305820093e4169138e9fc8ced5bccbc93372da29d53223deb52ea1f0271f9134e4f56a0029";

    protected MultiSigTokenWallet(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected MultiSigTokenWallet(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<ConfirmationEventResponse> getConfirmationEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Confirmation", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<ConfirmationEventResponse> responses = new ArrayList<ConfirmationEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ConfirmationEventResponse typedResponse = new ConfirmationEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._sender = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._transactionId = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ConfirmationEventResponse> confirmationEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Confirmation", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ConfirmationEventResponse>() {
            @Override
            public ConfirmationEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                ConfirmationEventResponse typedResponse = new ConfirmationEventResponse();
                typedResponse.log = log;
                typedResponse._sender = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._transactionId = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public List<RevocationEventResponse> getRevocationEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Revocation", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<RevocationEventResponse> responses = new ArrayList<RevocationEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RevocationEventResponse typedResponse = new RevocationEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._sender = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._transactionId = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<RevocationEventResponse> revocationEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Revocation", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, RevocationEventResponse>() {
            @Override
            public RevocationEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                RevocationEventResponse typedResponse = new RevocationEventResponse();
                typedResponse.log = log;
                typedResponse._sender = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._transactionId = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public List<SubmissionEventResponse> getSubmissionEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Submission", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<SubmissionEventResponse> responses = new ArrayList<SubmissionEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            SubmissionEventResponse typedResponse = new SubmissionEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<SubmissionEventResponse> submissionEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Submission", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, SubmissionEventResponse>() {
            @Override
            public SubmissionEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                SubmissionEventResponse typedResponse = new SubmissionEventResponse();
                typedResponse.log = log;
                typedResponse._transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public List<ExecutionEventResponse> getExecutionEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Execution", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<ExecutionEventResponse> responses = new ArrayList<ExecutionEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ExecutionEventResponse typedResponse = new ExecutionEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ExecutionEventResponse> executionEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Execution", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ExecutionEventResponse>() {
            @Override
            public ExecutionEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                ExecutionEventResponse typedResponse = new ExecutionEventResponse();
                typedResponse.log = log;
                typedResponse._transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public List<ExecutionFailureEventResponse> getExecutionFailureEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("ExecutionFailure", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<ExecutionFailureEventResponse> responses = new ArrayList<ExecutionFailureEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ExecutionFailureEventResponse typedResponse = new ExecutionFailureEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ExecutionFailureEventResponse> executionFailureEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("ExecutionFailure", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ExecutionFailureEventResponse>() {
            @Override
            public ExecutionFailureEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                ExecutionFailureEventResponse typedResponse = new ExecutionFailureEventResponse();
                typedResponse.log = log;
                typedResponse._transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public List<DepositEventResponse> getDepositEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Deposit", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<DepositEventResponse> responses = new ArrayList<DepositEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DepositEventResponse typedResponse = new DepositEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._sender = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<DepositEventResponse> depositEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Deposit", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, DepositEventResponse>() {
            @Override
            public DepositEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                DepositEventResponse typedResponse = new DepositEventResponse();
                typedResponse.log = log;
                typedResponse._sender = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public List<TokenDepositEventResponse> getTokenDepositEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("TokenDeposit", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<TokenDepositEventResponse> responses = new ArrayList<TokenDepositEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TokenDepositEventResponse typedResponse = new TokenDepositEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._sender = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._token = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<TokenDepositEventResponse> tokenDepositEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("TokenDeposit", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, TokenDepositEventResponse>() {
            @Override
            public TokenDepositEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                TokenDepositEventResponse typedResponse = new TokenDepositEventResponse();
                typedResponse.log = log;
                typedResponse._sender = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._token = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public List<OwnerAdditionEventResponse> getOwnerAdditionEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("OwnerAddition", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList());
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<OwnerAdditionEventResponse> responses = new ArrayList<OwnerAdditionEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnerAdditionEventResponse typedResponse = new OwnerAdditionEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._owner = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<OwnerAdditionEventResponse> ownerAdditionEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("OwnerAddition", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, OwnerAdditionEventResponse>() {
            @Override
            public OwnerAdditionEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                OwnerAdditionEventResponse typedResponse = new OwnerAdditionEventResponse();
                typedResponse.log = log;
                typedResponse._owner = (String) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public List<OwnerRemovalEventResponse> getOwnerRemovalEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("OwnerRemoval", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList());
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<OwnerRemovalEventResponse> responses = new ArrayList<OwnerRemovalEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnerRemovalEventResponse typedResponse = new OwnerRemovalEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._owner = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<OwnerRemovalEventResponse> ownerRemovalEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("OwnerRemoval", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, OwnerRemovalEventResponse>() {
            @Override
            public OwnerRemovalEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                OwnerRemovalEventResponse typedResponse = new OwnerRemovalEventResponse();
                typedResponse.log = log;
                typedResponse._owner = (String) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public List<RequirementChangeEventResponse> getRequirementChangeEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("RequirementChange", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<RequirementChangeEventResponse> responses = new ArrayList<RequirementChangeEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RequirementChangeEventResponse typedResponse = new RequirementChangeEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._required = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<RequirementChangeEventResponse> requirementChangeEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("RequirementChange", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, RequirementChangeEventResponse>() {
            @Override
            public RequirementChangeEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                RequirementChangeEventResponse typedResponse = new RequirementChangeEventResponse();
                typedResponse.log = log;
                typedResponse._required = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public RemoteCall<TransactionReceipt> withdrawToken(String _tokenAddr, String _dest, BigInteger _amount) {
        final Function function = new Function(
                "withdrawToken", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_tokenAddr), 
                new org.web3j.abi.datatypes.Address(_dest), 
                new org.web3j.abi.datatypes.generated.Uint256(_amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> owners(BigInteger param0) {
        final Function function = new Function("owners", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> removeOwner(String owner) {
        final Function function = new Function(
                "removeOwner", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(owner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> revokeConfirmation(BigInteger transactionId) {
        final Function function = new Function(
                "revokeConfirmation", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(transactionId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<List> getTokenList() {
        final Function function = new Function("getTokenList", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteCall<List>(
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteCall<Boolean> isOwner(String param0) {
        final Function function = new Function("isOwner", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> confirmations(BigInteger param0, String param1) {
        final Function function = new Function("confirmations", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0), 
                new org.web3j.abi.datatypes.Address(param1)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<TransactionReceipt> setTokenList(List<String> _tokenList) {
        final Function function = new Function(
                "setTokenList", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                        org.web3j.abi.Utils.typeMap(_tokenList, org.web3j.abi.datatypes.Address.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> constructor(List<String> _owners, BigInteger _required) {
        final Function function = new Function(
                "constructor", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                        org.web3j.abi.Utils.typeMap(_owners, org.web3j.abi.datatypes.Address.class)), 
                new org.web3j.abi.datatypes.generated.Uint256(_required)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> setMyTokenList(List<String> _tokenList) {
        final Function function = new Function(
                "setMyTokenList", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                        org.web3j.abi.Utils.typeMap(_tokenList, org.web3j.abi.datatypes.Address.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> tokens(BigInteger param0) {
        final Function function = new Function("tokens", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> deposit(String _from, BigInteger _amount, String _token, byte[] _data) {
        final Function function = new Function(
                "deposit", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_from), 
                new org.web3j.abi.datatypes.generated.Uint256(_amount), 
                new org.web3j.abi.datatypes.Address(_token), 
                new org.web3j.abi.datatypes.DynamicBytes(_data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> tokenBalances(String param0) {
        final Function function = new Function("tokenBalances", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> getTransactionCount(Boolean pending, Boolean executed) {
        final Function function = new Function("getTransactionCount", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Bool(pending), 
                new org.web3j.abi.datatypes.Bool(executed)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> watch(String _tokenAddr) {
        final Function function = new Function(
                "watch", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_tokenAddr)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> addOwner(String owner) {
        final Function function = new Function(
                "addOwner", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(owner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> isConfirmed(BigInteger transactionId) {
        final Function function = new Function("isConfirmed", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(transactionId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<BigInteger> getConfirmationCount(BigInteger transactionId) {
        final Function function = new Function("getConfirmationCount", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(transactionId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> receiveApproval(String _from, BigInteger _amount, String _token, byte[] _data) {
        final Function function = new Function(
                "receiveApproval", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_from), 
                new org.web3j.abi.datatypes.generated.Uint256(_amount), 
                new org.web3j.abi.datatypes.Address(_token), 
                new org.web3j.abi.datatypes.DynamicBytes(_data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Tuple4<String, BigInteger, byte[], Boolean>> transactions(BigInteger param0) {
        final Function function = new Function("transactions", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Bool>() {}));
        return new RemoteCall<Tuple4<String, BigInteger, byte[], Boolean>>(
                new Callable<Tuple4<String, BigInteger, byte[], Boolean>>() {
                    @Override
                    public Tuple4<String, BigInteger, byte[], Boolean> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<String, BigInteger, byte[], Boolean>(
                                (String) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (byte[]) results.get(2).getValue(), 
                                (Boolean) results.get(3).getValue());
                    }
                });
    }

    public RemoteCall<List> getOwners() {
        final Function function = new Function("getOwners", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteCall<List>(
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteCall<TransactionReceipt> withdrawAllTokens(String _dest) {
        final Function function = new Function(
                "withdrawAllTokens", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_dest)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<List> getTransactionIds(BigInteger from, BigInteger to, Boolean pending, Boolean executed) {
        final Function function = new Function("getTransactionIds", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(from), 
                new org.web3j.abi.datatypes.generated.Uint256(to), 
                new org.web3j.abi.datatypes.Bool(pending), 
                new org.web3j.abi.datatypes.Bool(executed)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteCall<List>(
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteCall<BigInteger> nonce() {
        final Function function = new Function("nonce", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<List> getConfirmations(BigInteger transactionId) {
        final Function function = new Function("getConfirmations", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(transactionId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteCall<List>(
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteCall<BigInteger> transactionCount() {
        final Function function = new Function("transactionCount", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> releaseWallet(String _dest) {
        final Function function = new Function(
                "releaseWallet", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_dest)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changeRequirement(BigInteger _required) {
        final Function function = new Function(
                "changeRequirement", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_required)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> confirmTransaction(BigInteger transactionId) {
        final Function function = new Function(
                "confirmTransaction", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(transactionId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> tokenFallback(String _from, BigInteger _amount, byte[] _data) {
        final Function function = new Function(
                "tokenFallback", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_from), 
                new org.web3j.abi.datatypes.generated.Uint256(_amount), 
                new org.web3j.abi.datatypes.DynamicBytes(_data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> submitTransaction(String destination, BigInteger value, byte[] data) {
        final Function function = new Function(
                "submitTransaction", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(destination), 
                new org.web3j.abi.datatypes.generated.Uint256(value), 
                new org.web3j.abi.datatypes.DynamicBytes(data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> userList(String param0, BigInteger param1) {
        final Function function = new Function("userList", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(param0), 
                new org.web3j.abi.datatypes.generated.Uint256(param1)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<BigInteger> MAX_OWNER_COUNT() {
        final Function function = new Function("MAX_OWNER_COUNT", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> required() {
        final Function function = new Function("required", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> replaceOwner(String owner, String newOwner) {
        final Function function = new Function(
                "replaceOwner", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(owner), 
                new org.web3j.abi.datatypes.Address(newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> depositToken(String _token, byte[] _data) {
        final Function function = new Function(
                "depositToken", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_token), 
                new org.web3j.abi.datatypes.DynamicBytes(_data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> executeTransaction(BigInteger transactionId) {
        final Function function = new Function(
                "executeTransaction", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(transactionId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> withdrawEverything(String _dest) {
        final Function function = new Function(
                "withdrawEverything", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_dest)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static RemoteCall<MultiSigTokenWallet> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(MultiSigTokenWallet.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<MultiSigTokenWallet> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(MultiSigTokenWallet.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static MultiSigTokenWallet load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new MultiSigTokenWallet(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static MultiSigTokenWallet load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new MultiSigTokenWallet(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class ConfirmationEventResponse {
        public Log log;

        public String _sender;

        public BigInteger _transactionId;
    }

    public static class RevocationEventResponse {
        public Log log;

        public String _sender;

        public BigInteger _transactionId;
    }

    public static class SubmissionEventResponse {
        public Log log;

        public BigInteger _transactionId;
    }

    public static class ExecutionEventResponse {
        public Log log;

        public BigInteger _transactionId;
    }

    public static class ExecutionFailureEventResponse {
        public Log log;

        public BigInteger _transactionId;
    }

    public static class DepositEventResponse {
        public Log log;

        public String _sender;

        public BigInteger _value;
    }

    public static class TokenDepositEventResponse {
        public Log log;

        public String _sender;

        public String _token;

        public BigInteger _value;
    }

    public static class OwnerAdditionEventResponse {
        public Log log;

        public String _owner;
    }

    public static class OwnerRemovalEventResponse {
        public Log log;

        public String _owner;
    }

    public static class RequirementChangeEventResponse {
        public Log log;

        public BigInteger _required;
    }
}
