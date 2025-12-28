#include <jni.h>
#include <string>
#include <android/log.h>
#include <stdio.h>
#include <vector>

// LaiNES Headers
#include "laines/src/include/cartridge.hpp"
#include "laines/src/include/cpu.hpp"
#include "laines/src/include/ppu.hpp"
#include "laines/src/include/gui.hpp"
#include "laines/src/include/apu.hpp"

#define TAG "RetroFun-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Global state
static std::vector<int16_t> audio_buffer;
static int joypad_state_p1 = 0;

// Destination for pixels
static u32* current_pixel_buffer = nullptr;
static int frame_count = 0;

namespace GUI {
    void new_frame(u32* pixels) {
        if (current_pixel_buffer) {
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

#include <errno.h>

extern "C" JNIEXPORT void JNICALL
Java_org_retrofun_project_data_emulation_AndroidEmulatorEngine_initNative(JNIEnv* env, jobject, jbyteArray romData) {
    jsize len = env->GetArrayLength(romData);
    jbyte* bytes = env->GetByteArrayElements(romData, NULL);
    
    // Save to temp file strictly for LaiNES API
    const char* filename = "/data/data/org.retrofun.project/cache/game.rom";
    FILE* f = fopen(filename, "wb");
    if (f) {
        size_t written = fwrite(bytes, 1, len, f);
        fclose(f);
        LOGI("Wrote ROM to %s, bytes: %zu/%d", filename, written, len);
    } else {
        LOGE("Failed to write ROM to %s. Errno: %d", filename, errno);
         // Try fallback
         filename = "/data/local/tmp/game.rom";
         f = fopen(filename, "wb");
         if (f) { 
            fwrite(bytes, 1, len, f); 
            fclose(f); 
            LOGI("Wrote ROM to fallback %s", filename);
         } else {
             LOGE("Failed to write ROM to fallback %s. Errno: %d", filename, errno);
         }
    }
    env->ReleaseByteArrayElements(romData, bytes, JNI_ABORT);

    audio_buffer.clear();
    
    try {
        LOGI("Initializing APU...");
        APU::init(); 
        LOGI("Loading Cartridge: %s", filename);
        Cartridge::load(filename);
        LOGI("LaiNES initialized successfully. PC: %04X", CPU::PC);
    } catch (const char* msg) {
        LOGE("LaiNES Init Error: %s", msg);
    } catch (...) {
        LOGE("Failed to initialize LaiNES (Unknown exception).");
    }
}

extern "C" JNIEXPORT jintArray JNICALL
Java_org_retrofun_project_data_emulation_AndroidEmulatorEngine_runFrameNative(JNIEnv* env, jobject) {
    static uint32_t frame_pixels[256 * 240];
    
    // Capture pixels from PPU/GUI callback
    current_pixel_buffer = frame_pixels;
    
    // Debug: Log first few frames only
    if (frame_count < 120 && frame_count % 30 == 0) {
        LOGI("RunFrameNative: Start frame %d", frame_count);
    }

    try {
        CPU::run_frame();
    } catch (...) {
        LOGE("CPU::run_frame threw exception!");
    }

    if (frame_count < 120 && frame_count % 30 == 0) {
        // Check center pixel
        LOGI("RunFrameNative: End frame %d. Center pixel: %08X", frame_count, frame_pixels[128*256 + 128]);
    }
    
    current_pixel_buffer = nullptr;
    frame_count++;
    
    // Create new int array to return to Kotlin
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
    if (select) state |= (1 << 2);
    if (start)  state |= (1 << 3);
    if (up)     state |= (1 << 4);
    if (down)   state |= (1 << 5);
    if (left)   state |= (1 << 6);
    if (right)  state |= (1 << 7);
    
    joypad_state_p1 = state;
}

extern "C" JNIEXPORT void JNICALL
Java_org_retrofun_project_data_emulation_AndroidEmulatorEngine_resetNative(JNIEnv* env, jobject) {
    CPU::power();
}
