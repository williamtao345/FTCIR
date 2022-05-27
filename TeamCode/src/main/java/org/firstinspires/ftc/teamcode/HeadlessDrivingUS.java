package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * -- Configs --
 * <p>
 * Drive motors:        "lf, rf, lb, rb"
 * Slide motor name:    "slide"
 * Base motor name:     "base"
 * Grabber servo name:  "grabber"
 */

/**
 * -- GamePad Control --
 * <p>
 * A:           grabber control
 * Y:           KILL robot
 * Dpad L&R:    rotate base
 * L Trigger:   retract slide
 * R Trigger:   extend slide
 */

@TeleOp(name = "GoDrive (American)", group = "---")
public class HeadlessDrivingUS extends LinearOpMode {
    private double tranSpeed = 0.5;
    private double headSpeed = 0.4;
    private double headlessForwardHeading = 0;
    private boolean isNormalMode = true;
    private boolean isHeadless = false;
    private boolean usingPidHeadingControl = false;

    private DcMotor lfMotor = null;
    private DcMotor rfMotor = null;
    private DcMotor lbMotor = null;
    private DcMotor rbMotor = null;

    BNO055IMU imu;

    double p = 0.96;
    double i = 0.03;
    double d = 0.2;
    double t = 0.02;
    double pidTargetHeading = 0;
    PIDController hPid = new PIDController(p, i, d);
    private boolean isDriverControlling = false;
    BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();

    private boolean isPressingA = false;
    private boolean isPressingB = false;
    private boolean isPressingY = false;
    private boolean isPressingX = false;
    private boolean isPressingLeftBumper = false;
    private boolean isPressingDpadLeft = false;
    private boolean isPressingDpadRight = false;
    private boolean isPressingDpadUp = false;
    private boolean isPressingDpadDown = false;
    int currentPidSelection = 0;

    @Override
    public void runOpMode() throws InterruptedException {
        initializeDrive();

        start();
        waitForStart();

        while (opModeIsActive()) {
            detectGamePad();
            printDriveInfo();

            telemetry.addLine();

            displayCurrentPidSelection();

            telemetry.addData("TargetHead", pidTargetHeading);
            telemetry.addData("FakePidHead", getFakePidHeading());
            telemetry.update();
        }
    }

    public void initializeDrive(){
        lfMotor = hardwareMap.get(DcMotor.class, "lf");
        rfMotor = hardwareMap.get(DcMotor.class, "rf");
        lbMotor = hardwareMap.get(DcMotor.class, "lb");
        rbMotor = hardwareMap.get(DcMotor.class, "rb");

        lfMotor.setDirection(DcMotor.Direction.REVERSE);
        rfMotor.setDirection(DcMotor.Direction.FORWARD);
        lbMotor.setDirection(DcMotor.Direction.FORWARD);
        rbMotor.setDirection(DcMotor.Direction.REVERSE);

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
        imu.initialize(parameters);

        hPid = new PIDController(p, i, d);
        hPid.setTolerance(t);
    }

    public void printDriveInfo(){
        if (isNormalMode) {
            telemetry.addData("Mode", "Normal");
        } else {
            telemetry.addData("Mode", "Sport");
        }

        if (isHeadless) {
            headlessDriveRobot();
            telemetry.addData("Headless", "ACTIVE");
        } else {
            normalDriveRobot();
            telemetry.addData("Headless", "OFF");
        }

        if (usingPidHeadingControl) {
            telemetry.addData("Heading Control Assist", "ACTIVE");
        } else {
            telemetry.addData("Heading Control Assist", "OFF");
        }
    }

    public void normalDriveRobot() {
        double y = gamepad1.right_stick_y * tranSpeed;
        double x = -gamepad1.right_stick_x * tranSpeed;
        double gph = gamepad1.left_stick_x;
        double rx;

        if (usingPidHeadingControl && gph == 0) {
            if (isDriverControlling) {
                pidTargetHeading = imu.getAngularOrientation().firstAngle;
                hPid.reset();
                isDriverControlling = false;
            }
            rx = hPid.calculate(getFakePidHeading());
        } else {
            isDriverControlling = true;
            rx = -gph * headSpeed;
        }

        lfMotor.setPower(y + x + rx);
        rfMotor.setPower(y - x - rx);
        lbMotor.setPower(y - x + rx);
        rbMotor.setPower(y + x - rx);
    }

