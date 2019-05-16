import unittest

import numpy as np
import math

import storyboard.planner
import testdata.bench_gen


def main():
    print("Generating Data")
    df = testdata.bench_gen.gen_data(
        2_000_000,
        [(2,0), (10, 1), (10, 1), (10, 1)],
        f_skew=1.2,
        f_card=100_000_000
    )
    wp = storyboard.planner.WorkloadProperties(
        pred_weights=[.3, .3, .3],
        max_time_segments=1,
    )
    fp = storyboard.planner.FreqProcessor(
        total_size=2000,
        workload_prop=wp,
    )
    print("Creating Storyboard")
    groups = fp.create_storyboard(
        df_input=df,
        dim_col_names=["d0", "d1", "d2"],
        val_col_name="f"
    )
    print(groups)


if __name__ == "__main__":
    main()