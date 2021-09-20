### Cucumber Test Suite for Mesh's Data Pipeline

**To execute tests:** 
mvn test

**Check the test report at:**
target/cucumber/index.html

**Generate Java classes for Protobuf schema**
protoc --java_out=src/test/java src/test/resources/AddressBook.proto 
