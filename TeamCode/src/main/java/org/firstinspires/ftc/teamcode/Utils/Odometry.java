package org.firstinspires.ftc.teamcode.Utils;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public class Odometry {
    public Odometry(OpMode opMode) {
        System.loadLibrary("t265");
        opMode.msStuckDetectStop = 10000;
    }

    public native void start();

    public native void stop();

    public native Pose2d getPoseMeters();
}
