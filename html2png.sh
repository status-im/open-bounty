#!/bin/bash -eu
# need to run wkhtmltoimage in quiet mode because it misuses stdout
xvfb-run -a -s "-screen 0 640x480x16" wkhtmltoimage -q "$@" 2>/dev/null
