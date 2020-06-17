# CoopStore
Prototype implementation and evaluation for Cooperative summaries.
Full datasets used for evaluation not included.

The main implementation is in the java/ subdirectory. 
LBGFS-B optimization for cube summaries is in the cpp/ subdirectory.
Code for preparing datasets and generating plots is in the notebooks/ directory.

# Building + Running
```
cd cpp/
cmake .
make
cd ..

cd java/
mvn package
./genCP.sh

./loadRunner.sh conf/l_small_f.json
./queryRunner.sh conf/l_small_f.json
```
