#pragma once
#include "common.hpp"

namespace GUI {
    void new_frame(u32* pixels);
    void new_samples(const s16* samples, size_t count);
    int get_joypad_state(int n);
    bool is_fast_forward();
}
