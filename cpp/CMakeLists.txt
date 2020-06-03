cmake_minimum_required(VERSION 3.0)
project(cpp)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_FLAGS_RELEASE "-O3")

include_directories(include)
add_executable(solver main.cpp LBFGSB.h LBFGS.h)