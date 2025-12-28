cmake_minimum_required(VERSION 3.22.1)

project("retrofun")

# Force optimizations even in debug builds because emulation is heavy
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -DNDEBUG -funroll-loops")

file(GLOB LAINES_SRC
    "laines/src/*.cpp"
    "laines/src/mappers/*.cpp"
    "laines/lib/*.cpp"
)

add_library(
        retrofun
        SHARED
        native-lib.cpp
        ${LAINES_SRC}
)

target_compile_definitions(retrofun PRIVATE
    # Any NES specific definitions if needed, currently none distinct from flags
)

include_directories(
    laines/src/include
    laines/src/include/mappers
    laines/lib/include
    laines/lib/include/boost
)

find_library(
        log-lib
        log)

target_link_libraries(
        retrofun
        ${log-lib})
