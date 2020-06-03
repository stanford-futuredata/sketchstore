cmake_minimum_required(VERSION 3.15)
project(cpp)

set(CMAKE_CXX_STANDARD 14)

include_directories(include)
add_executable(solver main.cpp LBFGSB.h LBFGS.h)