const path = require('path');
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');

module.exports = {
  entry: './js/entry.js',
  output: {
    filename: 'entry.js',
    path: path.resolve(__dirname)
  },
  plugins: [
    // new UglifyJSPlugin()
  ]
};
