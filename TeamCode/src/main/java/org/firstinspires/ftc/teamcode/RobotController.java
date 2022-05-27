package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

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

@TeleOp(name = "Main Robot Controller", group = "---")
public class RobotController extends LinearOpMode {
    private static double PICK_UP_ELBOW_POSITION = 0.24;
    private static double DEPOSIT_ELBOW_POSITION = 0.98;
    private static double SPINNER_POWER = 0.3;
    private boolean isPickingUp = true;
    private int counter = 0;

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
    private DcMotor baseMotor = null;
    private Servo elbowServo = null;
    private CRServo spinnerServo;

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
    private boolean isPressingA2 = false;
    private boolean isPressingB = false;
    private boolean isPressingY = false;
    private boolean isPressingX = false;
    private boolean isPressingLeftBumper = false;
    private boolean isPressingRightBumper = false;
    private boolean isPressingDpadLeft = false;
    private boolean isPressingDpadRight = false;
    private boolean isPressingDpadUp = false;
    private boolean isPressingDpadDown = false;

    @Override
    public void runOpMode() throws InterruptedException {
        initializeDrive();

        start();
        waitForStart();


        while (opModeIsActive()) {
            detectGamePad();
            printDriveInfo();

            telemetry.addLine();

            telemetry.update();
        }
    }

    public void initializeDrive() {
        lfMotor = hardwareMap.get(DcMotor.class, "lf");
        rfMotor = hardwareMap.get(DcMotor.class, "rf");
        lbMotor = hardwareMap.get(DcMotor.class, "lb");
        rbMotor = hardwareMap.get(DcMotor.class, "rb");
        elbowServo = hardwareMap.get(Servo.class, "elbow");
        spinnerServo = hardwareMap.get(CRServo.class, "spinner");
        baseMotor = hardwareMap.get(DcMotor.class, "base");


        lfMotor.setDirection(DcMotor.Direction.REVERSE);
        rfMotor.setDirection(DcMotor.Direction.FORWARD);
        lbMotor.setDirection(DcMotor.Direction.FORWARD);
        rbMotor.setDirection(DcMotor.Direction.REVERSE);
        baseMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        elbowServo.setPosition(PICK_UP_ELBOW_POSITION);
        spinnerServo.setPower(SPINNER_POWER);

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
        imu.initialize(parameters);

        hPid = new PIDController(p, i, d);
        hPid.setTolerance(t);
    }

    public void printDriveInfo() {
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
        double y = gamepad1.left_stick_y * tranSpeed;
        double x = -gamepad1.left_stick_x * tranSpeed;
        double gph = gamepad1.right_stick_x;
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
        double gpx = gamepad1.left_stick_x;
        double gpy = -gamepad1.left_stick_y;
        double gph = gamepad1.right_stick_x;

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

        if (gamepad2.a && !isPressingA2) {
            isPressingA2 = true;
            counter++;
            if (counter == 1)
                telemetry.speak("Let's go Zack! " + counter + " ball taken");
            else
                telemetry.speak("Let's go Zack! " + counter + " balls taken");
        } else if(!gamepad2.a && isPressingA2) {
            isPressingA2 = false;
        }

        //Detect A
        if (gamepad1.a && !isPressingA) {
            isPressingA = true;
            if (isPickingUp) {
                isPickingUp = false;
                elbowServo.setPosition(DEPOSIT_ELBOW_POSITION);
                spinnerServo.setPower(0.1);
            } else {
                isPickingUp = true;
                elbowServo.setPosition(PICK_UP_ELBOW_POSITION);
                spinnerServo.setPower(SPINNER_POWER);
            }

        } else if (!gamepad1.a && isPressingA) {
            isPressingA = false;
        }

        //Detect B
        if (gamepad1.b && !isPressingB) {
            isPressingB = true;

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
            if (isNormalMode) {
                isNormalMode = false;
                tranSpeed = 0.7;
                headSpeed = 0.5;
                telemetry.speak("Sport Mode");
            } else {
                isNormalMode = true;
                tranSpeed = 0.5;
                headSpeed = 0.4;
                telemetry.speak("Slow Mode");
            }
        } else if (!gamepad1.x && isPressingX) {
            isPressingX = false;
        }

        //Detect
        if (gamepad1.left_bumper && !isPressingLeftBumper) {
            isPressingLeftBumper = true;
            isHeadless = !isHeadless;
            if (isHeadless)
                telemetry.speak("Headless on");
            else
                telemetry.speak("Headless off");
        } else if (!gamepad1.left_bumper && isPressingLeftBumper) {
            isPressingLeftBumper = false;
        }

        if (gamepad1.right_bumper && !isPressingRightBumper) {
            isPressingRightBumper = true;
            headlessForwardHeading = imu.getAngularOrientation().firstAngle;
        } else if (!gamepad1.right_bumper && isPressingRightBumper) {
            isPressingRightBumper = false;
        }

        //Detect Dpad
        if (gamepad1.dpad_up && !isPressingDpadUp) {
            isPressingDpadUp = true;


        } else if (!gamepad1.dpad_up && isPressingDpadUp) {
            isPressingDpadUp = false;
        }

        if (gamepad1.dpad_down && !isPressingDpadDown) {
            isPressingDpadDown = true;


        } else if (!gamepad1.dpad_down && isPressingDpadDown) {
            isPressingDpadDown = false;
        }

        if (gamepad1.dpad_left && !isPressingDpadLeft) {
            isPressingDpadLeft = true;


        } else if (!gamepad1.dpad_left && isPressingDpadLeft) {
            isPressingDpadLeft = false;
        }

        if (gamepad1.dpad_right && !isPressingDpadRight) {
            isPressingDpadRight = true;


        } else if (!gamepad1.dpad_right && isPressingDpadRight) {
            isPressingDpadRight = false;
        }
    }

    public double getFakePidHeading() {
        double heading = imu.getAngularOrientation().firstAngle - pidTargetHeading;
        if (heading > Math.PI)
            return heading - 2 * Math.PI;
        else if (heading < -Math.PI)
            return heading + 2 * Math.PI;
        else
            return heading;
    }
}

