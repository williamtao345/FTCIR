package org.firstinspires.ftc.teamcode.Driving;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.firstinspires.ftc.teamcode.Utils.Odometry;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "T265 Test", group = "---")
public class T265Test extends LinearOpMode {
    private void print(Object o) {
        telemetry.addData("+", o);
        telemetry.update();
    }

    private void print(String fmt, Object... o) {
        telemetry.addData("+", fmt, o);
        telemetry.update();
    }

    private void printException(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        print("***** EXCEPTION *****");
        print(e);
        print("***** STACK TRACE *****");
        print(writer.toString());
    }

    @Override
    public void runOpMode() throws InterruptedException {
        final Odometry odometry;
        try {
            odometry = new Odometry(this);
            odometry.start();
        } catch (Exception e) {
            printException(e);
            return;
        } finally {
            waitForStart();
        }

        try {
            while (opModeIsActive()) {
                Pose2d pose = odometry.getPoseMeters();
                print("%10.3f %10.3f (m)\n%10.3f (deg)", pose.getX(), pose.getY(), pose.getRotation().getDegrees());
            }
        } catch (Exception e) {
            printException(e);
        } finally {
            while (opModeIsActive())
                ;
            odometry.stop();
        }
    }
}
