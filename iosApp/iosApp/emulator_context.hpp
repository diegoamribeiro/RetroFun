#ifndef EMULATOR_CONTEXT_HPP
#define EMULATOR_CONTEXT_HPP

#include <vector>
#include <string>
#include <cstdint>
#include "emulator_core.hpp"

class EmulatorContext {
public:
    static EmulatorContext& getInstance();

    void init(int consoleType, const char* romPath, const uint8_t* romData, size_t romSize);
    void runFrame(uint32_t* pixels, int& width, int& height);
    int getAudioSamples(int16_t* buffer, int maxCount);
    void setControllerState(int playerId, int state);
    void reset();
    void pushAudio(const int16_t* samples, size_t count);
    
private:
    EmulatorContext();
    ~EmulatorContext();

    EmulatorCore* currentCore;
    std::vector<int16_t> audioBuffer;
    int joypadStateP1;
    bool gameLoaded;
};

#endif // EMULATOR_CONTEXT_HPP
