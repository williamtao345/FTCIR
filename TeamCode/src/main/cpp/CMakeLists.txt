cmake_minimum_required(VERSION 3.4.1)
project(mouse_events)
add_library(${PROJECT_NAME} SHARED lib.cpp)

set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
target_compile_features(${PROJECT_NAME} PRIVATE cxx_std_17)
target_compile_options(${PROJECT_NAME} PRIVATE -fno-exceptions -fno-rtti -fdiagnostics-color=always -Wall -Wextra -Werror -O3)

# execute_process(COMMAND ln -sf "${CMAKE_BINARY_DIR}/compile_commands.json" "${CMAKE_CURRENT_LIST_DIR}/../../../..")
