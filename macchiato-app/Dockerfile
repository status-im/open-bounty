FROM mhart/alpine-node:latest

MAINTAINER Your Name <you@example.com>

# Create app directory
RUN mkdir -p /macchiato-web3-example
WORKDIR /macchiato-web3-example

# Install app dependencies
COPY package.json /macchiato-web3-example
RUN npm install pm2 -g
RUN npm install

# Bundle app source
COPY target/release/macchiato-web3-example.js /macchiato-web3-example/macchiato-web3-example.js
COPY public /macchiato-web3-example/public

ENV HOST 0.0.0.0

EXPOSE 3000
CMD [ "pm2-docker", "/macchiato-web3-example/macchiato-web3-example.js" ]
