#!/bin/bash
gem install asciidoctor
gem install asciidoctor-diagram
asciidoctor -V
cd asciidoc
asciidoctor -r asciidoctor-diagram -D ../ sdpi-supplement.adoc
cd ..
mkdir sdpi-supplement
cp -R asciidoc/images sdpi-supplement/images
cp -R asciidoc/js sdpi-supplement/js
rm -rf sdpi-supplement/.asciidoctor
rm -rf sdpi-supplement/readme.md
