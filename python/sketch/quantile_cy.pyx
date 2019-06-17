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
        cur_x = xvals[i]
        while s_idx < n_s and saved[s_idx] <= cur_x:
            cum_saved += saved_weight[s_idx]
            s_idx += 1
        delta[i] = float(i)+1.0-cum_saved
    return np.array(delta)
