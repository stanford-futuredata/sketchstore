#!/usr/bin/env bash
./loadRunner.sh conf/remote/l_caida_f.json
./loadRunner.sh conf/remote/l_mnetwork_f.json
./loadRunner.sh conf/remote/l_mos_f.json
./loadRunner.sh conf/remote/l_mrecords_q.json
./loadRunner.sh conf/remote/l_power_q.json
./loadRunner.sh conf/remote/l_uniform_q.json
./loadRunner.sh conf/remote/l_zipf_f.json

./loadRunner.sh conf/remote/c_bcube_f.json
./loadRunner.sh conf/remote/c_bcube_lesion_f.json
./loadRunner.sh conf/remote/c_bcube_q.json
./loadRunner.sh conf/remote/c_bcube_work2_f.json
./loadRunner.sh conf/remote/c_bcube_work3_f.json
./loadRunner.sh conf/remote/c_insta_f.json
./loadRunner.sh conf/remote/c_mnetwork_f.json
./loadRunner.sh conf/remote/c_mos_f.json
./loadRunner.sh conf/remote/c_mrecords_q.json