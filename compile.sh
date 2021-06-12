#! /bin/bash
rm -rf bin/*.class
javac -cp ".;lib/postgresql-42.1.4.jar;" /extra/nhoss005/code/java/src/DBproject.java -d /extra/nhoss005/code/java/bin/
