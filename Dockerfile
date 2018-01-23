FROM clojure as builder

WORKDIR /tmp

RUN wget -O /usr/local/bin/solc https://github.com/ethereum/solidity/releases/download/v0.4.15/solc-static-linux
RUN chmod +x /usr/local/bin/solc

RUN wget https://github.com/web3j/web3j/releases/download/v2.3.0/web3j-2.3.0.tar
RUN tar -xf web3j-2.3.0.tar
RUN cp -r  web3j-2.3.0/* /usr/local/


COPY . /usr/src/app
WORKDIR /usr/src/app

ENV LEIN_SNAPSHOTS_IN_RELEASE=1


RUN lein less once
RUN lein uberjar


FROM clojure
WORKDIR /root/

COPY --from=builder /usr/src/app/target/uberjar/commiteth.jar .

CMD [""]
ENTRYPOINT ["/usr/bin/java", "-Duser.timezone=UTC", "-Dconf=config-test.edn", "-jar", "/root/commiteth.jar"]

