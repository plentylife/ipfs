// require("babel-polyfill");

// window.Gun = require('gun'); // in NodeJS
// window.Gun = require("../../../../gun-level/dist/browser");
// const Gun = require('../../../../gun/gun');
const Gun = require("gun-level");
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

window.gun = null

class GunCalls {

  constructor(peers) {
    window.gun = new Gun({
      level: LevelDB,
      localStorage: false,
      file: false,
      peers: this.peersToOpt(peers)
    })
  }

  getHubClass(id, cb) {
    gun.get(id).get('class').val(function(d, k) {
      console.log("SupGun got class of", id, d, k)
      cb(d)
    })
    // }, {wait: 0})
  }
  get(id, cb) {
    gun.get(id).val(function(d, k) {
      // console.log("SupGun got ", d, k)
      cb(d, k)
    }, {wait: 0})
  }
  getConnections(id, cb) {
    gun.get(id).get("connections").val(function(d) {
      // console.log("SupGun got connections " + JSON.stringify(d))
      cb(d)
    }, {wait: 0})
  }
  mapConnections(id, cb) {
    gun.get(id).get("connections").map().val(function(d, k) {
      // console.log("SupGun mapping connection", d, k)
      cb(d,k)
    }, {wait: 0})
  }
  getInstance() {
    return gun;
  }

  peersToOpt(peers) {
    var peersObj = {}
    peers.forEach(function (e) {
      peersObj.e = {url: e}
    })
    return peersObj
  }

}

window.GC = GunCalls

