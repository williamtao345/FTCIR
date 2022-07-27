/***********************************************************************
 *  GamePadController.java, 2022-07-27 at 11:57:45 CST.
 *  Copyright (c) 2022-2022 Xuefei (William) Tao. All Rights Reserved.
 **********************************************************************/

package org.firstinspires.ftc.teamcode.Utils;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.hardware.bosch.BNO055IMU;

/**
 * @author William
 */
public class GamePadController {
    //TODO: Configs
    final double P = 0.96;
    final double I = 0.03;
    final double D = 0.2;
    final double T = 0.02;

    private volatile boolean isHeadless = false;
    private volatile boolean isUsingPidHeadingControl = false;
    private volatile double headlessForwardHeading = 0;
    private volatile double tranSensitivity = 0.5;
    private volatile double headSensitivity = 0.4;

    BNO055IMU imu;
    PIDController hPid;
    private final Gamepad gamePad;
    private final DcMotor lfMotor;
    private final DcMotor rfMotor;
    private final DcMotor lbMotor;
    private final DcMotor rbMotor;

    private boolean isDriverControlling = false;
    private double pidTargetHeading = 0;

    private Thread runningThread;

    /**
     * @param lfMotor needs to be reversed manually if needed
     */
    public GamePadController(Gamepad gamePad, BNO055IMU imu, DcMotor lfMotor, DcMotor rfMotor, DcMotor lbMotor, DcMotor rbMotor) {
        this.gamePad = gamePad;
        this.imu = imu;
        this.lfMotor = lfMotor;
        this.rfMotor = rfMotor;
        this.lbMotor = lbMotor;
        this.rbMotor = rbMotor;

        hPid = new PIDController(P, I, D);
        hPid.setTolerance(T);

        runningThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (isHeadless) {
                    headlessDriveRobot();
                } else {
                    normalDriveRobot();
                }
            }
        });
    }

    /**
     * Start detecting gamePad and control the motors in a thread.
     */
    public void run() {
        runningThread.start();
    }

    /**
     * @param tranSensitivity value between 0.01 and 1
     * @param headSensitivity value between 0.01 and 1
     */
    public void changeSensitivity(double tranSensitivity, double headSensitivity) {
        this.tranSensitivity = Math.max(0.01, Math.min(tranSensitivity, 1));
        this.headSensitivity = Math.max(0.01, Math.min(headSensitivity, 1));
    }

    /**
     * @param on true to turn on Headless Mode
     */
    public void setHeadlessMode(boolean on) {
        this.isHeadless = on;
    }

    /**
     * @param on true to turn on HeadingAssisting Mode
     */
    public void setHeadingAssistingMode(boolean on) {
        this.isUsingPidHeadingControl = on;
    }

    /**
     * Set current heading as forward heading.
     */
    public void setHeadlessForwardHeading() {
        this.headlessForwardHeading = getHeading();
    }

    /**
     * Set heading parameter as forward heading.
     * @param heading forward heading
     */
    public void setHeadlessForwardHeading(double heading) {
        this.headlessForwardHeading = Math.max(-Math.PI, Math.min(heading, Math.PI));
    }

    /**
     * Get Headless Mode status.
     * @return
     */
    public boolean isUsingHeadlessMode() {
        return this.isHeadless;
    }

    /**
     * Get HeadingAssisting Mode status.
     * @return
     */
    public boolean isUsingHeadingAssistingMode() {
        return this.isUsingPidHeadingControl;
    }

    /**
     * Clean up all the using threads.
     */
    public void clean() {
        runningThread.interrupt();
    }

    private void normalDriveRobot() {
        double y = gamePad.left_stick_y * tranSensitivity;
        double x = -gamePad.left_stick_x * tranSensitivity;
        double gph = gamePad.right_stick_x;
        double rx;

        if (isUsingPidHeadingControl && gph == 0) {
            if (isDriverControlling) {
                //TODO: Change the axle of IMU angle.
                pidTargetHeading = imu.getAngularOrientation().firstAngle;
                hPid.reset();
                isDriverControlling = false;
            }
            rx = hPid.calculate(getFakePidHeading());
        } else {
            isDriverControlling = true;
            rx = -gph * headSensitivity;
        }

        lfMotor.setPower(y + x + rx);
        rfMotor.setPower(y - x - rx);
        lbMotor.setPower(y - x + rx);
        rbMotor.setPower(y + x - rx);
    }

    private void headlessDriveRobot() {
        double h = getHeading() + Math.PI;
        double gpx = gamePad.left_stick_x;
        double gpy = -gamePad.left_stick_y;
        double gph = gamePad.right_stick_x;

        double p = Math.sqrt(gpx * gpx + gpy * gpy) * tranSensitivity;
        double th;

        if (gpy > 0)
            th = -Math.atan(gpx / gpy) - h;
        else
            th = Math.PI - Math.atan(gpx / gpy) - h;

        double x, y;
        if (Double.isNaN(th)) {
            x = 0;
            y = 0;
        } else {
            x = (double) (-p * Math.sin(th));
            y = (double) (p * Math.cos(th));
        }

        double rx;

        if (isUsingPidHeadingControl && gph == 0) {
            if (isDriverControlling) {
                //TODO: Change the axle of IMU angle.
                pidTargetHeading = imu.getAngularOrientation().firstAngle;
                hPid.reset();
                isDriverControlling = false;
            }
            rx = hPid.calculate(getFakePidHeading());
        } else {
            isDriverControlling = true;
            rx = -gph * headSensitivity;
        }

        lfMotor.setPower(y + x + rx);
        rfMotor.setPower(y - x - rx);
        lbMotor.setPower(y - x + rx);
        rbMotor.setPower(y + x - rx);
    }

    private double getHeading() {
        return imu.getAngularOrientation().firstAngle - headlessForwardHeading;
    }

    private double getFakePidHeading() {
        double heading = imu.getAngularOrientation().firstAngle - pidTargetHeading;
        if (heading > Math.PI) {
            return heading - 2 * Math.PI;
        } else if (heading < -Math.PI) {
            return heading + 2 * Math.PI;
        }

        return heading;
    }
}
