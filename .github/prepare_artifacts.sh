#!/bin/bash
sudo apt install graphviz
gem install asciidoctor
gem install asciidoctor-diagram
gem install asciidoctor-diagram-plantuml
asciidoctor -V
cd asciidoc || exit
asciidoctor -r asciidoctor-diagram -D ../ sdpi-standard.adoc
cd ..
mkdir sdpi-standard
cp -R asciidoc/images sdpi-standard/images
cp -R asciidoc/js sdpi-standard/js
cp -R asciidoc/css sdpi-standard/css
cp -R asciidoc/fonts sdpi-standard/fonts
rm -rf sdpi-standard/.asciidoctor
rm -rf sdpi-standard/readme.md

sudo apt-get install zip gzip tar
zip -r "sdpi-standard-$1.zip" sdpi-standard