#pragma once
#include "emulator_core.hpp"
#include <cstring>
#include <vector>
#include <android/log.h>

extern "C" {
#include "shared.h"
#include "loadrom.h"
#include "io_ctrl.h" 
#include "input_hw/input.h"
}

// Global hooks required by GenPlusGX if not found?
// Usually shared.h pulls them in.

#ifndef LOGI
#define TAG_GEN "GenesisCore"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG_GEN, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG_GEN, __VA_ARGS__)
#endif

class GenesisCore : public EmulatorCore {
private:
    std::vector<uint32_t> video_buffer;
    std::vector<int16_t> audio_temp_buffer;

public:
    GenesisCore() {
        // Allocate global 'ext' (Cartridge hardware state) if dynamic alloc is used
        // This MUST be done before system_init() because gen_init/sms_cart_init access it.
        #ifdef USE_DYNAMIC_ALLOC
        if (!ext) {
            ext = (external_t*)calloc(1, sizeof(external_t));
            LOGI("GenesisCore: Allocated ext memory (%zu bytes)", sizeof(external_t));
        }
        #endif

        // Initialize Genesis Plus GX
        // Set up bitmap
        memset(&bitmap, 0, sizeof(bitmap));
        bitmap.width = 320;
        bitmap.height = 240; // Max vertical resolution
        bitmap.pitch = 320 * 4; // 32bpp
        video_buffer.resize(320 * 240); 
        bitmap.data = (uint8_t*)video_buffer.data();
        
        // Input config
        input.system[0] = SYSTEM_GAMEPAD; // MD gamepad
        input.system[1] = SYSTEM_GAMEPAD;
        
        // Config defaults
        config.hq_fm = 1;
        config.psg_preamp = 150;
        config.fm_preamp = 100;
        config.region_detect = 0; // Auto
        config.vdp_mode = 0; // Auto
        config.master_clock = 0; // Auto
        config.addr_error = 1;
        config.bios = 0;
        
        // Audio
        audio_init(44100, 60.0);
        
        // System
        system_init();
        
        // Configure Viewport defaults
        bitmap.viewport.x = 0;
        bitmap.viewport.y = 0;
        bitmap.viewport.w = 320;
        bitmap.viewport.h = 240;
        
        audio_temp_buffer.resize(4096 * 2); 
    }
    
    ~GenesisCore() {
        #ifdef USE_DYNAMIC_ALLOC
        if (ext) {
            free(ext);
            ext = nullptr;
            LOGI("GenesisCore: Freed ext memory");
        }
        #endif
    }

    bool loadGame(const std::string& path) override {
        // GenPlusGX load_rom expects a char*
        // It returns 1 on success
        LOGI("GenesisCore: Attempting to load ROM from %s", path.c_str());
        if (load_rom((char*)path.c_str())) {
            // CRITICAL: re-initialize system to update memory maps and hardware config
            // based on the loaded ROM (detected console type, size, etc.)
            system_init();
            
            system_reset();
            LOGI("GenesisCore: load_rom returned success. ROM loaded.");
            return true;
        }
        LOGE("GenesisCore: load_rom returned failure!");
        return false;
    }

    void runFrame() override {
        // Run one frame
        system_frame_gen(0);
        
        // Debug Viewport
        // LOGI("GenesisCore: Viewport: %d x %d", bitmap.viewport.w, bitmap.viewport.h);
        
        // Audio processing
        // audio_update returns number of FRAMES (samples per channel)
        // Since GenPlusGX produces Stereo (L+R), the actual number of samples is Frames * 2
        int frames = audio_update(audio_temp_buffer.data());
        if (frames > 0) {
            GUI::new_samples(audio_temp_buffer.data(), frames * 2);
        }
        
        // Video processing
        // bitmap.data is updated.
        // Copy to current_pixel_buffer (managed by native-lib)
        // We assume native-lib provided a 256x240 buffer, but Genesis is 320x224.
        // We need to scale or crop. For prototype, we will crop/center or just copy what fits.
        
        extern u32* current_pixel_buffer;
        if (current_pixel_buffer) {
            // Genesis resolution varies. usually 320x224.
            // Screen is 256x240.
            // If Genesis is 320 wide, we can't fit in 256 without scaling.
            // NOTE: The Kotlin side expects 256x240 fixed?
            // "AndroidEmulatorEngine.kt" implies fixed VideoFrame(256, 240).
            // We should probably resize the Kotlin bitmap to 320x240 or scale here.
            // Simple Nearest Neighbor scaling for now: 320 -> 256 (skip pixels)
            
            int v_w = bitmap.viewport.w;
            int v_h = bitmap.viewport.h;
            
            // Center in 256x240
            for (int y = 0; y < 240; y++) {
                if (y >= v_h) break;
                
                for (int x = 0; x < 256; x++) {
                    // Map x(0..255) to src(0..v_w)
                    int src_x = (x * v_w) / 256;
                    int src_y = y; // Vertical fits (224 < 240)
                    
                    if (src_y < v_h) {
                        uint32_t color = video_buffer[src_y * 320 + src_x];
                        // GenPlusGX 32bpp is likely ARGB or ABGR.
                        // Force Alpha to 0xFF
                        current_pixel_buffer[y * 256 + x] = color | 0xFF000000; 
                    }
                }
            }
        }
    }

    void reset() override {
        system_reset();
    }

    void setController(int port, int state) override {
        // Map LaiNES bitmask to Genesis
        // NES: A(0), B(1), Sel(2), Start(3), Up(4), Down(5), Left(6), Right(7)
        // Genesis: Up(0), Down(1), Left(2), Right(3), B(4), C(5), A(6), Start(7)
        
        // input.pad needs bitmap.
        // #define INPUT_B   0x10
        // #define INPUT_C   0x20
        // #define INPUT_A   0x40
        // #define INPUT_START 0x80
        // #define INPUT_MODE  0x0800
        // #define INPUT_X     0x0400
        // #define INPUT_Y     0x0200
        // #define INPUT_Z     0x0100
        
        int gen_state = 0;
        if (state & (1 << 4)) gen_state |= 0x01; // UP
        if (state & (1 << 5)) gen_state |= 0x02; // DOWN
        if (state & (1 << 6)) gen_state |= 0x04; // LEFT
        if (state & (1 << 7)) gen_state |= 0x08; // RIGHT
        if (state & (1 << 1)) gen_state |= 0x10; // B (NES B -> Gen B)
        if (state & (1 << 0)) gen_state |= 0x20; // A (NES A -> Gen C for main action)
                                                 // Map NES Select to A?
        if (state & (1 << 3)) gen_state |= 0x80; // START
        
        input.pad[0] = gen_state;
    }
    
    void getPixels(uint32_t* buffer, int width, int height) override {}
    void getAudioSamples(std::vector<int16_t>& buffer) override {}
};
