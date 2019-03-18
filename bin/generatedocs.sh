#!/bin/bash

# This script auto-generates all documentation contained within OSCARS 1.0.
# Code source files are added to docs/source.
# Deployable and exportable Sphinx output documentation is auto-built and added to docs/build.

# Documentation includes:
    # Manually created user-documentation: located in docs/source/userdoc.
    # Auto-generated code-documentation from Javadoc: located in docs/source/codedoc.

# Required Dependencies:
    # Sphinx
    # Javasphinx
    # Make

if [ ! -f ./bin/generatedocs.sh ]; then
    echo "Please change dirs to the top level directory of the OSCARS distribution."
    exit 1
fi

# Step 1. Compile all the Javadoc into codedoc directory.
javasphinx-apidoc -o ./docs/source/codedoc . -u

#format: -o (output directory) <output directory> <input directory> -u (update outdated only)

# Step 2. Build all HTML files from Sphinx source files (reStructuredText files).
make html --directory ./docs/
