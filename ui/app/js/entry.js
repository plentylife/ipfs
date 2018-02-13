// require("babel-polyfill");

window.Hashes = require('jshashes');
// window.Gun = require('gun'); // in NodeJS
window.Gun = require('../../../../gun/gun'); // in NodeJS
require("gun-level");
const levelup = require('levelup');
const encode = require('encoding-down');
const jslevel = require("level-js");

window.levelDB = levelup(
  encode(
    jslevel('data'),
    { valueEncoding: 'json' }
  )
);


levelDB.open().then(db => {
  console.log(db);
  window.igun = new Gun({
    file: false,
    localStorage: false,
    level: db
    // level: levelDB
  })
});
