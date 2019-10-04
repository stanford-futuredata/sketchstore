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
import storyboard.bias_optimizer


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


# Dimension values must be consecutive integers
def get_dataset(data_name) -> Tuple[pd.DataFrame, Sequence[str], str]:
    if data_name == "synthf@2":
        df, dim_names = testdata.bench_gen.gen_data(
            1_000_000,
            [(10, 1),
             (5, 1)],
            f_skew=1.1,
            f_card=10000,
            seed=0,
        )
        return df, dim_names, "f"
    elif data_name == "synthf@4":
        df, dim_names = testdata.bench_gen.gen_data(
            10_000_000,
            [(10, 1),
             (5, 1),
             (2, 1),
             (2, 1),
             ],
            f_skew=1.1,
            f_card=10000,
            seed=0,
        )
        return df, dim_names, "f"
    else:
        raise Exception("Invalid dataset name")


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
        return df_sizes
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
    df_raw, dim_names, x_name = get_dataset(data_name)
    df_total = write_totals(df_raw, dims=dim_names, val_name=x_name, data_name=data_name)
    x_to_track = get_tracked(data_name)
    sketch_gen = get_sketch_gen(sketch_name, x_to_track=x_to_track)
    board_constructor = board_gen.BoardGen(sketch_gen)

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
    sketch_sizes = storyboard.size_optimizer.scale_a_weights(sketch_sizes, board_size)

    sketch_biases = np.zeros(shape=len(segments))
    if bias_opt:
        x_counts = [np.unique(seg_values, return_counts=True)[1] for seg_values in segments]
        sketch_biases = storyboard.bias_optimizer.opt_sequence(
            x_counts=x_counts,
            sizes=sketch_sizes,
            n_iter=2000,
        )

    print(sketch_sizes)
    print(sketch_biases)

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


def main():
    sketch_names = [
        "top_values",
        "random_sample",
        "truncation",
        "cms_min",
        "pps",
    ]
    run_test(
        data_name="synthf@2",
        split_strategy="weighted@10",
        board_size=2048,
        sketch_name="top_values",
        bias_opt=False,
    )


if __name__ == "__main__":
    main()