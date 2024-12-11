#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Compile Credentials.java
javac -d $DIR/../classes $DIR/../src/Credentials.java

# Compile PizzaStore.java
javac -d $DIR/../classes -cp $DIR/../classes $DIR/../src/PizzaStore.java

# Run the Java program
java -cp $DIR/../classes:$DIR/../lib/pg73jdbc3.jar PizzaStore "${USER}_project_phase_3_DB" $PGPORT $USER


