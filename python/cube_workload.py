import unittest

from storyboard.planner import FreqGroup
import storyboard.eval
import storyboard.planner
import numpy as np
import math
import testdata.bench_gen
import itertools
import pandas as pd


def run_small():
    df, dim_names = testdata.bench_gen.gen_data(
        1_000_000,
        [(10, 1),
         (5, 1)],
        f_skew=1.1,
        f_card=10000
    )
    n_dims = len(dim_names)

    dim_cards = df.nunique()[dim_names].values
    wps = [
        storyboard.planner.WorkloadProperties(
            pred_weights=[.1] * n_dims,
            pred_cardinalities=dim_cards,
            max_time_segments=1,
        ),
        storyboard.planner.WorkloadProperties(
            pred_weights=[1] * n_dims,
            pred_cardinalities=dim_cards,
            max_time_segments=1,
        ),
        storyboard.planner.WorkloadProperties(
            pred_weights=[0] * n_dims,
            pred_cardinalities=dim_cards,
            max_time_segments=1,
        ),
    ]
    fps = [
        storyboard.planner.FreqProcessor(
            total_size=1000,
            workload_prop=wp,
        ) for wp in wps
    ]
    fps += [
        storyboard.planner.FreqProcessor(
            total_size=1000,
            workload_prop=wp,
            opt_bias=False
        ) for wp in wps
    ]
    sbs = [
        fp.create_storyboard(
            df_input=df,
            dim_col_names=dim_names,
            val_col_name="f"
        )
        for fp in fps
    ]
    x_to_track = np.random.choice(df["f"], 400)

    for i,groups in enumerate(sbs):
        print("Workload: {}".format(i))
        # print(",".join([str(g) for g in groups]))
        eval = storyboard.eval.StoryboardVarianceEstimator(wps[0], 0)
        res = eval.calc_error(groups)
        print("calculated: {}".format(str(res)))
        res2 = eval.est_error(groups, n_trials=1000)
        print("estimated: {}".format(res2))

        sq = storyboard.eval.StoryboardQueryExecutor(groups)
        rq = storyboard.eval.RawQueryExecutor(df, dim_names=dim_names, val_name="f")
        res3 = eval.eval_error(sq=sq, rq=rq, x_to_track=x_to_track, n_trials=200)
        print("evaluated: {}".format(res3))


def run_test_bench():
    print("hello world")


def main():
    run_small()
    # run_test_bench()


if __name__ == "__main__":
    main()
