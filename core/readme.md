## Building/Testing the WebUI for Development
1. Run "npm install -g webpack-dev server"
2. Run "webpack-dev-server" to monitor changes to javascript/html.
3. Run "bin/start.sh" in oscars-newtech/bin to launch OSCARS.
4. Navigate to localhost:8080/webpack-dev-server/{insert your endpoint here} to see changes.
5. New endpoints must be added in webpack.config.js under "devServer" as proxies. 