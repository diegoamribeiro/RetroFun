#include "emulator_context.hpp"
#include "nes_core.hpp"
#include "genesis_core.hpp"
#include <android/log.h>
#include <cstring>

#define TAG "RetroFun-Context"
// Macro to allow logging on Android, or no-op/printf on iOS?
// For cross-platform, we might need #ifdef ANDROID
#ifdef __ANDROID__
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#else
#define LOGI(...) printf(__VA_ARGS__); printf("\n")
#define LOGE(...) printf(__VA_ARGS__); printf("\n")
#endif

// ==========================================================
// Global Glue for Cores (GUI Namespace)
// ==========================================================
u32* context_pixel_buffer = nullptr; // Pointer to the buffer currently being rendered to
int context_joypad_state = 0;

namespace GUI {
    void new_frame(u32* pixels) {
        if (context_pixel_buffer) {
            for (int i = 0; i < 256 * 240; i++) {
                context_pixel_buffer[i] = pixels[i] | 0xFF000000;
            }
        }
    }

    void new_samples(const s16* samples, size_t count) {
        EmulatorContext::getInstance().pushAudio(samples, count);
    }

    int get_joypad_state(int n) {
        if (n == 0) return context_joypad_state;
        return 0;
    }

    bool is_fast_forward() { return false; }
}

// ==========================================================
// EmulatorContext Implementation
// ==========================================================

EmulatorContext& EmulatorContext::getInstance() {
    static EmulatorContext instance;
    return instance;
}

EmulatorContext::EmulatorContext() : currentCore(nullptr), joypadStateP1(0), gameLoaded(false) {}

EmulatorContext::~EmulatorContext() {
    if (currentCore) delete currentCore;
}

void EmulatorContext::pushAudio(const int16_t* samples, size_t count) {
    audioBuffer.insert(audioBuffer.end(), samples, samples + count);
}

void EmulatorContext::init(int consoleType, const char* romPath, const uint8_t* romData, size_t romSize) {
    audioBuffer.clear();
    
    if (currentCore) {
        delete currentCore;
        currentCore = nullptr;
    }
    gameLoaded = false;
    
    // Try to open the file to write romData if provided (for JNI case where we get bytes)
    // Or if romPath is just a destination.
    // If romPath exists and we have data, write it.
    if (romData && romSize > 0 && romPath) {
        FILE* f = fopen(romPath, "wb");
        if (f) {
            fwrite(romData, 1, romSize, f);
            fclose(f);
            LOGI("Wrote ROM to %s", romPath);
        } else {
             LOGE("Failed to write ROM to %s", romPath);
        }
    }

    if (consoleType == 0) {
        LOGI("Initializing NES Core");
        currentCore = new NesCore();
    } else {
        LOGI("Initializing Genesis Core");
        currentCore = new GenesisCore();
    }

    if (currentCore->loadGame(romPath)) {
        LOGI("Core loaded game successfully.");
        gameLoaded = true;
    } else {
        LOGE("Core failed to load game.");
        gameLoaded = false;
    }
}

void EmulatorContext::runFrame(uint32_t* pixels, int& width, int& height) {
    if (!currentCore || !gameLoaded) return;

    context_pixel_buffer = pixels; // Set global pointer so GUI::new_frame can write to it
    
    try {
        currentCore->runFrame();
    } catch (...) {
        LOGE("Core runFrame exception");
    }
    
    context_pixel_buffer = nullptr;
    
    // Hardcoded for now, Genesis might change this
    width = 256;
    height = 240;
}

int EmulatorContext::getAudioSamples(int16_t* buffer, int maxCount) {
    size_t count = audioBuffer.size();
    if (count == 0) return 0;

    if (count > maxCount) count = maxCount;
    
    std::memcpy(buffer, audioBuffer.data(), count * sizeof(int16_t));
    audioBuffer.erase(audioBuffer.begin(), audioBuffer.begin() + count); // Inefficient for vector but okay for prototype
    
    return count;
}

void EmulatorContext::setControllerState(int playerId, int state) {
    joypadStateP1 = state;
    context_joypad_state = state;
    if (currentCore) {
        currentCore->setController(playerId, state);
    }
}

void EmulatorContext::reset() {
    if (currentCore) currentCore->reset();
}
