import math
import os
from typing import Sequence, Tuple

import numpy as np
import pandas as pd
from tqdm import tqdm

import sketch.compress_freq as cf
import sketch.compress_quant as cq
import sketch.compress_dyadic as cd
import sketch.sketch_gen as sketch_gen
import storyboard.board_gen as board_gen
import testdata.bench_gen
import linear_board

import storyboard.size_optimizer
import storyboard.bias_solver as bopt


def get_file_name(
        data_name: str, split_strategy: str,
        board_size: int, sketch_name: str,
        bias: bool,
) -> str:
    dir_name = "output/boards/{data}/".format(
        data=data_name,
    )
    output_file_name = os.path.join(
        dir_name,
        "{}_{}_{}_b{}.pkl".format(
            sketch_name, split_strategy, int(board_size),
            int(bias)
        )
    )
    return output_file_name


def get_totals_name(data_name: str) -> str:
    dir_name = "output/boards/{data}/".format(
        data=data_name,
    )
    output_file_name = os.path.join(
        dir_name,
        "totals.csv"
    )
    return output_file_name


def get_tracked(data_name) -> np.ndarray:
    if data_name == "synthf@2":
        df, _ = testdata.bench_gen.gen_data(
            200, [], f_skew=1.1, f_card=10000, seed=17
        )
        return df["f"].values
    elif data_name == "synthf@4":
        df, _ = testdata.bench_gen.gen_data(
            200, [], f_skew=1.1, f_card=10000, seed=17
        )
        return df["f"].values
    elif data_name == "bsynthf@4":
        df = pd.read_csv(
            "/Users/edwardgan/Documents/Projects/datasets/sketchstore_synth/bcube4_10M_f_track.csv"
        )
        return df["x_track"].values
        # df, _ = testdata.bench_gen.gen_data(
        #     200, [], f_skew=1.1, f_card=50000, seed=17
        # )
        # return df["f"].values
    elif data_name == "bsynthq@4":
        df = pd.read_csv(
            "/Users/edwardgan/Documents/Projects/datasets/sketchstore_synth/bcube4_10M_q_track.csv"
        )
        return df["x_track"].values
        #
        # df, _ = testdata.bench_gen.gen_data(
        #     200, [], f_skew=1.1, f_card=50000, seed=17
        # )
        # return np.sort(df["q"].values)
    elif data_name == "insta":
        df = pd.read_csv(
            "/Users/edwardgan/Documents/Projects/datasets/instacart/tracked.csv"
        )
        return df["f"].values
    elif data_name == "msft_os_3M":
        df = pd.read_csv(
            "/Users/edwardgan/Documents/Projects/datasets/msft/mb-3M-os-track.csv"
        )
        return df["x_track"].values
    elif data_name == "msft_network_3M":
        df = pd.read_csv("/Users/edwardgan/Documents/Projects/datasets/msft/mb-3M-network-track.csv")
        return df["x_track"].values
    elif data_name == "msft_records_3M":
        df = pd.read_csv("/Users/edwardgan/Documents/Projects/datasets/msft/mb-3M-records-track.csv")
        return np.sort(df["x_track"].values)
    else:
        raise Exception("Invalid dataset name")


def get_workload_properties(df_raw: pd.DataFrame, dim_names, p: float):
    wp = storyboard.size_optimizer.WorkloadProperties(
        dim_names=dim_names,
        pred_weights=[p] * len(dim_names),
        pred_cardinalities=[df_raw[d].nunique() for d in dim_names],
        max_time_segments=1,
    )
    return wp


