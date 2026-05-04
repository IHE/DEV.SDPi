#!/bin/bash
sudo apt install graphviz
gem install asciidoctor
gem install asciidoctor-diagram
gem install asciidoctor-diagram-plantuml
asciidoctor -V
cd asciidoc || exit

mkdir sdpi-documents

asciidoctor -r asciidoctor-diagram -D ../sdpi-documents sdpi-standard.adoc
asciidoctor -r asciidoctor-diagram -D ../sdpi-documents sdpi-supplement.adoc
cd ..

mkdir sdpi-documents/sdpi-standard
cp -R asciidoc/images sdpi-documents/sdpi-standard/images
cp -R asciidoc/js sdpi-documents/sdpi-standard/js
cp -R asciidoc/css sdpi-documents/sdpi-standard/css
cp -R asciidoc/fonts sdpi-documents/sdpi-standard/fonts
rm -rf sdpi-documents/sdpi-standard/.asciidoctor
rm -rf sdpi-documents/sdpi-standard/readme.md

mkdir sdpi-documents/sdpi-supplement
cp -R asciidoc/images sdpi-documents/sdpi-supplement/images
cp -R asciidoc/js sdpi-documents/sdpi-supplement/js
cp -R asciidoc/css sdpi-documents/sdpi-supplement/css
cp -R asciidoc/fonts sdpi-documents/sdpi-supplement/fonts
rm -rf sdpi-documents/sdpi-supplement/.asciidoctor
rm -rf sdpi-documents/sdpi-supplement/readme.md

rm sdpi-documents/sdpi-standard.html
rm sdpi-documents/sdpi-supplement.html

sudo apt-get install zip gzip tar
zip -r "sdpi-documents-$1.zip" sdpi-documents