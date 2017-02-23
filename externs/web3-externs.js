
/*
externs for web3 injected into js context. needed for things to work with advanced cljs compilation

TODO: probably not a good idea to maintain this manually in the long run*/

var web3 = {
    eth: {
        accounts: [],
        sendTransaction: function() {}
    }
};
