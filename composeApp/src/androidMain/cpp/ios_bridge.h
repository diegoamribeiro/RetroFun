#ifndef IOS_BRIDGE_H
#define IOS_BRIDGE_H

#ifdef __cplusplus
extern "C" {
#endif

void ios_init(int consoleType, const char* romPath);
void ios_run_frame(int* pixels, int width, int height); 
int ios_get_audio(short* buffer, int maxCount);
void ios_set_input(int up, int down, int left, int right, int a, int b, int start, int select);
void ios_reset();

#ifdef __cplusplus
}
#endif

#endif
