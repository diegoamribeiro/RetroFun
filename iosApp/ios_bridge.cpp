#include "ios_bridge.h"
#include "emulator_context.hpp"

// Implementation of C wrappers

void ios_init(int consoleType, const char* romPath) {
    // For iOS, we assume the file is already at romPath (bundle or Documents)
    // We pass nullptr for data so it doesn't try to write it.
    EmulatorContext::getInstance().init(consoleType, romPath, nullptr, 0);
}

void ios_run_frame(int* pixels, int width, int height) {
    int w = width;
    int h = height;
    // Cast int* to uint32_t* - safe as they are same size
    EmulatorContext::getInstance().runFrame((uint32_t*)pixels, w, h);
}

int ios_get_audio(short* buffer, int maxCount) {
    return EmulatorContext::getInstance().getAudioSamples(buffer, maxCount);
}

void ios_set_input(int up, int down, int left, int right, int a, int b, int start, int select) {
    int state = 0;
    if (a)      state |= (1 << 0);
    if (b)      state |= (1 << 1);
    if (select) state |= (1 << 2);
    if (start)  state |= (1 << 3);
    if (up)     state |= (1 << 4);
    if (down)   state |= (1 << 5);
    if (left)   state |= (1 << 6);
    if (right)  state |= (1 << 7);

    EmulatorContext::getInstance().setControllerState(0, state);
}

void ios_reset() {
    EmulatorContext::getInstance().reset();
}
