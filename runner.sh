#!/bin/bash

cp ./evosuite/master/target/evosuite-master-1.2.0.jar ./evosuite-libs
cp ../evosuite/master/target/evosuite-master-1.2.1-SNAPSHOT.jar ./evosuite-libs
cp ./evosuite/standalone_runtime/target/evosuite-standalone-runtime-1.2.0.jar ./evosuite-libs
cp ../evosuite/standalone_runtime/target/evosuite-standalone-runtime-1.2.1-SNAPSHOT.jar ./evosuite-libs
cd evosuite-libs
if [[$2 -eq "modified"]]
then
    export EVOSUITE="$(pwd)/evosuite-master-1.2.0.jar"
    export EVOSUITE_RUNTIME="$(pwd)/evosuite-standalone-runtime-1.2.0.jar"
else
    export EVOSUITE="$(pwd)/evosuite-master-1.2.1-SNAPSHOT.jar"
    export EVOSUITE_RUNTIME="$(pwd)/evosuite-standalone-runtime-1.2.1-SNAPSHOT.jar"
fi
cd ..
cd $1
mvn clean -DskipTests install
mvn dependency:copy-dependencies
cd evosuite-tests
export CLASSPATH="../target/dependency/hamcrest-core-1.3.jar:../target/dependency/junit-4.11.jar:../target/classes:$EVOSUITE_RUNTIME:."
javac tutorial/*.java
java org.junit.runner.JUnitCore tutorial.Foo_ESTest
