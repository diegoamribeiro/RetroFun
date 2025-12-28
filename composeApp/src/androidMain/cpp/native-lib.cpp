#include <jni.h>
#include <string>
#include <android/log.h>
#include <stdio.h>
#include <vector>
#include <errno.h>

#define TAG "RetroFun-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Emulator Interfaces
#include "emulator_core.hpp"
#include "nes_core.hpp"
#include "genesis_core.hpp"

// Global state
static std::vector<int16_t> audio_buffer;
static int joypad_state_p1 = 0;

// Genesis Plus GX Globals
t_config config;
char GG_ROM[256];
char AR_ROM[256];
char SK_ROM[256];
char SK_UPMEM[256];
char GG_BIOS[256];
char MD_BIOS[256];
char CD_BIOS_EU[256];
char CD_BIOS_US[256];
char CD_BIOS_JP[256];
char MS_BIOS_US[256];
char MS_BIOS_EU[256];
char MS_BIOS_JP[256];

extern "C" void osd_input_update() {}

extern "C" int load_archive(char *filename, unsigned char *buffer, int maxsize, char *extension) {
    if (!filename || !buffer) return 0;

    FILE* f = fopen(filename, "rb");
    if (!f) {
        LOGE("load_archive: Failed to open %s", filename);
        return 0;
    }

    fseek(f, 0, SEEK_END);
    long size = ftell(f);
    fseek(f, 0, SEEK_SET);

    if (size > maxsize) {
        LOGE("load_archive: File too large (%ld > %d)", size, maxsize);
        size = maxsize;
    }

    size_t read_size = fread(buffer, 1, size, f);
    fclose(f);

    if (extension) {
        // Extract extension
        const char* dot = strrchr(filename, '.');
        if (dot) {
            strncpy(extension, dot + 1, 3);
            extension[3] = 0; // Ensure null char if needed, though load_rom uses char[4]
        } else {
            strcpy(extension, "BIN");
        }
    }
    
    LOGI("load_archive: Loaded %zu bytes from %s. Ext: %s", read_size, filename, extension ? extension : "NULL");

    return (int)read_size;
}

// YX5200 MP3 Stubs
extern "C" {
void yx5200_init(int samplerate) {}
void yx5200_reset(void) {}
void yx5200_write(unsigned int rx_data) {}
void yx5200_update(unsigned int samples) {}
int yx5200_context_save(uint8_t *state) { return 0; }
int yx5200_context_load(uint8_t *state) { return 0; }
}

// Destination for pixels (Shared between cores if they use callbacks)
u32* current_pixel_buffer = nullptr;
static int frame_count = 0;

// Helper function exports for LaiNES
namespace GUI {
    void new_frame(u32* pixels) {
        if (current_pixel_buffer) {
            // Force Alpha to 0xFF for Android Bitmap compatibility
            for (int i = 0; i < 256 * 240; i++) {
                current_pixel_buffer[i] = pixels[i] | 0xFF000000;
            }
        }
    }

    void new_samples(const s16* samples, size_t count) {
        audio_buffer.insert(audio_buffer.end(), samples, samples + count);
    }

    int get_joypad_state(int n) {
        if (n == 0) return joypad_state_p1;
        return 0;
    }

    bool is_fast_forward() { return false; }
}

// Active Core
static EmulatorCore* current_core = nullptr;
static bool game_loaded = false;

