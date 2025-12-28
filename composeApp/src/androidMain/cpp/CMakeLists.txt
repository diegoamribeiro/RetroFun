cmake_minimum_required(VERSION 3.22.1)

project("retrofun")

# Force optimizations even in debug builds because emulation is heavy
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -DNDEBUG -funroll-loops")

file(GLOB LAINES_SRC
    "laines/src/*.cpp"
    "laines/src/mappers/*.cpp"
    "laines/lib/*.cpp"
)

# Genesis Plus GX Sources
file(GLOB_RECURSE GENPLUS_SRC
    "genesis_plus_gx/core/*.c"
)
# Exclude libchdr as it pulls in zlib/lzma deps we don't need for basic cart emulation
file(GLOB_RECURSE CHDR_SRC "genesis_plus_gx/core/cd_hw/libchdr/*.c")
# Exclude optional cartridge hardware that requires extra deps
file(GLOB YX5200_SRC "genesis_plus_gx/core/cart_hw/yx5200.c")

list(REMOVE_ITEM GENPLUS_SRC ${CHDR_SRC} ${YX5200_SRC})

add_library(
        retrofun
        SHARED
        native-lib.cpp
        ${LAINES_SRC}
        ${GENPLUS_SRC}
)

target_compile_definitions(retrofun PRIVATE
    # LaiNES
    # Genesis Plus GX
    -DUSE_32BPP_RENDERING
    -DLSB_FIRST
    -DUSE_DYNAMIC_ALLOC
)

include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}
    laines/src/include
    laines/src/include/mappers
    laines/lib/include
    laines/lib/include/boost
    
    # Genesis Plus GX includes
    genesis_plus_gx/core
    genesis_plus_gx/core/m68k
    genesis_plus_gx/core/z80
    genesis_plus_gx/core/sound
    genesis_plus_gx/core/input_hw
    genesis_plus_gx/core/cart_hw
    genesis_plus_gx/core/cart_hw/svp
    genesis_plus_gx/core/cd_hw
    genesis_plus_gx/core/ntsc
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

find_library(
        z-lib
        z)

target_link_libraries(
        retrofun
        ${log-lib}
        ${z-lib})
