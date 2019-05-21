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