extern "C" JNIEXPORT void JNICALL
Java_org_retrofun_project_data_emulation_AndroidEmulatorEngine_initNative(JNIEnv* env, jobject, jbyteArray romData, jint consoleType) {
    jsize len = env->GetArrayLength(romData);
    jbyte* bytes = env->GetByteArrayElements(romData, NULL);
    
    // Use correct extension for Genesis Plus GX logic
    std::string filenameStr = "/data/data/org.retrofun.project/cache/game";
    if (consoleType == 1) filenameStr += ".md";
    else filenameStr += ".nes";
    
    const char* filename = filenameStr.c_str();

    FILE* f = fopen(filename, "wb");
    if (f) {
        size_t written = fwrite(bytes, 1, len, f);
        fclose(f);
        LOGI("Wrote ROM to %s, bytes: %zu/%d", filename, written, len);
    } else {
        LOGE("Failed to write ROM to %s. Errno: %d", filename, errno);
         // Fallback
         filenameStr = "/data/local/tmp/game";
         if (consoleType == 1) filenameStr += ".md";
         else filenameStr += ".nes";
         filename = filenameStr.c_str();
         
         f = fopen(filename, "wb");
         if (f) { 
            fwrite(bytes, 1, len, f); 
            fclose(f); 
            LOGI("Wrote ROM to fallback %s", filename);
         }
    }
    env->ReleaseByteArrayElements(romData, bytes, JNI_ABORT);

    audio_buffer.clear();
    frame_count = 0;
    
    if (current_core) {
        delete current_core;
        current_core = nullptr;
    }
    game_loaded = false;

    if (consoleType == 0) {
        LOGI("Initializing NES Core");
        current_core = new NesCore();
    } else {
        LOGI("Initializing Genesis Core");
        current_core = new GenesisCore();
    }

    if (current_core->loadGame(filename)) {
        LOGI("Core loaded game successfully.");
        game_loaded = true;
    } else {
        LOGE("Core failed to load game.");
        game_loaded = false;
    }
}

extern "C" JNIEXPORT jintArray JNICALL
Java_org_retrofun_project_data_emulation_AndroidEmulatorEngine_runFrameNative(JNIEnv* env, jobject) {
    static uint32_t frame_pixels[256 * 240]; // Max size buffer (Genesis might need resize)
    
    if (!current_core || !game_loaded) return nullptr;

    // Capture pixels from PPU/GUI callback (for LaiNES)
    // GenesisCore might write to this buffer differently.
    current_pixel_buffer = frame_pixels;
    
    try {
        current_core->runFrame();
    } catch (...) {
        LOGE("Core::runFrame threw exception!");
    }
    
    current_pixel_buffer = nullptr;
    frame_count++;
    
    // Create new int array to return to Kotlin
    // TODO: Handle dynamic resolution for Genesis (320x224 vs 256x240)
    // For now strict 256x240 for prototype verification
    jintArray result = env->NewIntArray(256 * 240);
    env->SetIntArrayRegion(result, 0, 256 * 240, (jint*)frame_pixels);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_org_retrofun_project_data_emulation_AndroidEmulatorEngine_getAudioSamplesNative(JNIEnv* env, jobject, jshortArray outBuffer) {
    size_t count = audio_buffer.size();
    if (count == 0) return 0;

    jsize limit = env->GetArrayLength(outBuffer);
    if (count > limit) count = limit;

    env->SetShortArrayRegion(outBuffer, 0, count, audio_buffer.data());
    audio_buffer.erase(audio_buffer.begin(), audio_buffer.begin() + count);

    return count;
}

extern "C" JNIEXPORT void JNICALL
Java_org_retrofun_project_data_emulation_AndroidEmulatorEngine_setControllerStateNative(
        JNIEnv* env, jobject, 
        jboolean up, jboolean down, jboolean left, jboolean right, 
        jboolean a, jboolean b, jboolean start, jboolean select) {
    
    int state = 0;
    if (a)      state |= (1 << 0);
    if (b)      state |= (1 << 1);
    // Select/Start mapping for NES
    if (select) state |= (1 << 2);
    if (start)  state |= (1 << 3);
    if (up)     state |= (1 << 4);
    if (down)   state |= (1 << 5);
    if (left)   state |= (1 << 6);
    if (right)  state |= (1 << 7);
    
    joypad_state_p1 = state;
    
    if (current_core) {
        current_core->setController(0, state);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_org_retrofun_project_data_emulation_AndroidEmulatorEngine_resetNative(JNIEnv* env, jobject) {
    if (current_core) current_core->reset();
}
