mkdir ..\..\sdpi-documents
mkdir ..\..\sdpi-documents\sdpi-standard-debug
gradlew.bat run --args="--input-file ../../asciidoc/sdpi-standard.adoc --output-folder ../../sdpi-documents/sdpi-standard-debug --backend html --debug"
