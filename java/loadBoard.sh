#!/usr/bin/env bash
java -Xmx12g -Xms12g -cp target/sketchstore-1.0-SNAPSHOT.jar:$(cat cp.txt) runner.LoadBoard $@