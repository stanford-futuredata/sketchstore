import math
import os
from typing import Sequence

import numpy as np
import pandas as pd
from tqdm import tqdm

import sketch.compress_freq as cf
import sketch.compress_quant as cq
import sketch.compress_dyadic as cd
import sketch.sketch_frequent as f
import sketch.sketch_gen as board_sketch
import storyboard.board_gen as board_gen

DATA_DIR = "/mnt/disks/data/datasets/"

def get_tracked(data_name) -> np.ndarray:
    x_to_track = []
    if data_name == "caida_1M":
        x_df = pd.read_csv("notebooks/caida1M-xtrack.csv")
        x_to_track = x_df["x_track"].values
    elif data_name == "caida_10M":
        fname = os.path.join(DATA_DIR, "caida/caida10M-ipdst-xtrack.csv")
        x_df = pd.read_csv(fname)
        x_to_track = x_df["x_track"].values[:200]
    elif data_name == "uniform_1M":
        x_to_track = np.linspace(0, 1, 101)
    elif data_name == "power_2M":
        fname = os.path.join(DATA_DIR, "power/power_tracked.csv")
        x_df = pd.read_csv(fname)
        x_to_track = x_df["x_track"].values
    elif data_name == "zipf1p1_10M":
        fname = os.path.join(DATA_DIR, "zipf/zipf10M-xtrack.csv")
        x_df = pd.read_csv(fname)
        x_to_track = x_df["x_track"].values[:200]
    elif data_name == "msft_records_10M":
        fname = os.path.join(DATA_DIR, "msft/mb-10M-records-track.csv")
        x_df = pd.read_csv(fname)
        x_to_track = x_df["x_track"].values
    elif data_name == "msft_network_10M":
        fname = os.path.join(DATA_DIR, "msft/mb-10M-network-track.csv")
        x_df = pd.read_csv(fname)
        x_to_track = x_df["x_track"].values
    elif data_name == "msft_os_10M":
        fname = os.path.join(DATA_DIR, "msft/mb-10M-os-track.csv")
        x_df = pd.read_csv(fname)
        x_to_track = x_df["x_track"].values
    else:
        raise Exception("Invalid Dataset: {}".format(data_name))
    return np.sort(x_to_track)


def get_dataset(data_name) -> np.ndarray:
    x_stream = None
    x_to_track = None
    if data_name == "caida_1M":
        df_in = pd.read_csv("notebooks/caida1M-dest-stream.csv")
        x_stream = df_in["Destination"].values
    elif data_name == "caida_10M":
        fname = os.path.join(DATA_DIR, "caida/caida10M-ipdst.csv")
        df_in = pd.read_csv(fname)
        x_stream = df_in["ip.dst"].values
    elif data_name == "zipf1p1_10M":
        fname = os.path.join(DATA_DIR, "zipf/zipf10M.csv")
        df_in = pd.read_csv(fname, nrows=10_000_000)
        x_stream = df_in["x"].values
    elif data_name == "uniform_1M":
        r = np.random.RandomState(0)
        x_stream = r.uniform(0, 1, size=1_000_000)
    elif data_name == "power_2M":
        fname = os.path.join(DATA_DIR, "power/power.csv")
        df_in = pd.read_csv(fname)
        x_stream = df_in["Global_active_power"].values
    elif data_name == "msft_records_10M":
        fname = os.path.join(DATA_DIR, "msft/mb-10M.csv")
        df_in = pd.read_csv(fname)
        x_stream = df_in["records_received_count"].values
    elif data_name == "msft_network_10M":
        fname = os.path.join(DATA_DIR, "msft/mb-10M.csv")
        df_in = pd.read_csv(fname)
        x_stream = df_in["DeviceInfo_NetworkProvider"].values
    elif data_name == "msft_os_10M":
        fname = os.path.join(DATA_DIR, "msft/mb-10M.csv")
        df_in = pd.read_csv(fname)
        x_stream = df_in["DeviceInfo_OsBuild"].values
    else:
        raise Exception("Invalid Dataset: {}".format(data_name))
    return x_stream


def get_dyadic_base(sketch_name: str) -> int:
    return int(sketch_name[sketch_name.rfind("_b") + 2:])


def get_dyadic_adjusted_size(size: int, base: int, max_segments: int):
    dyadic_height = int(math.log(max_segments, base))
    return dyadic_height, size / (dyadic_height + 1)


