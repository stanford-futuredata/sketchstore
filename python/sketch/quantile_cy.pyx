# cython: language_level=3

import numpy as np
cimport numpy as np
import cython
cimport cython


@cython.boundscheck(False)
cpdef fast_delta(double[:] xvals, double[:] saved, double[:] saved_weight):
    cdef int n = len(xvals)
    cdef int n_s = len(saved)
    cdef double[::1] delta = np.zeros(shape=n, dtype=float)
    cdef int s_idx = 0
    cdef int i
    cdef double cum_saved = 0.0
    for i in range(n):
        while s_idx < n_s and saved[s_idx] <= xvals[i]:
            cum_saved += saved_weight[s_idx]
            s_idx += 1
        delta[i] = float(i)+1.0-cum_saved
    return delta

cpdef get_deltas_2(dict true_weight, dict est_weight):
    cdef int n = len(true_weight)

    cdef double[::1] xvals = np.sort(np.fromiter(true_weight.keys(), dtype=np.float_))
    cdef double[::1] delta = np.zeros(shape=n, dtype=np.float_)

    cdef int i
    cdef double cur_x
    cdef double prev_delta
    for i in range(n):
        cur_x = xvals[i]
        prev_delta = 0
        if i > 0:
            prev_delta = delta[i-1]
        delta[i] = prev_delta + true_weight[cur_x]
        if cur_x in est_weight:
            delta[i] -= est_weight.get(cur_x, 0)

    return np.asarray(xvals, dtype=np.float_), np.asarray(delta, dtype=np.float_)

