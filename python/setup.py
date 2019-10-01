from distutils.core import setup
from distutils.extension import Extension
from Cython.Build import cythonize

import numpy

extensions = [
    Extension(
        "sketch.frequent_cy", ["sketch/frequent_cy.pyx"],
        include_dirs=[numpy.get_include()],
        libraries=None,
        library_dirs=None),
    Extension(
        "sketch.quantile_cy", ["sketch/quantile_cy.pyx"],
        include_dirs=[numpy.get_include()],
        libraries=None,
        library_dirs=None),
    Extension(
        "storyboard.dyadic_cy", ["storyboard/dyadic_cy.pyx"],
        include_dirs=[numpy.get_include()],
        libraries=None,
        library_dirs=None),
    Extension(
        "storyboard.planner_cy", ["storyboard/planner_cy.pyx"],
        include_dirs=[numpy.get_include()],
        libraries=None,
        library_dirs=None),
]
setup(
    name="Cython Test App",
    ext_modules=cythonize(extensions),
)
