import numpy as np
import pandas as pd


def gen_zipf_weights(s, a=1):
    ws = 1.0/(np.arange(1,s+1)**a)
    return ws / np.sum(ws)


def gen_data(n_rows, dim_params, f_skew=1.2, f_card=10, seed=None):
    df = pd.DataFrame()
    df["t"] = np.arange(n_rows)
    r = np.random.RandomState(seed=seed)
    df["q"] = r.normal(size=n_rows).astype("float32")
    f_vals = r.zipf(a=f_skew, size=10*n_rows)
    df["f"] = f_vals[f_vals < f_card][:n_rows]
    dim_names = []
    for i in range(len(dim_params)):
        dim_size, dim_skew = dim_params[i]
        w = gen_zipf_weights(dim_size, dim_skew)
        dname = "d{}".format(i)
        dim_names.append(dname)
        df[dname] = r.choice(
            np.arange(dim_size),
            size=n_rows,
            replace=True,
            p=w
        )
    return df, dim_names