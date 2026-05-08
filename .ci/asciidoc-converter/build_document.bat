mkdir ..\..\sdpi-documents
mkdir ..\..\sdpi-documents\sdpi-standard
mkdir ..\..\sdpi-documents\sdpi-supplement
gradlew.bat run --args="--input-file ../../asciidoc/sdpi-standard.adoc --output-folder ../../sdpi-documents/sdpi-standard --backend html"
gradlew.bat run --args="--input-file ../../asciidoc/sdpi-supplement.adoc --output-folder ../../sdpi-documents/sdpi-supplement --backend html"
