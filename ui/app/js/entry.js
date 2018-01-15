const Ipfs = require('ipfs');
const OrbitDB = require('orbit-db');

// Create the IPFS node instance
const node = new Ipfs({
  init: true, // default
  // init: false,
  // init: {
  //   bits: 1024 // size of the RSA key generated
  // },
  start: true, // default
  EXPERIMENTAL: {
    pubsub: true
  },
});

console.log(node);

node.on('ready', function () {
  // Your node is now ready to use \o/
  console.log("ready", node);

  // stopping a node
  node.stop(function () {
    // node is now 'offline'
  })
});


// Main.main()