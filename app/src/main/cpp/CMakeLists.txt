cmake_minimum_required(VERSION 3.22.1)
project("bloom-llama")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Prevent llama.cpp from building unnecessary examples/tests
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)

include(FetchContent)

# Download llama.cpp from GitHub, pinned to the exact commit this project is
# verified against — reproducible on any machine (incl. store build servers) 💗
FetchContent_Declare(
    llama_cpp
    GIT_REPOSITORY https://github.com/ggerganov/llama.cpp.git
    GIT_TAG 61141f1487e63d9b22aec193131253bb2ea0800c
)
FetchContent_MakeAvailable(llama_cpp)

# Your JNI Bridge
add_library(bloom-llama SHARED
    llama_bridge.cpp
)

# Link against the 'llama' library created by the llama.cpp project
target_link_libraries(bloom-llama
    llama
    android
    log
)
