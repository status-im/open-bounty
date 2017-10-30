#!/usr/bin/env sh

# TODO: Gulp etc pipeline, for now just HTML tweaks

echo "Copying index.html, dest, and assets to resources/{templates,public}."
cp static_langing_page/index.html resources/templates/index.html
cp -r static_langing_page/dest/ resources/public/dest/
cp -r static_langing_page/assets/ resources/public/assets/



