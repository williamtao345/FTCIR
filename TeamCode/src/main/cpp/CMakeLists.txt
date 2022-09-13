cmake_minimum_required(VERSION 3.4.1)
project(mouse_events)
add_library(${PROJECT_NAME} SHARED lib.cpp)

target_compile_features(${PROJECT_NAME} PRIVATE cxx_std_17)
target_compile_options(${PROJECT_NAME} PRIVATE -fno-exceptions -fno-rtti -fdiagnostics-color=always -Wall -Wextra -Werror -O3 -mfpu=neon -flimit-debug-info -UNDEBUG)
target_link_libraries(${PROJECT_NAME} PRIVATE -Wl,--gc-sections -llog)
