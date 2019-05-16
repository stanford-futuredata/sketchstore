# cython: language_level=3

cimport cython
cimport numpy as np
import numpy as np


cdef class RawGroupL:
    cpdef readonly list dims
    cpdef readonly long[:] vals
    def __cinit__(self, list dims, long[:] vals):
        self.dims = dims
        self.vals = vals

    def __str__(self):
        return "{}->{}".format(self.dims, str(np.asarray(self.vals)))

    def __repr__(self):
        return self.__str__()


cpdef group_vals(list dim_vals, long[:] val_col):
    cdef int n_dims = len(dim_vals)
    cdef int n_rows = len(val_col)

    if n_dims == 0:
        return [RawGroupL([], val_col)]

    cdef dict group_vals = {}
    cdef int row_idx
    for row_idx in range(n_rows):
        row_key = tuple((dim_col[row_idx] for dim_col in dim_vals))
        if row_key not in group_vals:
            group_vals[row_key] = list()
        group_vals[row_key].append(val_col[row_idx])

    cdef list group_list = []
    for cur_key, cur_vals in group_vals.items():
        group_list.append(RawGroupL(list(cur_key), cur_vals))
    return group_list