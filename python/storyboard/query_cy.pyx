# cython: language_level=3

cimport cython
cimport numpy as np
import numpy as np
import pandas as pd

import storyboard.dyadic_cy as dyadic_cy
import sketch.sketch_gen as board_sketch
import bounter


from libc.math cimport pow, ceil, floor, log

cdef class BoardSketch:
    def name(self) -> str:
        raise NotImplemented

    def estimate(self, x, bint rank = False) -> float:
        raise NotImplemented


cdef class DictSketch(BoardSketch):
    cpdef dict vals
    def __init__(self, dict vals):
        self.vals = vals

    def name(self) -> str:
        return "dict"

    def estimate(self, x, bint rank=False) -> float:
        if rank:
            x_sum = 0
            for k,v in self.vals.items():
                if k <= x:
                    x_sum += v
            return x_sum
        else:
            return self.vals.get(x, 0)


cdef class CMSSketch(BoardSketch):
    cdef object cms_obj
    def __init__(self, cms_obj):
        self.cms_obj = cms_obj

    def name(self) -> str:
        return "countmin"

    def estimate(self, x, bint rank=False) -> float:
        return self.cms_obj[str(x)]



cpdef calc_errors(
        true_sums,
        est_sums,
):
    errors = np.abs(np.array(true_sums) - np.array(est_sums))
    e_avg = np.mean(errors)
    e_rmse = np.sqrt(np.mean(errors**2))
    e_max = np.max(errors)
    return {
        "mean": e_avg,
        "rmse": e_rmse,
        "max": e_max,
    }


cpdef float query_linear_tot(
        df,
        int seg_start,
        int seg_end,
):
    mask = (df["seg_idx"] >= seg_start) & (df["seg_idx"] < seg_end)
    tot_sum = df["total"][mask].sum()
    return tot_sum


cpdef float query_cube_tot(
        df,
        dict query_values,
):
    mask = np.repeat(True, len(df))
    for cur_dim, cur_value in query_values.items():
        mask = mask & (df[cur_dim] == cur_value)
    tot_sum = df["total"][mask].sum()
    return tot_sum


cpdef list query_cube(
        df,
        dict query_values,
        np.ndarray x_to_track,
        bint quantile,
):
    mask = np.repeat(True, len(df))
    for cur_dim, cur_value in query_values.items():
        mask = mask & (df[cur_dim] == cur_value)
    summaries = df["data"][mask]

    cdef double[:] tot_results = np.zeros(shape=len(x_to_track))
    cdef BoardSketch cur_summary
    cdef int i
    for cur_summary in summaries:
        for i in range(len(x_to_track)):
            tot_results[i] += cur_summary.estimate(x_to_track[i], rank=quantile)
    return list(tot_results)


cpdef list query_linear(
        df,
        int seg_start,
        int seg_end,
        np.ndarray x_to_track,
        bint quantile,
        int dyadic_base,
):
    if dyadic_base > 0:
        return query_linear_dyadic(df, seg_start, seg_end, x_to_track, quantile, dyadic_base)
    mask = (df["seg_idx"] >= seg_start) & (df["seg_idx"] < seg_end)
    summaries = df["data"][mask]
    cdef double[:] tot_results = np.zeros(shape=len(x_to_track))
    cdef BoardSketch cur_summary
    cdef int i
    for cur_summary in summaries:
        for i in range(len(x_to_track)):
            tot_results[i] += cur_summary.estimate(x_to_track[i], rank=quantile)
    return list(tot_results)


cpdef list query_linear_dyadic(
        df,
        int seg_start,
        int seg_end,
        np.ndarray x_to_track,
        bint quantile,
        int dyadic_base,
):
    cdef double[:] tot_results = np.zeros(shape=len(x_to_track))
    dfi = df.set_index(["seg_idx", "level"])
    cdef list dyadic_segments = dyadic_cy.dyadic_breakdown(
        seg_start, seg_end, base=dyadic_base
    )
    cdef BoardSketch cur_summary
    cdef int i
    for dyadic_index in dyadic_segments:
        cur_summary = dfi["data"].loc[dyadic_index[0] - 1, dyadic_index[1]]
        for i in range(len(x_to_track)):
            tot_results[i] += cur_summary.estimate(x_to_track[i], rank=quantile)
    return list(tot_results)

