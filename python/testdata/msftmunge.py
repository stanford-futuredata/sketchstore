import numpy as np
import pandas as pd
import random
from tqdm import tqdm
import math
import os
import json

fname = "/Users/edwardgan/Documents/Projects/datasets/msft/mb200k.tsv"
outfname = "/Users/edwardgan/Documents/Projects/datasets/msft/mb200k.feather"

column_names = [
    'PipelineInfo_IngestionTime',
 'SDKVersion',
 'APIVersion',
 'DeviceHash_Id',
 'AppInfo_Language',
 'AppInfo_Version',
 'DeviceInfo_Make',
 'DeviceInfo_OsBuild',
 'DeviceInfo_OsVersion',
 'DeviceInfo_Model',
 'DeviceInfo_NetworkType',
 'DeviceInfo_NetworkProvider',
 'UserInfo_Language',
 'UserInfo_TimeZone',
 'eventpriority',
 'records_received_count',
 'records_tried_to_send_count',
 'records_sent_count',
 'olsize',
 'olsize_start',
 'olc_start',
 'ol_w',
 'olc',
 'records_dropped_count',
#  'UserHash_Id',
 'inq',
 'infl',
 'r_count',
 'PipelineInfo_ClientCountry',
 'EventInfo_InitId',
 'EventInfo_Sequence',
 'e_meth',
 'TenantId',
 'DataPackageId',
 'EventInfo_Time',
 'r_no_name',
 'r_size',
 'r_ban',
 'r_kl',
 'r_ps',
 'r_403',
 'r_inv',
 'd_assert',
 'd_bad_tenant',
 'd_disk_full',
 'd_io_fail',
 'd_bond_fail',
 'd_disk_off',
 'd_unk']

df = pd.read_csv(
    fname,
    sep="\t",
    names=column_names
)

f_metrics = [
    "DeviceInfo_OsBuild",
    "DeviceInfo_NetworkProvider"
]
q_metric = "records_received_count"
dims = [
    "TenantId",
    "AppInfo_Version",
    "UserInfo_TimeZone",
    "DeviceInfo_NetworkType",
]

df[q_metric] = df[q_metric].fillna(0)

for cur_f in tqdm(f_metrics + dims):
    df[cur_f] = df[cur_f].fillna("na")
    vc = df[cur_f].value_counts()
    vc_rep = dict(zip(
        vc.index,
        range(len(vc))
    ))
    df.replace({cur_f: vc_rep}, inplace=True)

df[dims + f_metrics + [q_metric]].to_feather(
    outfname
)