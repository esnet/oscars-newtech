- the templates/template_index.html file is th one used by the webpack dev server
and injected at runtime with the generated bundle.js

it is also required by the production build but the output is not used
(the paths end up mangled in the webjar)

- the frontend/index.html is the one that is served by the backend. it needs to be
changed to point to the right path when the artifact version changes