def get_sketch_gen(sketch_name: str, x_to_track: Sequence = None) -> board_sketch.SketchGen:
    sketch_gen = None
    if sketch_name == "top_values":
        sketch_gen = board_sketch.ItemDictCompressorGen(
            name=sketch_name,
            compressor=cf.TopValueCompressor(x_to_track=x_to_track)
        )
    elif sketch_name == "cooperative":
        sketch_gen = board_sketch.ItemDictCompressorGen(
            name=sketch_name,
            compressor=cf.IncrementalRangeCompressor()
        )
    elif sketch_name.startswith("cooperative"):
        base = get_dyadic_base(sketch_name)
        sketch_gen = board_sketch.ItemDictCompressorGen(
            name=sketch_name,
            compressor=cf.IncrementalRangeCompressor(max_t=base)
        )
    elif sketch_name == "random_sample":
        sketch_gen = board_sketch.ItemDictCompressorGen(
            name=sketch_name,
            compressor=cf.UniformSamplingCompressor()
        )
    elif sketch_name == "cms_min":
        sketch_gen = board_sketch.CMSGen()
    elif sketch_name == "truncation":
        sketch_gen = board_sketch.ItemDictCompressorGen(
            name=sketch_name,
            compressor=cf.TruncationCompressor()
        )
    elif sketch_name == "pps":
        sketch_gen = board_sketch.ItemDictCompressorGen(
            name=sketch_name,
            compressor=cf.PPSCompressor()
        )
    elif sketch_name.startswith("dyadic"):
        base = get_dyadic_base(sketch_name)
        sketch_gen = board_sketch.DyadicItemDictGen(
            h_compressor=cd.DyadicFrequencyCompressor(max_height=20, base=base)
        )
    ## Quantile Sketches
    elif sketch_name == "q_top_values":
        sketch_gen = board_sketch.SeqDictCompressorGen(
            name=sketch_name,
            compressor=cq.RankTracker(x_tracked=np.unique(x_to_track))
        )
    elif sketch_name == "q_cooperative":
        sketch_gen = board_sketch.SeqDictCompressorGen(
            name=sketch_name,
            compressor=cq.CoopCompressorFinite()
        )
    elif sketch_name == "q_random_sample":
        sketch_gen = board_sketch.SeqDictCompressorGen(
            name=sketch_name,
            compressor=cq.QRandomSampleCompressor()
        )
    elif sketch_name == "kll":
        sketch_gen = board_sketch.KLLGen()
    elif sketch_name == "q_truncation":
        sketch_gen = board_sketch.SeqDictCompressorGen(
            name=sketch_name,
            compressor=cq.SkipCompressor(biased=True)
        )
    elif sketch_name == "q_pps":
        sketch_gen = board_sketch.SeqDictCompressorGen(
            name=sketch_name,
            compressor=cq.SkipCompressor(biased=False)
        )
    elif sketch_name.startswith("q_dyadic"):
        base = get_dyadic_base(sketch_name)
        sketch_gen = board_sketch.DyadicSeqDictGen(
            h_compressor=cd.DyadicQuantileCompressor(max_height=20, base=base)
        )
    else:
        raise Exception("Invalid Sketch: {}".format(sketch_name))
    return sketch_gen


def get_file_name(data_name: str, granularity: int, sketch_name: str, sketch_size: int):
    dir_name = "output/boards/{data}_{granularity}/".format(
        data=data_name,
        granularity=granularity
    )
    output_file_name = os.path.join(
        dir_name,
        "{}_{}.pkl".format(sketch_name, int(sketch_size))
    )
    return output_file_name


def get_totals_name(data_name: str, granularity: int):
    dir_name = "output/boards/{data}_{granularity}/".format(
        data=data_name,
        granularity=granularity
    )
    output_file_name = os.path.join(
        dir_name,
        "totals.csv"
    )
    return output_file_name


def write_totals(data_name: str, granularity: int, segments: Sequence):
    output_file_name = get_totals_name(data_name, granularity)
    df = pd.DataFrame()
    seg_sizes = [len(segment) for segment in segments]
    df["total"] = seg_sizes
    df["seg_idx"] = list(range(len(segments)))
    df.to_csv(output_file_name, index=False)


def run_test(data_name, cur_granularity, sketch_size, sketch_name):
    x_stream = get_dataset(data_name)
    x_to_track = get_tracked(data_name)
    segments = np.array_split(x_stream, cur_granularity)
    sketch_gen = get_sketch_gen(sketch_name, x_to_track=x_to_track)
    size_arg = sketch_size
    if "dyadic" in sketch_name:
        base = get_dyadic_base(sketch_name)
        dyadic_height, size_arg = get_dyadic_adjusted_size(
            size=sketch_size, base=base, max_segments=len(segments))
        print("Dyadic Base: {}, Height: {}, Size:{}".format(base, dyadic_height, size_arg))
    board_constructor = board_gen.BoardGen(sketch_gen)

    segment_times = np.cumsum([len(cur_seg) for cur_seg in segments])
    df = board_constructor.generate(
        segments=segments,
        tags=[{
            "t": t, "size": size_arg
        } for t in segment_times],
    )
    df["dataset"] = data_name
    output_file_name = get_file_name(
        data_name=data_name,
        granularity=cur_granularity,
        sketch_name=sketch_name,
        sketch_size=sketch_size,
    )
    dir_name = os.path.split(output_file_name)[0]
    print("Output written to: {}".format(output_file_name))
    os.makedirs(dir_name, exist_ok=True)
    write_totals(data_name, granularity=cur_granularity, segments=segments)
    board_constructor.serialize(df, output_file_name)