def get_dim_names(data_name) -> Tuple[Sequence[str], str]:
    if data_name == "synthf@2":
        return ["d{}".format(i) for i in range(2)], "f"
    elif data_name == "synthf@4":
        return ["d{}".format(i) for i in range(4)], "f"
    elif data_name == "bsynthf@4":
        return ["d{}".format(i) for i in range(4)], "f"
    elif data_name == "bsynthq@4":
        return ["d{}".format(i) for i in range(4)], "q"
    elif data_name == "insta":
        # 32 million rows
        # 10080 combinations
        return ["reordered", "order_dow", "order_hour_of_day", "add_to_cart_order"], "product_id"
    elif data_name.startswith("msft_os"):
        # 4000 combinations
        dims = ["TenantId", "AppInfo_Version", "UserInfo_TimeZone", "DeviceInfo_NetworkType"]
        metric = "DeviceInfo_OsBuild"
        return dims, metric
    elif data_name.startswith("msft_network"):
        dims = ["TenantId", "AppInfo_Version", "UserInfo_TimeZone", "DeviceInfo_NetworkType"]
        metric = "DeviceInfo_NetworkProvider"
        return dims, metric
    elif data_name.startswith("msft_records"):
        dims = ["TenantId", "AppInfo_Version", "UserInfo_TimeZone", "DeviceInfo_NetworkType"]
        metric = "records_received_count"
        return dims, metric
    else:
        raise Exception("Bad Data Name: {}".format(data_name))


# Dimension values must be consecutive integers
def get_dataset(data_name) -> pd.DataFrame:
    if data_name == "synthf@2":
        df, dim_names = testdata.bench_gen.gen_data(
            1_000_000,
            [(10, 1),
             (5, 1)],
            f_skew=1.1,
            f_card=10000,
            seed=0,
        )
        return df
    elif data_name == "synthf@4":
        df = pd.read_feather("/Users/edwardgan/Documents/Projects/datasets/sketchstore_synth/cube4_10M.feather")
        return df
    elif data_name == "bsynthf@4":
        df = pd.read_feather("/Users/edwardgan/Documents/Projects/datasets/sketchstore_synth/bcube4_10M.feather")
        return df
    elif data_name == "bsynthq@4":
        df = pd.read_feather("/Users/edwardgan/Documents/Projects/datasets/sketchstore_synth/bcube4_10M.feather")
        return df
    elif data_name == "insta":
        df = pd.read_feather("/Users/edwardgan/Documents/Projects/datasets/instacart/p_df.feather")
        return df
    elif (data_name.startswith("msft") and data_name.endswith("3M")):
        df = pd.read_csv("/Users/edwardgan/Documents/Projects/datasets/msft/mb-3M-cube.csv")
        return df
    else:
        raise Exception("Invalid dataset name: {}".format(data_name))


def get_sketch_gen(sketch_name: str, x_to_track: np.ndarray = None) -> sketch_gen.SketchGen:
    return linear_board.get_sketch_gen(sketch_name, x_to_track=x_to_track)


def get_p_from_split_strat(
        split_strategy: str
) -> float:
    p = int(split_strategy[split_strategy.rfind("@") + 1:]) / 100
    return p


def apply_split_strategy(
        split_strategy: str,
        df_total: pd.DataFrame,
        df_raw: pd.DataFrame,
        dim_names: Sequence[str],
) -> pd.DataFrame:
    if split_strategy.startswith("weighted"):
        p = get_p_from_split_strat(split_strategy)
        wp = get_workload_properties(df_raw, dim_names, p)
        df_sizes = storyboard.size_optimizer.get_a_weights_poiss(
            wp=wp,
            df_total=df_total
        )
        df_sizes = df_sizes**(1.0/3)
        return df_sizes
    elif split_strategy.startswith("sweighted"):
        p = get_p_from_split_strat(split_strategy)
        wp = get_workload_properties(df_raw, dim_names, p)
        df_sizes = storyboard.size_optimizer.get_a_weights_poiss(
            wp=wp,
            df_total=df_total
        )
        df_sizes = df_sizes ** (1.0/2)
        return df_sizes
    elif split_strategy.startswith("uniform"):
        df_sizes = storyboard.size_optimizer.get_a_weights_uniform(dim_names, df_total=df_total)
        return df_sizes
    elif split_strategy.startswith("prop"):
        return storyboard.size_optimizer.get_a_weights_prop(dim_names, df_total=df_total)
    else:
        raise Exception("Invalid split strategy")


