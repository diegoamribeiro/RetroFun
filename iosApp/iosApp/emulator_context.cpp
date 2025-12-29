#include "emulator_context.hpp"
#include "nes_core.hpp"
#include <cstring>
#include <cstdio>

#ifdef __ANDROID__
#include <android/log.h>
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "RetroFun", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "RetroFun", __VA_ARGS__)
#else
#define LOGI(...) printf(__VA_ARGS__); printf("\n")
#define LOGE(...) printf("ERROR: "); printf(__VA_ARGS__); printf("\n")
#endif

u32* current_pixel_buffer = nullptr;
int joypad_state_p1 = 0;

namespace GUI {
    void new_frame(u32* pixels) {
        if (current_pixel_buffer) {
            for (int i = 0; i < 256 * 240; i++) {
                current_pixel_buffer[i] = pixels[i] | 0xFF000000;
            }
        }
    }

    void new_samples(const s16* samples, size_t count) {
        EmulatorContext::getInstance().pushAudio(samples, count);
    }

    int get_joypad_state(int n) {
        if (n == 0) return joypad_state_p1;
        return 0;
    }

    bool is_fast_forward() { return false; }
}

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
    
    // Escrever ROM para arquivo se fornecido como dados
    if (romData && romSize > 0 && romPath) {
        FILE* f = fopen(romPath, "wb");
        if (f) {
            fwrite(romData, 1, romSize, f);
            fclose(f);
            LOGI("ROM escrita em %s", romPath);
        }
    }

    // Apenas NES suportado no iOS
    if (consoleType == 0) {
        LOGI("Inicializando NES Core");
        currentCore = new NesCore();
    } else {
        LOGE("Genesis não suportado no iOS");
        return;
    }

    if (currentCore && currentCore->loadGame(romPath)) {
        LOGI("Jogo carregado com sucesso");
        gameLoaded = true;
    } else {
        LOGE("Falha ao carregar jogo");
        gameLoaded = false;
    }
}

void EmulatorContext::runFrame(uint32_t* pixels, int& width, int& height) {
    if (!currentCore || !gameLoaded) return;

    current_pixel_buffer = pixels;
    
    try {
        currentCore->runFrame();
    } catch (...) {
        LOGE("Exceção no runFrame");
    }
    
    current_pixel_buffer = nullptr;
    width = 256;
    height = 240;
}

int EmulatorContext::getAudioSamples(int16_t* buffer, int maxCount) {
    size_t count = audioBuffer.size();
    if (count == 0) return 0;
    if (count > maxCount) count = maxCount;
    
    std::memcpy(buffer, audioBuffer.data(), count * sizeof(int16_t));
    audioBuffer.erase(audioBuffer.begin(), audioBuffer.begin() + count);
    
    return count;
}

void EmulatorContext::setControllerState(int playerId, int state) {
    joypadStateP1 = state;
    joypad_state_p1 = state;
    if (currentCore) {
        currentCore->setController(playerId, state);
    }
}

void EmulatorContext::reset() {
    if (currentCore) currentCore->reset();
}
