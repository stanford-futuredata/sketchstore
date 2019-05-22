# cython: language_level=3

cimport cython

@cython.boundscheck(False)
@cython.wraparound(False)
cpdef find_t(double [:] counts, int s):
    cdef double sum_rest = sum_d(counts)
    cdef double cur_t = sum_rest / s
    cdef bint found_tail = False
    cdef int tail_idx = 0
    for tail_idx in range(len(counts)):
        if tail_idx > 0:
            sum_rest -= counts[tail_idx-1]
        cur_t = sum_rest / (s-tail_idx)
        if counts[tail_idx] < cur_t:
            found_tail = True
            break
    if not found_tail:
        cur_t = 0
        tail_idx = len(counts)
    return cur_t, tail_idx


@cython.boundscheck(False)
@cython.wraparound(False)
cpdef sum_d(double [:] f):
    cdef double acc = 0;
    cdef int n = f.shape[0]
    for i in range(n):
        acc += f[i]
    return acc
