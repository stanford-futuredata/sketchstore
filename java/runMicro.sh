#!/usr/bin/env bash
java -Xmx10g -Xms10g -cp target/sketchstore-1.0-SNAPSHOT.jar:$(cat cp.txt) runner.MicroBench $@