var path = require('path');

module.exports = {
    entry: './src/main/js/app.js',
    devtool: 'sourcemaps',
    cache: true,
    debug: true,
    output: {
        path:  path.join(__dirname, 'src', 'main', 'resources', 'static', 'built'),
        filename: 'bundle.js'
    },
    module: {
        loaders: [
            {
                test: /\.js|\.jsx/,
                exclude: /(node_modules)/,
                loader: 'babel',
                query: {
                    cacheDirectory: true,
                    presets: ['es2015', 'react']
                }
            }
        ]
    },
    devServer: {
        port: 8181,
        contentBase: 'src/main/resources/static/built/',
        historyApiFallback: {
            index: 'index.html'
        },
        proxy: {
            "/resv/*": {
                secure: false,
                target: "https://localhost:8001/"
            },
            "/viz/*": {
                secure: false,
                target: "https://localhost:8001/"
            },
            "/topology/*": {
                secure: false,
                target: "https://localhost:8001/"
            },
            "/info/*": {
                secure: false,
                target: "https://localhost:8001/"
            }
        },
        watchOptions: {
            poll: 1000
        }
    }

};