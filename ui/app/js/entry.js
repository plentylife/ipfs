// require("babel-polyfill");

// window.Gun = require('gun'); // in NodeJS
// window.Gun = require("../../../../gun-level/dist/browser");
// window.Gun = require('../../../../gun/gun'); // in NodeJS
window.Hashes = require('jshashes');
window.Gun = require("gun-level");

const levelup = require('levelup');
const encode = require('encoding-down');
const jslevel = require("level-js");

window.LevelDB = levelup(
  encode(
    jslevel('data'),
    { valueEncoding: 'json' }
  )
);
