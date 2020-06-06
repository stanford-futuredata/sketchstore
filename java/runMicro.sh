#!/usr/bin/env bash
java -Xmx100g -Xms100g -cp target/sketchstore-1.0-SNAPSHOT.jar:$(cat cp.txt) runner.MicroBench $@
