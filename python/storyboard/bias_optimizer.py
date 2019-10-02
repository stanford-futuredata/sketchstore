from typing import Mapping, Any, Sequence

import numpy as np
import heapq
import math
from tqdm import tqdm


def n_bias(x_count: np.ndarray, bias: float):
    #     return np.sum(x_count[x_count >= bias])
    clipped = np.clip(x_count - bias, a_min=0, a_max=None)
    return np.sum(clipped)


def cost(bs, ns):
    return np.sum(bs) ** 2 + (1 / 4) * (np.sum(ns ** 2))


def n_deriv(x_count, bias):
    return np.sum(x_count >= bias)


def opt_sequence(
        x_counts: Sequence[np.ndarray],
        sizes: Sequence[int],
        n_iter: int=10
) -> np.ndarray:
    n = len(x_counts)
    b_pows = np.zeros(n) - 1
    bs = np.floor(2.0 ** b_pows)
    n_adj = np.zeros(n)
    for i in range(n):
        n_adj[i] = n_bias(x_counts[i], bs[i]) / sizes[i]

    pq = []
    for s_idx in range(len(x_counts)):
        heapq.heappush(
            pq,
            (-n_deriv(x_counts[s_idx], bs[s_idx]), s_idx)
        )

    shifts = np.array([-1, 0, 1])
    print("Optimizing Bias")
    for cur_iter in tqdm(range(n_iter)):
        _, opt_idx = heapq.heappop(pq)
        # print("bs:{}".format(bs))
        # print("ns:{}".format(n_adj))
        #         print("cost: {}".format(old_cost))

        new_costs = np.zeros(3)
        for shift_idx, cur_shift in enumerate(shifts):
            cur_b_pow = b_pows[opt_idx] + cur_shift
            bs[opt_idx] = math.floor(2.0 ** cur_b_pow)
            n_adj[opt_idx] = n_bias(x_counts[opt_idx], bs[opt_idx]) / sizes[opt_idx]
            new_costs[shift_idx] = cost(bs, n_adj)
            # print("i:{},b:{},deltas:{}".format(opt_idx, cur_b_pow, new_costs - old_cost))

        best_shift_idx = np.argmin(new_costs)
        b_pows[opt_idx] += shifts[best_shift_idx]
        bs[opt_idx] = math.floor(2.0 ** b_pows[opt_idx])
        n_adj[opt_idx] = n_bias(x_counts[opt_idx], bs[opt_idx]) / sizes[opt_idx]
        if shifts[best_shift_idx] == 0:
            break
        heapq.heappush(
            pq,
            (-n_deriv(x_counts[opt_idx], bs[opt_idx]), opt_idx)
        )

    return bs