#pragma once
#include "emulator_core.hpp"

// Stub vazio - Genesis não suportado no iOS por enquanto
class GenesisCore : public EmulatorCore {
public:
    GenesisCore() {}
    bool loadGame(const std::string& path) override { return false; }
    void runFrame() override {}
    void reset() override {}
    void setController(int port, int state) override {}
    void getPixels(uint32_t* buffer, int width, int height) override {}
    void getAudioSamples(std::vector<int16_t>& buffer) override {}
};
