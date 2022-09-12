#include <android/log.h>
#include <errno.h>
#include <fcntl.h>
#include <jni.h>
#include <linux/input.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>

#define LOG(fmt, ...) __android_log_print(ANDROID_LOG_DEBUG, "MouseEvents", fmt, ##__VA_ARGS__)

static constexpr auto DEVICE = "/dev/input/event2";
static pthread_t s_thread;

extern "C" {
JNIEXPORT void JNICALL Java_org_firstinspires_ftc_teamcode_Driving_MouseTest_mouseStart(JNIEnv*, jobject)
{
    auto rc = pthread_create(
        &s_thread, nullptr, [](void*) -> void* {
            LOG("Trying to open %s", DEVICE);
            auto fd = open(DEVICE, O_RDONLY);
            if (fd < 0) {
                LOG("Failed to open %s: %s", DEVICE, strerror(errno));
                return nullptr;
            }

            struct sigaction act { };
            act.sa_handler = [](int) {
                pthread_exit(nullptr);
            };
            auto rc = sigaction(SIGUSR2, &act, nullptr); // SIGUSR1 is blocked by JVM???
            if (rc < 0) {
                LOG("Failed to set sigaction: %s", strerror(errno));
                return nullptr;
            }

            pthread_cleanup_push([](void* fd) {
                auto f = *static_cast<int*>(fd);
                LOG("Closing file descriptor %d", f);
                close(f);
            },
                &fd);

            input_event ev {};
            int x = 0, y = 0, dx = 0, dy = 0;

            for (;;) {
                auto rc = read(fd, &ev, sizeof(ev));
                if (rc < 0) {
                    LOG("Failed to read: %s", strerror(errno));
                    break;
                }
                if (rc == 0) {
                    LOG("Reached EOF");
                    break;
                }

                if (ev.type == EV_REL) {
                    if (ev.code == REL_X)
                        dx += ev.value;
                    else if (ev.code == REL_Y)
                        dy += ev.value;
                    else
                        LOG("Unknown event code for EV_REL: %d", ev.code);
                } else if (ev.type == EV_SYN) {
                    if (ev.code == SYN_REPORT) {
                        x += dx;
                        y += dy;
                        LOG("x = %7d, y = %7d, dx = %4d, dy = %4d", x, y, dx, dy);
                        dx = dy = 0;
                    } else {
                        LOG("Unknown event code for EV_SYN: %d", ev.code);
                    }
                } else {
                    LOG("Unknown event type %d", ev.type);
                }
            }

            pthread_cleanup_pop(true);
            return nullptr;
        },
        nullptr);
    if (rc != 0)
        LOG("Failed to create thread: %s", strerror(rc));
}

JNIEXPORT void JNICALL Java_org_firstinspires_ftc_teamcode_Driving_MouseTest_mouseStop(JNIEnv*, jobject)
{
    LOG("Stopping thread");
    auto rc = pthread_kill(s_thread, SIGUSR2);
    if (rc != 0)
        LOG("Failed to send signal: %s", strerror(rc));
}
}
