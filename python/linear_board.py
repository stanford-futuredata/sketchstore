import math
import os

import numpy as np
import pandas as pd
from tqdm import tqdm

import sketch.compress_freq as cf
import sketch.sketch_frequent as f
import sketch.compress_dyadic as cd
import sketch.sketch_gen as board_sketch
import storyboard.board_gen as board_gen


def get_dataset(data_name):
    x_stream = None
    x_to_track = None
    if data_name == "caida_1M":
        df_in = pd.read_csv("notebooks/caida1M-dest-stream.csv")
        x_stream = df_in["Destination"].to_numpy(dtype=int)
        x_df = pd.read_csv("notebooks/caida1M-xtrack.csv")
        x_to_track = x_df["x_track"].to_numpy(dtype=int)
        x_to_track = np.sort(x_to_track)
    elif data_name == "caida_10M":
        df_in = pd.read_csv("/Users/edwardgan/Documents/Projects/datasets/caida-pcap/caida10M-ipdst.csv")
        x_stream = df_in["ip.dst"].values
        x_df = pd.read_csv("/Users/edwardgan/Documents/Projects/datasets/caida-pcap/caida10M-ipdst-xtrack.csv")
        x_to_track = x_df["x_track"].values
    else:
        raise Exception("Invalid Dataset: {}".format(data_name))
    return x_stream, x_to_track


def get_sketch_gen(sketch_name, x_to_track=None) -> board_sketch.SketchGen:
    sketch_gen = None
    if sketch_name == "top_value":
        sketch_gen = board_sketch.ItemDictCompressorGen(
            name=sketch_name,
            compressor=cf.TopValueCompressor(x_to_track=x_to_track)
        )
    elif sketch_name == "cooperative":
        sketch_gen = board_sketch.ItemDictCompressorGen(
            name=sketch_name,
            compressor=cf.IncrementalRangeCompressor()
        )
    elif sketch_name == "random_sample":
        sketch_gen = board_sketch.ItemDictCompressorGen(
            name=sketch_name,
            compressor=cf.UniformSamplingCompressor()
        )
    elif sketch_name == "dyadic":
        sketch_gen = board_sketch.DyadicItemDictGen(
            h_compressor=cd.DyadicFrequencyCompressor(max_height=20)
        )
    return sketch_gen


def run_test(data_name, cur_granularity, sketch_size, sketch_name):
    x_stream, x_to_track = get_dataset(data_name)
    segments = np.array_split(x_stream, cur_granularity)
    sketch_gen = get_sketch_gen(sketch_name, x_to_track=x_to_track)
    if sketch_name == "dyadic":
        dyadic_height = int(math.log2(len(segments)))
        sketch_size /= (dyadic_height + 1)
        print("Dyadic Height: {}, Size:{}".format(dyadic_height, sketch_size))
    board_constructor = board_gen.BoardGen(sketch_gen)

    segment_times = np.cumsum([len(cur_seg) for cur_seg in segments])
    df = board_constructor.generate(
        segments=segments,
        tags=[{
            "t": t, "size": sketch_size
        } for t in segment_times],
    )
    df["dataset"] = data_name
    dir_name = "output/boards/{data}_{granularity}/".format(
        data=data_name,
        granularity=cur_granularity
    )
    output_file_name = os.path.join(dir_name, "{}_df.pkl".format(sketch_name))
    print("Output written to: {}".format(output_file_name))
    os.makedirs(dir_name, exist_ok=True)
    board_constructor.serialize(df, output_file_name)


def main():
    run_test(
        data_name="caida_1M",
        cur_granularity=2048,
        sketch_size=64,
        sketch_name="top_value"
    )


if __name__ == "__main__":
    main()