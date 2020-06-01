#!/usr/bin/env bash
java -server -XX:CompileThreshold=50 -Xmx12g -Xms12g -cp target/sketchstore-1.0-SNAPSHOT.jar:$(cat cp.txt) runner.QueryRunner $@