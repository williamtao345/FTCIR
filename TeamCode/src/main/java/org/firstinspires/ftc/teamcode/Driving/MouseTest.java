package org.firstinspires.ftc.teamcode.Driving;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Mouse Test", group = "---")
public class MouseTest extends LinearOpMode {
    private native void mouseStart();

    private native void mouseStop();

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
        try {
            telemetry.setAutoClear(false);
            // We have to run this in adb due to permission error
            // Runtime.getRuntime().exec("su root chmod 777 /dev/input/*");
            System.loadLibrary("mouse_events");
        } catch (Exception e) {
            printException(e);
            return;
        } finally {
            waitForStart();
        }

        try {
            mouseStart();
        } catch (Exception e) {
            printException(e);
        } finally {
            while (opModeIsActive())
                ;
            mouseStop();
        }
    }
}
