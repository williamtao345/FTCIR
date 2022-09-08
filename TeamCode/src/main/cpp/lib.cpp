#include <errno.h>
#include <fcntl.h>
#include <jni.h>
#include <linux/input.h>
#include <string.h>
#include <type_traits>
#include <unistd.h>

#define PRINTF(fmt, ...) tprintf(*env, obj, fmt, ##__VA_ARGS__)

template<typename... Args>
void tprintf(JNIEnv& env, jobject obj, const char* fmt, Args&&... args)
{
    auto arr = env.NewObjectArray(sizeof...(args), env.FindClass("java/lang/String"), env.NewStringUTF(""));
    size_t i = 0;
    (..., [&] {
        using T = std::remove_reference_t<Args>;
        static_assert(std::is_same_v<T, char*> || std::is_integral_v<T>);
        jobject o = nullptr;
        if constexpr (std::is_same_v<T, char*>) {
            o = env.NewStringUTF(args);
        } else if constexpr (std::is_integral_v<T>) {
            auto int_class = env.FindClass("java/lang/Integer");
            o = env.NewObject(int_class, env.GetMethodID(int_class, "<init>", "(I)V"), args);
        }

        env.SetObjectArrayElement(arr, i++, o);
    }());
    auto klass = env.FindClass("org/firstinspires/ftc/teamcode/Driving/ObjectDetection");
    auto method = env.GetMethodID(klass, "print", "(Ljava/lang/String;[Ljava/lang/Object;)V");
    env.CallVoidMethod(obj, method, env.NewStringUTF(fmt), arr);
}

JNIEXPORT void JNICALL Java_org_firstinspires_ftc_teamcode_Driving_ObjectDetection_dumpEvents(JNIEnv* env, jobject obj)
{
    auto fd = open("/dev/input/event0", O_RDONLY);
    if (fd < 0) {
        PRINTF("Failed to open: %s", strerror(errno));
        return;
    }

    input_event ev {};
    int x = 0, y = 0;

    for (;;) {
        auto rc = read(fd, &ev, sizeof(ev));
        if (rc < 0) {
            PRINTF("Failed to read: %s", strerror(errno));
            break;
        } else if (rc == 0) {
            PRINTF("Reached EOF");
            break;
        } else {
            if (ev.type == EV_REL) {
                if (ev.value == REL_X)
                    x += ev.value;
                else if (ev.value == REL_Y)
                    y += ev.value;
                else
                    PRINTF("Unknown value for EV_REL: %d", ev.value);
            } else if (ev.type == EV_SYN) {
                if (ev.value == SYN_REPORT) {
                    PRINTF("%d, %d", x, y);
                    x = y = 0;
                } else {
                    PRINTF("Unknown value for EV_SYN: %d", ev.value);
                }
            } else {
                PRINTF("Unknown type %d", ev.type);
            }
        }
    }

    close(fd);
}