def write_totals(df: pd.DataFrame, dims: Sequence[str], val_name: str, data_name: str):
    output_file_name = get_totals_name(data_name)
    os.makedirs(os.path.split(output_file_name)[0], exist_ok=True)
    df_total = (df.groupby(dims)[[val_name]]
           .count()
           .reset_index()
           .rename(columns={val_name: "total"})
           )
    df_total.to_csv(output_file_name, index=False)
    return df_total[list(dims) + ["total"]]


def run_test(
        data_name: str,
        split_strategy: str,
        board_size: int,
        sketch_name: str,
        bias_opt: bool=False,
):
    print("Generating Dataset")
    df_raw = get_dataset(data_name)
    dim_names, x_name = get_dim_names(data_name)
    df_total = write_totals(df_raw, dims=dim_names, val_name=x_name, data_name=data_name)
    x_to_track = get_tracked(data_name)
    sketch_gen = get_sketch_gen(sketch_name, x_to_track=x_to_track)
    board_constructor = board_gen.BoardGen(sketch_gen)

    print("Applying Split Strategy")
    df_sizes = apply_split_strategy(
        split_strategy=split_strategy,
        df_total=df_total,
        df_raw=df_raw,
        dim_names=dim_names
    )

    segment_dims = []
    segments = []
    sketch_sizes = []
    for df_key, df_seg in df_raw.groupby(dim_names):
        segment_dims.append(dict(zip(dim_names, df_key)))
        segments.append(df_seg[x_name].values)
        sketch_sizes.append(df_sizes.loc[df_key])
    sketch_sizes = np.array(sketch_sizes)
    sketch_sizes = storyboard.size_optimizer.scale_a_weights(
        sketch_sizes,
        board_size,
        min_amt=1,
    )

    sketch_biases = np.zeros(shape=len(segments))
    if bias_opt:
        x_counts = [np.unique(seg_values, return_counts=True)[1] for seg_values in segments]
        sketch_biases = bopt.opt_sequence(
            x_counts=x_counts,
            sizes=sketch_sizes,
        )

    bias_top = np.sort(sketch_biases)[::-1]
    print(sketch_sizes[:10])
    print(sketch_biases[:10])
    print("Top Biases: {}".format(bias_top[:10]))

    tags = []
    for i in range(len(segments)):
        cur_dict = dict()
        cur_dict.update(segment_dims[i])
        # cur_dict = {
        #     cur_dim: segment_dims[i][cur_dim]
        #     for cur_dim in dim_names
        # }
        cur_dict["size"] = sketch_sizes[i]
        if sketch_biases is not None:
            cur_dict["bias"] = sketch_biases[i]
        tags.append(cur_dict)

    df_board = board_constructor.generate(
        segments=segments,
        tags=tags,
    )
    df_board["dataset"] = data_name

    output_file_name = get_file_name(
        data_name=data_name,
        split_strategy=split_strategy,
        board_size=board_size,
        sketch_name=sketch_name,
        bias=bias_opt,
    )
    dir_name = os.path.split(output_file_name)[0]
    print("Output written to: {}".format(output_file_name))
    os.makedirs(dir_name, exist_ok=True)
    write_totals(df_raw, dim_names, val_name=x_name, data_name=data_name)
    board_constructor.serialize(df_board, output_file_name)

