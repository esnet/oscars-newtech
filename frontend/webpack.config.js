var path = require('path');
var packageJSON = require('./package.json');
var HtmlWebpackPlugin = require('html-webpack-plugin');


const PATHS = {
    build: path.join(__dirname, 'target', 'classes', 'META-INF', 'resources', 'webjars', packageJSON.name, packageJSON.version),
    templates: path.join(__dirname, 'src', 'main', 'resources', 'templates')
};

module.exports = {
    entry: './src/main/js/app.js',
    devtool: 'sourcemaps',
    cache: true,
    output: {
        path: PATHS.build,
        filename: 'bundle.js'
    },
    module: {
        loaders: [
            {
                test: /\.js|\.jsx/,
                exclude: /(node_modules)/,
                loader: 'babel-loader',
                query: {
                    cacheDirectory: true,
                    presets: ['es2015', 'react']
                }
            }
        ]
    },
    plugins: [new HtmlWebpackPlugin({
        template: PATHS.templates + "/template_index.html",
        inject: "body"
    })],

    devServer: {
        port: 8181,
        contentBase: PATHS.build,
        historyApiFallback: {
            index: 'index.html'
        },
        watchOptions: {
            poll: 1000
        }
    }
};