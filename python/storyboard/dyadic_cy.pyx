# cython: language_level=3

cimport cython

from libc.math cimport pow, ceil, floor, log

cdef fit_segments(int start_idx, int end_idx, int power, int base):
    cdef int segment_length = int(pow(base,power))
    if end_idx - start_idx < segment_length:
        return []
    cdef int start_factor = int(ceil(start_idx*1.0/segment_length))
    cdef int end_factor = int(floor(end_idx*1.0/segment_length))
    if end_factor == start_factor:
        return []
    return [
        ((fi+1)*segment_length, power)
        for fi in range(start_factor, end_factor)
    ]

cdef cdyadic_breakdown(int start_idx, int end_idx, int base, int max_power):
    if start_idx >= end_idx:
        return []
    if end_idx - start_idx == 1:
        return [(end_idx, 0)]
    cdef list largest_segments = []
    cdef int cur_power = max_power
    while True:
        largest_segments = fit_segments(start_idx, end_idx, cur_power, base)
        if len(largest_segments) > 0:
            break
        else:
            cur_power -= 1
    return (
        largest_segments
        + cdyadic_breakdown(
            start_idx,
            largest_segments[0][0] - int(pow(base, largest_segments[0][1])),
            base,
            cur_power-1)
        + cdyadic_breakdown(
            largest_segments[-1][0],
            end_idx,
            base,
            cur_power-1
        )
    )

cpdef dyadic_breakdown(int start_idx, int end_idx, int base=2, int max_power=-1):
    if max_power < 0:
        max_power = int(log(end_idx - start_idx)/log(base))
    return cdyadic_breakdown(start_idx, end_idx, base, max_power)