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
  let bss = document.getElementsByClassName("section-nav-buttons")
  if (bss.length > 0) {
    let bs = bss[0]
    let rect = bs.getBoundingClientRect()
    let w = window.innerWidth - rect.left
    bs.style = "width:" + w + "px;"
    // console.log("BS", window.innerWidth, window.scrollX, w, rect.left)
    // console.log(document.getElementsByTagName("html")[0].scrollWidth)
  }
}

window.onresize = positionSectionNav
window.onscroll = positionSectionNav
