require("babel-polyfill");

window.Ipfs = require('ipfs');
window.OrbitDb = require('orbit-db');
window.wrtc = require('wrtc'); // or require('electron-webrtc')()
window.WStar = require('libp2p-webrtc-star');
window.wstar = new WStar({wrtc: wrtc});
window.Buffer = require('buffer/').Buffer;  // note: the trailing slash is important!