space_experiment = [
    {
        "data_name": "zipf1p1_10M",
        "quantile": False,
        "granularity": 2048,
        # "baseline_sizes": [4, 8, 16, 32, 64, 128, 256, 512],
        "baseline_sizes": [64],
        "sketches": [
            "top_values",
            "cooperative",
            "random_sample",
            "cms_min",
            "truncation",
            "pps",
            "dyadic_b2",
            # "dyadic_b4",
            # "dyadic_b10"
        ]
    }, # 0
    {
        "data_name": "caida_10M",
        "quantile": False,
        "granularity": 2048,
        # "baseline_sizes": [4, 8, 16, 32, 64, 128, 256, 512],
        "baseline_sizes": [64],
        "sketches": [
            "top_values",
            "cooperative",
            "random_sample",
            "cms_min",
            "truncation",
            "pps",
            "dyadic_b2",
            # "dyadic_b4",
            # "dyadic_b10",
        ]
    },  # 1
    {
        "data_name": "uniform_1M",
        "quantile": True,
        "granularity": 2048,
        # "baseline_sizes": [4, 8, 16, 32, 64, 128, 256, 512],
        "baseline_sizes": [64],
        "sketches": {
            "q_cooperative",
            "q_random_sample",
            "q_truncation",
            "q_pps",
            "kll",
            "q_dyadic_b2",
            "q_dyadic_b3",
        }
    },  # 2
    {
        "data_name": "power_2M",
        "quantile": True,
        "granularity": 2048,
        "baseline_sizes": [64],
        "sketches": {
            "q_top_values",
            "q_cooperative",
            "q_random_sample",
            "q_truncation",
            "q_pps",
            "kll",
            "q_dyadic_b2",
        }
    },  # 3
    {
        "data_name": "caida_10M",
        "quantile": False,
        "granularity": 2048,
        "baseline_sizes": [64],
        "sketches": [
            "cooperative_b8",
            "cooperative_b32",
            "cooperative_b128",
            "cooperative_b512",
            "cooperative_b2048"
        ],
        "query_lens": [64],
        "num_queries": 100,
    },  # 4: varying lookback range
    {
        "data_name": "msft_records_10M",
        "quantile": True,
        "granularity": 2048,
        "baseline_sizes": [64],
        "sketches": {
            "q_top_values",
            "q_cooperative",
            "q_random_sample",
            "q_truncation",
            "q_pps",
            "kll",
            "q_dyadic_b2",
        }
    },  # 5
    {
        "data_name": "msft_network_10M",
        "quantile": False,
        "granularity": 2048,
        "baseline_sizes": [64],
        "sketches": {
            "top_values",
            "cooperative",
            "random_sample",
            "cms_min",
            "truncation",
            "pps",
            "dyadic_b2",
        }
    },  # 6
    {
        "data_name": "msft_os_10M",
        "quantile": False,
        "granularity": 2048,
        "baseline_sizes": [64],
        "sketches": [
            "top_values",
            "cooperative",
            "random_sample",
            "cms_min",
            "truncation",
            "pps",
            "dyadic_b2",
        ]
    },  # 7
    {
        "data_name": "caida_10M",
        "quantile": False,
        "granularity": 2048,
        "accumulator_sizes": [64, 128, 1000, 10_000, 100_000, 1_000_000],
        "query_lens": [512],
        "baseline_sizes": [64],
        "sketches": [
            "top_values",
            "cooperative",
            "random_sample",
            # "cms_min",
            "truncation",
            "pps",
            # "dyadic_b2",
            # "dyadic_b4",
            # "dyadic_b10",
        ]
    },  # 8
]

def main():
    experiment_id = 7
    cur_experiment = space_experiment[experiment_id]
    data_name = cur_experiment["data_name"]
    cur_granularity = cur_experiment["granularity"]
    sketch_sizes = cur_experiment["baseline_sizes"]
    sketch_names = cur_experiment["sketches"]
    quantile = cur_experiment["quantile"]

    for sketch_size in sketch_sizes:
        print("Sketch Size: {}".format(sketch_size))
        for sketch_name in sketch_names:
            run_test(
                data_name=data_name,
                cur_granularity=cur_granularity,
                sketch_size=sketch_size,
                sketch_name=sketch_name
            )


if __name__ == "__main__":
    main()