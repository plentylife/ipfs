// require("babel-polyfill");

// window.Gun = require('gun'); // in NodeJS
// window.Gun = require("../../../../gun-level/dist/browser");
// window.Gun = require('../../../../gun/gun'); // in NodeJS
window.Hashes = require('jshashes');
window.GunConstructor = require("gun-level");

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
  gunGetClass : function(id, cb) {
    console.log("SupGun getClass")
    gun.get(id).get('class').val(function(d) {
      console.log("SupGun got class " + JSON.stringify(d))
      cb(d)
    }, {wait: 0})
  },
  gunGet : function(id, cb) {
    console.log("SupGun get")
    gun.get(id).val(function(d) {
      console.log("SupGun got " + JSON.stringify(d))
      cb(d)
    }, {wait: 0})
  },
  getConnections: function(id, cb) {
    gun.get(id).get("connections").val(function(d) {
      console.log("SupGun got connections " + JSON.stringify(d))
      cb(d)
    }, {wait: 0})
  },
  instantiate: function(peers) {
    return window.LevelDB.open().then(function(db) {
      gun = Gun({
        level: db,
        localStroage: false,
        file: false,
        peers: peers
      })
    })
  },
  getInstance: function () {
    return gun;
  }
}
