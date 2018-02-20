// require("babel-polyfill");

// window.Gun = require('gun'); // in NodeJS
// window.Gun = require("../../../../gun-level/dist/browser");
const Gun = require('../../../../gun/gun'); // in NodeJS
require("gun-level");
window.Hashes = require('jshashes');

const levelup = require('levelup');
const encode = require('encoding-down');
const jslevel = require("level-js");

window.LevelDB = levelup(
  encode(
    jslevel('data'),
    { valueEncoding: 'json' }
  )
);

var gun = null

window.GunCalls = {
  getHubClass : function(id, cb) {
    gun.get(id).get('class').val(function(d, k) {
      // console.log("SupGun got class " + JSON.stringify(d), k)
      cb(d)
    }, {wait: 0})
  },
  get : function(id, cb) {
    gun.get(id).val(function(d, k) {
      // console.log("SupGun got ", d, k)
      cb(d, k)
    }, {wait: 0})
  },
  getConnections: function(id, cb) {
    gun.get(id).get("connections").val(function(d) {
      // console.log("SupGun got connections " + JSON.stringify(d))
      cb(d)
    }, {wait: 0})
  },
  mapConnections: function(id, cb) {
    gun.get(id).get("connections").map().val(function(d, k) {
      // console.log("SupGun mapping connection", d, k)
      cb(d,k)
    }, {wait: 0})
  },
  instantiate: function(peers) {
    return window.LevelDB.open().then(function(db) {
      console.log("Creating gun instance with peers", peers, db)
      gun = new Gun({
        level: db,
        localStorage: false,
        file: false
        // ,
        // peers: peersToOpt(peers)
      })
    })
  },
  getInstance: function () {
    return gun;
  }
}

function peersToOpt(peers) {
  var peersObj = {}
  peers.forEach(function (e) {
    peersObj.e = {url: e}
  })
  return peersObj
}
