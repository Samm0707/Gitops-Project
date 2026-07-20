// webpack.config.js
const path = require('path');

module.exports = {
  mode: 'development', // or 'production' for a production build
  entry: './src/index.js', // Adjust this path to your main React entry file
  output: {
    filename: 'dashboard.bundle.js', // The name of your bundled file
    path: path.resolve(__dirname, 'js'), // The output directory for the bundle
  },
  module: {
    rules: [
      {
        test: /\.jsx?$/, // Process .js and .jsx files with Babel
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env', '@babel/preset-react'],
          },
        },
      },
      // You can add rules for other file types here (e.g., CSS, images)
    ],
  },
  resolve: {
    extensions: ['.js', '.jsx'], // Allow importing .js and .jsx files without specifying the extension
    alias: {
      '@': path.resolve(__dirname, 'src/'), // Optional: for easier relative imports
    },
  },
};