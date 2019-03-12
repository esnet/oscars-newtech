# oscars-frontend

A frontend for OSCARS 1.0

## Building blocks
- React (v16.4.2)
- MobX (v5.0.0)
- React-Bootstrap (v4.1.3)
- vis.js (v4.21.0)


## Developer guide

### Installing prerequisites

`npm install` (`mvn package` will also do that)

### Bring up a dev server 

`npm run start`

### Packaging for Maven deployment 

`mvn clean package`

### Deploying artifact to github repo

`mvn clean package deploy`
 
## Development notes

- The version number is important; it should be the same in all of these files:
  - `package.json` 
  - `pom.xml`
  - `src/main/resources/frontend/index.html`
  
- In order for the backend to reflect the new changes on the frontend, we need to do the following
  - Bump the version number in the frontend in all the files mentioned above
  - Run a `mvn package deploy` on the frontend (do this ONLY when everything is tested and working as expected)
    - If you're in development mode, you can skip the deploy mode for now. Just do a `mvn install` locally
  - Bump the version dependency on the backend (`oscars-newtech`) in the top level `pom.xml`
  - Then, in the backend, run `mvn clean install` from the top level directory

### IntelliJ IDEA settings 

- Plugins
  - Install NodeJS plugin, restart IDEA  
- Preferences -> Languages & Frameworks
  - Node.js and NPM: Enable
  - Javascript: Set project language to React JSX
  - Javascript -> Libraries
    - Download
    - Select 'jasmine', Download and install, Apply
