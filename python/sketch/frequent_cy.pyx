# cython: language_level=3

import numpy as np
cimport cython

@cython.boundscheck(False)
@cython.wraparound(False)
cpdef find_t(long [:] counts, int s):
    cdef int sum_rest = sum(counts)
    cdef long cur_t = sum_rest // s
    cdef bint found_tail = False
    cdef int tail_idx = 0
    for tail_idx in range(len(counts)):
        if tail_idx > 0:
            sum_rest -= counts[tail_idx-1]
        cur_t = sum_rest // (s-tail_idx)
        if counts[tail_idx] < cur_t:
            found_tail = True
            break
    if not found_tail:
        cur_t = 0
        tail_idx = len(counts)
    return cur_t, tail_idx


@cython.boundscheck(False)
@cython.wraparound(False)
cpdef sum(long [:] f):
    cdef long acc = 0;
    cdef int n = f.shape[0]
    for i in range(n):
        acc += f[i]
    return acc
