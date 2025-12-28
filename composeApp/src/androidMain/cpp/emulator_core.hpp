#pragma once
#include <vector>
#include <cstdint>
#include <string>

// Common interface for all emulators
class EmulatorCore {
public:
    virtual ~EmulatorCore() = default;

    // Load ROM from a file path
    virtual bool loadGame(const std::string& path) = 0;

    // Run a single frame (outputting video and audio)
    // Audio is typically handled by internal buffering, this just steps the CPU/PPU.
    virtual void runFrame() = 0;

    // Reset the console
    virtual void reset() = 0;

    // Set controller state (mask of buttons)
    virtual void setController(int port, int state) = 0;
    
    // Copy pixels to the provided buffer (ARGB 32-bit, size should be handled by caller based on spec)
    // For simplicity, we assume 256x240 for NES, but Genesis might be 320x224.
    // The JNI layer handles the resizing/buffer allocation or we pass a max-sized buffer.
    virtual void getPixels(uint32_t* buffer, int width, int height) = 0;
    
    // Retrieve audio samples
    virtual void getAudioSamples(std::vector<int16_t>& buffer) = 0;
};
