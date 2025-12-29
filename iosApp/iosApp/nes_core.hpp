#pragma once
#include "emulator_core.hpp"
#include "laines/src/include/apu.hpp"
#include "laines/src/include/cartridge.hpp"
#include "laines/src/include/cpu.hpp"
#include "laines/src/include/gui.hpp"

// We need to access the pixel destination that GUI::new_frame writes to
// Since LaiNES uses a static/global approach for GUI callbacks, we might need to 
// conform to that or adjust native-lib to route it correctly.
// For now, we will stick to the native-lib managing the global 'current_pixel_buffer'
// and NesCore simply calling CPU::run_frame().

class NesCore : public EmulatorCore {
public:
    NesCore() {}

    bool loadGame(const std::string& path) override {
        try {
            APU::init(); 
            Cartridge::load(path.c_str());
            return true;
        } catch (...) {
            return false;
        }
    }

    void runFrame() override {
        CPU::run_frame();
    }

    void reset() override {
        CPU::power();
    }

    void setController(int port, int state) override {
        // In LaiNES, this is handled via a global variable or getter.
        // We actually need to update the variable that GUI::get_joypad_state reads.
        // This variable is currently in generic native-lib.cpp 'joypad_state_p1'.
        // We will expose a setter in GUI namespace or a global setter.
        // For now, we'll leave it as a no-op here and let native-lib handle the global update
        // BEFORE calling runFrame.
    }

    void getPixels(uint32_t* buffer, int width, int height) override {
        // LaiNES writes directly via GUI::new_frame callback.
        // So this method might be redundant if we keep the callback architecture.
        // However, for clean design, ideally NesCore would manage its own buffer.
        // Given existing LaiNES code, we won't rewrite LaiNES internals yet.
    }

    void getAudioSamples(std::vector<int16_t>& buffer) override {
        // Similar to pixels, LaiNES pushes to GUI::new_samples.
    }
};