    public void headlessDriveRobot() {
        double h = getHeading() + Math.PI;
        double gpx = gamepad1.right_stick_x;
        double gpy = -gamepad1.right_stick_y;
        double gph = gamepad1.left_stick_x;

        double p = Math.sqrt(gpx * gpx + gpy * gpy) * tranSpeed;
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
        if (usingPidHeadingControl && gph == 0) {
            if (isDriverControlling) {
                pidTargetHeading = imu.getAngularOrientation().firstAngle;
                hPid.reset();
                isDriverControlling = false;
            }
            rx = hPid.calculate(getFakePidHeading());
        } else {
            isDriverControlling = true;
            rx = -gph * headSpeed;
        }

        lfMotor.setPower(y + x + rx);
        rfMotor.setPower(y - x - rx);
        lbMotor.setPower(y - x + rx);
        rbMotor.setPower(y + x - rx);
    }

    public double getHeading() {
        return imu.getAngularOrientation().firstAngle - headlessForwardHeading;
    }

    public void detectGamePad() {
        if (gamepad1.back) {
            lfMotor.setPower(0);
            rfMotor.setPower(0);
            lbMotor.setPower(0);
            rbMotor.setPower(0);
            requestOpModeStop();
        }

        //Detect A
        if (gamepad1.a && !isPressingA) {
            isPressingA = true;
            if (isNormalMode) {
                isNormalMode = false;
                tranSpeed = 0.7;
                headSpeed = 0.5;
                telemetry.speak("Sport Mode");
            } else {
                isNormalMode = true;
                tranSpeed = 0.5;
                headSpeed = 0.4;
                telemetry.speak("Normal Mode");
            }

        } else if (!gamepad1.a && isPressingA) {
            isPressingA = false;
        }

        //Detect B
        if (gamepad1.b && !isPressingB) {
            isPressingB = true;
            headlessForwardHeading = imu.getAngularOrientation().firstAngle;

        } else {
            isPressingB = false;
        }

        //Detect Y
        if (gamepad1.y && !isPressingY) {
            isPressingY = true;
            usingPidHeadingControl = !usingPidHeadingControl;
            if (usingPidHeadingControl)
                telemetry.speak("PID Assist active");
            else
                telemetry.speak("PID Assist off");

        } else if (!gamepad1.y && isPressingY) {
            isPressingY = false;
        }

        //Detect X
        if (gamepad1.x && !isPressingX) {
            isPressingX = true;
            isHeadless = !isHeadless;
            if (isHeadless)
                telemetry.speak("Headless on");
            else
                telemetry.speak("Headless off");

        } else if (!gamepad1.x && isPressingX) {
            isPressingX = false;
        }

        //Detect
        if (gamepad1.left_bumper && !isPressingLeftBumper) {
            isPressingLeftBumper = true;

        } else if (!gamepad1.left_bumper && isPressingLeftBumper) {
            isPressingLeftBumper = false;
        }

        //Detect Dpad
        if (gamepad1.dpad_up && !isPressingDpadUp) {
            isPressingDpadUp = true;
            changeValueOfCurrentPidSelection(0.01);
        } else if (!gamepad1.dpad_up && isPressingDpadUp) {
            isPressingDpadUp = false;
        }

        if (gamepad1.dpad_down && !isPressingDpadDown) {
            isPressingDpadDown = true;
            changeValueOfCurrentPidSelection(-0.01);
        } else if (!gamepad1.dpad_down && isPressingDpadDown) {
            isPressingDpadDown = false;
        }

        if (gamepad1.dpad_left && !isPressingDpadLeft) {
            isPressingDpadLeft = true;
            currentPidSelection = (currentPidSelection + 3) % 4;
        } else if (!gamepad1.dpad_left && isPressingDpadLeft) {
            isPressingDpadLeft = false;
        }

        if (gamepad1.dpad_right && !isPressingDpadRight) {
            isPressingDpadRight = true;
            currentPidSelection = (currentPidSelection + 1) % 4;
        } else if (!gamepad1.dpad_right && isPressingDpadRight) {
            isPressingDpadRight = false;
        }
    }

    public void displayCurrentPidSelection() {
        String temp;
        switch (currentPidSelection) {
            case 0:
                temp = "P";
                break;
            case 1:
                temp = "I";
                break;
            case 2:
                temp = "D";
                break;
            default:
                temp = "T";
                break;
        }

        telemetry.addData("Selection", temp);
        telemetry.addData("P", p);
        telemetry.addData("I", i);
        telemetry.addData("D", d);
        telemetry.addData("T", t);
    }

    public void changeValueOfCurrentPidSelection(double offset) {
        switch (currentPidSelection) {
            case 0:
                p += offset;
                break;
            case 1:
                i += offset;
                break;
            case 2:
                d += offset;
                break;
            default:
                t += offset;
                break;
        }

        hPid = new PIDController(p, i, d);
        hPid.setTolerance(t);
    }

    public double getFakePidHeading() {
        double heading = imu.getAngularOrientation().firstAngle - pidTargetHeading;
        if (heading > Math.PI) {
            return heading - 2 * Math.PI;
        } else if (heading < -Math.PI) {
            return heading + 2 * Math.PI;
        }

        return heading;
    }
}
