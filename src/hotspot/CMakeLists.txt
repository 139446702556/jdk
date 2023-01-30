cmake_minimum_required(VERSION 3.16)
project(hotspot)

file(GLOB_RECURSE SOURCE_FILES "*.cpp" "*.hpp" "*.c" "*.h")

add_executable(hotspot ${SOURCE_FILES})