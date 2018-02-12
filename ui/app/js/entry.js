// require("babel-polyfill");

import PouchDB from 'pouchdb'
import PouchDBAuthentication from 'pouchdb-authentication'

// window.PouchDB = require('pouchdb');
// PouchDB.plugin(require('pouchdb-authentication'));
PouchDB.plugin(PouchDBAuthentication);
window.PouchDB = PouchDB;
window.Hashes = require('jshashes');
// window.Gun = require('gun'); // in NodeJS
window.Gun = require('../../../../gun/gun'); // in NodeJS
