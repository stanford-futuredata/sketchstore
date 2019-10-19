from typing import Mapping, Any, Sequence

import numpy as np
import heapq
import math
from tqdm import tqdm
import scipy.optimize
import cvxpy as cvx


def n_bias(x_count: np.ndarray, bias: float):
    #     return np.sum(x_count[x_count >= bias])
    clipped = np.clip(x_count - bias, a_min=0, a_max=None)
    return np.sum(clipped)


def cost(bs, ns):
    return np.sum(bs) ** 2 + (1 / 4) * (np.sum(ns ** 2))


def opt_cvx(
        x_counts: Sequence[np.ndarray],
        sizes: Sequence[int],
        n_iter: int=10
) -> np.ndarray:
    n = len(sizes)
    Bs = cvx.Variable(n)
    constraints = [
        Bs >= 0
    ]
    term2 = 0
    for i in range(n):
        x_count = x_counts[i]
        size = sizes[i]
        term2 += cvx.square(cvx.sum(cvx.pos(x_count - Bs[i])) / size)
    o = cvx.Minimize(
        4 * cvx.square(cvx.sum(Bs)) + term2
    )
    prob = cvx.Problem(o, constraints)
    sol = prob.solve(solver=cvx.ECOS)
    b_values = Bs.value
    n_adj = np.zeros(n)
    for i in range(n):
        n_adj[i] = n_bias(x_counts[i], b_values[i]) / sizes[i]
    print("Cost: {}".format(cost(b_values, n_adj)))
    return np.round(b_values)



def n_deriv(x_count, bias, nraw=1, s=1):
    return nraw/s**2 * np.sum(x_count >= bias)

base = 2.0

def convert_to_bs(b_pows):
    bs = base**b_pows
    if isinstance(bs, np.ndarray):
        bs[bs < 1] = 0
    else:
        if bs < 1:
            bs = 0
    # bs = np.floor(2.0 ** b_pows)
    return bs


def opt_sequence_2(
        x_counts: Sequence[np.ndarray],
        sizes: Sequence[int],
        n_iter: int=10
) -> np.ndarray:
    n = len(x_counts)
    bs = np.zeros(n)
    n_adj = np.zeros(n)
    for i in range(n):
        n_adj[i] = n_bias(x_counts[i], bs[i]) / sizes[i]

    pq = []
    for s_idx in range(len(x_counts)):
        n_raw = n_adj[s_idx] * sizes[s_idx]
        heapq.heappush(
            pq,
            (-n_deriv(x_counts[s_idx], bs[s_idx], nraw=n_raw, s=sizes[s_idx]), s_idx)
        )

    print("Optimizing Bias")
    for cur_iter in tqdm(range(n_iter)):
        _, opt_idx = heapq.heappop(pq)
        # opt_idx = cur_iter % 3
        # print("bs:{}".format(bs))
        # print("ns:{}".format(n_adj))
        #         print("cost: {}".format(old_cost))

        old_cost = cost(bs, n_adj)
        def cost_b_fun(b):
            new_bs = bs.copy()
            new_adj = n_adj.copy()
            new_bs[opt_idx] = b
            new_adj[opt_idx] = n_bias(x_counts[opt_idx], b) / sizes[opt_idx]
            return cost(new_bs, new_adj)
        max_b = np.sum(x_counts[opt_idx])/sizes[opt_idx]
        bracket = None
        if bs[opt_idx] > 0:
            bracket = (0, bs[opt_idx], max_b)

        res = scipy.optimize.minimize_scalar(
            cost_b_fun,
            bracket=bracket,
            bounds=(0, max_b),
            tol=0.1
        )
        best_b = res.x
        print("best b: {}".format(best_b))
        new_cost = res.fun
        print("Old Cost: {}".format(old_cost))
        print("New Cost: {}".format(new_cost))
        # if (new_cost > old_cost*.98):
        #     break
        bs[opt_idx] = best_b
        n_adj[opt_idx] = n_bias(x_counts[opt_idx], bs[opt_idx]) / sizes[opt_idx]
        n_raw = n_adj[opt_idx] * sizes[opt_idx]
        heapq.heappush(
            pq,
            (-n_deriv(x_counts[opt_idx], bs[opt_idx], nraw=n_raw, s=sizes[opt_idx]), opt_idx)
        )
        print("Heap: {}".format(pq))

    return bs

def opt_sequence(
        x_counts: Sequence[np.ndarray],
        sizes: Sequence[int],
        n_iter: int=10
) -> np.ndarray:
    n = len(x_counts)
    b_pows = np.zeros(n) - 1
    bs = convert_to_bs(b_pows)
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
            bs[opt_idx] = convert_to_bs(cur_b_pow)
            # bs[opt_idx] = math.floor(2.0 ** cur_b_pow)
            n_adj[opt_idx] = n_bias(x_counts[opt_idx], bs[opt_idx]) / sizes[opt_idx]
            new_costs[shift_idx] = cost(bs, n_adj)
            # print("i:{},b:{},deltas:{}".format(opt_idx, cur_b_pow, new_costs - old_cost))


        best_shift_idx = np.argmin(new_costs)
        print("New Cost: {}".format(new_costs[best_shift_idx]))
        b_pows[opt_idx] += shifts[best_shift_idx]
        # bs[opt_idx] = math.floor(2.0 ** b_pows[opt_idx])
        bs[opt_idx] = convert_to_bs(b_pows[opt_idx])
        n_adj[opt_idx] = n_bias(x_counts[opt_idx], bs[opt_idx]) / sizes[opt_idx]
        if shifts[best_shift_idx] == 0:
            break
        heapq.heappush(
            pq,
            (-n_deriv(x_counts[opt_idx], bs[opt_idx]), opt_idx)
        )

    return bs