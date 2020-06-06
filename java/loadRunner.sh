#!/usr/bin/env bash
java -Xmx120g -Xms120g -cp target/sketchstore-1.0-SNAPSHOT.jar:$(cat cp.txt) runner.LoadRunner $@
