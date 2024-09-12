#!/bin/bash
sudo apt install graphviz
gem install asciidoctor
gem install asciidoctor-diagram
asciidoctor -V
cd asciidoc || exit
asciidoctor -r asciidoctor-diagram -D ../ sdpi-supplement.adoc
cd ..
mkdir sdpi-supplement
cp -R asciidoc/images sdpi-supplement/images
cp -R asciidoc/js sdpi-supplement/js
cp -R asciidoc/css sdpi-supplement/css
cp -R asciidoc/fonts sdpi-supplement/fonts
rm -rf sdpi-supplement/.asciidoctor
rm -rf sdpi-supplement/readme.md

sudo apt-get install zip gzip tar
zip -r "sdpi-supplement-$1.zip" sdpi-supplement