:: Creates html output file based on a test ascii doc. 
:: We leave off all the html headers to make the test more robust 
:: (that is, test don't depend on css in headers). 

:: Argument: name of test ascii doc file in src/test/resources/

gradlew.bat run --args="--input-file src/test/resources/%1.adoc --output-folder src/test/resources --backend html --test"
