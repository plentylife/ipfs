require("babel-polyfill");

window.sodium = require('libsodium-wrappers-sumo')
window.Hashes = require('jshashes');
let sharedb = require('sharedb/lib/client');

window.startDB = function(peer) {
  var socket = new WebSocket(peer);
  window.ShareDB = new sharedb.Connection(socket);
  return ShareDB;
}

window.positionSectionNav = function() {
  var bss = document.getElementsByClassName("section-nav-buttons")
  if (bss.length > 0) {
    var bs = bss[0]
    var rect = bs.getBoundingClientRect()
    var w = window.innerWidth - rect.left
    console.log("BS", window.innerWidth, window.scrollX, w, rect.left)
    // console.log(rect)
    bs.style = "width:" + w + "px;"
  }
}

window.onresize = positionSectionNav
window.onscroll = positionSectionNav