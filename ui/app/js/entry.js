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

window.SupGun = {
  gunGetClass : function(g, id, cb) {
    console.log("SupGun getClass")
    g.get(id).get('class').val(function(d) {
      console.log("SupGun got class " + JSON.stringify(d))
      cb(d)
    }, {wait: 0})
  },
  gunGet : function(g, id, cb) {
    console.log("SupGun get")
    g.get(id).val(function(d) {
      console.log("SupGun got " + JSON.stringify(d))
      cb(d)
    }, {wait: 0})
  }
}
