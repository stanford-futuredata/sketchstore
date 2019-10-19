from typing import Mapping, Any, Sequence

import numpy as np
import heapq
import math
from tqdm import tqdm
import scipy.optimize
import pandas as pd


def stack_x(x_counts: Sequence[np.ndarray]):
    df = pd.DataFrame(x_counts)
    return df.fillna(0).values


def adjust_xs(xs: np.ndarray, sizes: np.ndarray):
    x_totals = np.sum(xs, axis=1)
    hs = x_totals/sizes
    big_mask = (xs > hs.reshape(-1,1))
    xs_adj = xs.copy()
    xs_adj[big_mask] = 0
    num_hh = np.sum(big_mask, axis=1)
    adj_sizes = sizes - num_hh
    return xs_adj, adj_sizes


def n_bias(x_counts: np.ndarray, bias: np.ndarray):
    clipped = np.clip(x_counts.T - bias, a_min=0, a_max=None)
    return np.sum(clipped, axis=0)


def u_bias(x_counts, bias):
    return np.sum(x_counts.T > bias, axis=0)


def cost(x_counts, sizes, bias):
    n_adj = n_bias(x_counts, bias) / sizes
    return np.sum(bias)**2 + np.sum(n_adj*n_adj)/4


def deriv(x_counts, sizes, bias):
    n_adj = n_bias(x_counts, bias) / sizes
    u_adj = u_bias(x_counts, bias)
    ones = np.ones(len(sizes))
    return 2*np.sum(bias)*ones + n_adj / sizes * (-u_adj)/2


def opt_sequence(
        x_counts: Sequence[np.ndarray],
        sizes: np.ndarray,
        n_iter: int=10
) -> np.ndarray:
    n = len(x_counts)
    x_array = stack_x(x_counts)
    # xs_adj, sizes_adj = adjust_xs(x_array, sizes)
    xs_adj, sizes_adj = x_array, sizes

    def fun_combined(b):
        # np.maximum()
        # clipped = np.clip(x_array.T - b, a_min=0, a_max=None)
        clipped = np.maximum(xs_adj.T - b, 0)
        n_bias = np.sum(clipped, axis=0)
        # u_adj = np.sum(x_array.T > b, axis=0)
        u_adj = np.count_nonzero(clipped, axis=0)
        n_adj = n_bias/sizes_adj
        bsum = np.sum(b)
        cost = bsum**2 + np.sum(n_adj*n_adj)/4
        ones = np.ones(n)
        deriv = 2*bsum*ones + n_adj/sizes_adj*(-u_adj)/2
        return cost, deriv

    print("Optimizing")
    res = scipy.optimize.minimize(
        fun=fun_combined,
        x0=np.zeros(n),
        jac=True,
        bounds=scipy.optimize.Bounds(0, np.inf),
        options={
            "maxiter": 40,
            "disp": True,
        }
        # tol=.5,
    )
    # print("Function: {}".format(res.fun))
    return np.round(res.x)