experiment_runs = [
    {
        "data_name": "bsynthq@4",
        "board_size": 50000,
        "quantile": True,
        "workload_p": .2,
        "sketch_types": [
            # ("q_top_values", "uniform", False),
            ("q_pps", "weighted@20", False),
            ("q_random_sample", "uniform", False),
            ("q_random_sample", "sweighted@20", False),
            ("q_random_sample", "prop", False),
            ("q_truncation", "uniform", False),
            ("kll", "uniform", False),

            ("q_pps", "uniform", False),
            ("q_random_sample", "weighted@20", False),
        ]
    },  # 0
    {
        "data_name": "bsynthf@4",
        "board_size": 50000,
        "quantile": False,
        "workload_p": .2,
        "sketch_types": [
            ("top_values", "uniform", False),
            ("pps", "weighted@20", True),
            ("random_sample", "uniform", False),
            ("random_sample", "sweighted@20", False),
            ("random_sample", "prop", False),
            ("truncation", "uniform", False),
            ("cms_min", "uniform", False),

            ("pps", "uniform", True),
            ("pps", "weighted@20", False),
            ("pps", "weighted@5", True),
            ("random_sample", "weighted@20", True),
        ]
    },  # 1
    {
        "data_name": "synthf@4",
        "board_size": 2048,
        "quantile": False,
        "workload_p": .2,
        "sketch_types": [
            # ("top_values", "uniform", False),
            ("pps", "weighted@20", True),
            # ("random_sample", "uniform", False),
            # ("random_sample", "prop", False),
            # ("truncation", "uniform", False),
            # ("cms_min", "uniform", False),

            ("pps", "uniform", True),
            # ("pps", "weighted@20", False),
            # ("random_sample", "weighted@20", True),
        ]
    },  # 2
    {
        "data_name": "insta",
        "board_size": 300_000,
        "quantile": False,
        "workload_p": .2,
        "sketch_types": [
            # ("top_values", "uniform", False),
            ("pps", "weighted@20", True),
            # ("random_sample", "uniform", False),
            # ("random_sample", "sweighted@20", False),
            # ("random_sample", "prop", False),
            # ("truncation", "uniform", False),
            # ("cms_min", "uniform", False),

            ("pps", "uniform", True),
            ("pps", "weighted@20", False),
            ("pps", "weighted@5", True),
            ("random_sample", "weighted@20", True),
        ]
    },  # 3
    {
        "data_name": "msft_os_3M",
        "board_size": 100_000,
        "quantile": False,
        "workload_p": .2,
        "sketch_types": [
            ("top_values", "uniform", False),
            ("pps", "weighted@20", True),
            ("random_sample", "uniform", False),
            ("random_sample", "sweighted@20", False),
            ("random_sample", "prop", False),
            ("truncation", "uniform", False),
            ("cms_min", "uniform", False),

            ("pps", "uniform", True),
            ("pps", "weighted@20", False),
            ("pps", "weighted@5", True),
            ("random_sample", "weighted@20", True),
        ]
    },  # 4
    {
        "data_name": "msft_records_3M",
        "board_size": 50000,
        "quantile": True,
        "workload_p": .2,
        "sketch_types": [
            ("q_top_values", "uniform", False),
            ("q_pps", "weighted@20", False),
            ("q_random_sample", "uniform", False),
            ("q_random_sample", "sweighted@20", False),
            ("q_random_sample", "prop", False),
            ("q_truncation", "uniform", False),
            ("kll", "uniform", False),

            ("q_pps", "uniform", False),
            ("q_random_sample", "weighted@20", False),
        ]
    },  # 5
    {
        "data_name": "msft_network_3M",
        "board_size": 100_000,
        "quantile": False,
        "workload_p": .2,
        "sketch_types": [
            ("top_values", "uniform", False),
            ("pps", "weighted@20", True),
            ("random_sample", "uniform", False),
            ("random_sample", "sweighted@20", False),
            ("random_sample", "prop", False),
            ("truncation", "uniform", False),
            ("cms_min", "uniform", False),

            # ("pps", "uniform", True),
            # ("pps", "weighted@20", False),
            # ("pps", "weighted@5", True),
            # ("random_sample", "weighted@20", True),
        ]
    },  # 6

]

def main():
    experiment_num = 6
    cur_experiment = experiment_runs[experiment_num]
    sketch_types = cur_experiment["sketch_types"]
    board_size = cur_experiment["board_size"]
    data_name = cur_experiment["data_name"]

    for cur_sketch, split_strategy, bias_opt in sketch_types:
        run_test(
            data_name=data_name,
            split_strategy=split_strategy,
            board_size=board_size,
            sketch_name=cur_sketch,
            bias_opt=bias_opt,
        )


if __name__ == "__main__":
    main()