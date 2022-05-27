package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
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

@TeleOp(name = "DriverControl", group = "---")
@Disabled
public class DriverControl extends LinearOpMode {
    private static double GRABBER_CLOSE_POSITION = 0.17;
    private static double GRABBER_OPEN_POSITION = 0;
    private static double MAX_SLIDE_EXTENDING_SPEED = 0.7;

    private DcMotor lfMotor = null;
    private DcMotor rfMotor = null;
    private DcMotor lbMotor = null;
    private DcMotor rbMotor = null;
    private DcMotor baseMotor = null;
    private DcMotor slideMotor = null;
    private Servo grabber = null;
//    BNO055IMU imu;

    Thread driveThread = null;
    private boolean grabberIsClosed = true;

    boolean isPressingA = false;
    boolean isPressingDpadLeft = false;
    boolean isPressingDpadRight = false;

    @Override
    public void runOpMode() throws InterruptedException {
        lfMotor = hardwareMap.get(DcMotor.class, "lf");
        rfMotor = hardwareMap.get(DcMotor.class, "rf");
        lbMotor = hardwareMap.get(DcMotor.class, "lb");
        rbMotor = hardwareMap.get(DcMotor.class, "rb");
        baseMotor = hardwareMap.get(DcMotor.class, "base");
        slideMotor = hardwareMap.get(DcMotor.class, "slide");
        grabber = hardwareMap.get(Servo.class, "grabber");

        lfMotor.setDirection(DcMotor.Direction.FORWARD);
        rfMotor.setDirection(DcMotor.Direction.REVERSE);
        lbMotor.setDirection(DcMotor.Direction.REVERSE);
        rbMotor.setDirection(DcMotor.Direction.REVERSE);
        slideMotor.setDirection(DcMotor.Direction.FORWARD);
        baseMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        lfMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rfMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lbMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rbMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        baseMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        //Initialize imu
//        imu = hardwareMap.get(BNO055IMU.class, "imu");
//        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
//        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
//        imu.initialize(parameters);

        driveThread = new Thread(this::driveRobot);

        grabber.setPosition(GRABBER_OPEN_POSITION);
        grabberIsClosed = false;

        waitForStart();

//        driveThread.start();
        while (opModeIsActive()) {
            detectGamePad();
        }
    }

    public void detectGamePad() {
        driveRobot();
        if (gamepad1.a && !isPressingA) {
            isPressingA = true;

            if (grabberIsClosed) {
                grabber.setPosition(GRABBER_OPEN_POSITION);
                grabberIsClosed = false;
            } else {
                grabber.setPosition(GRABBER_CLOSE_POSITION);
                grabberIsClosed = true;
            }

        } else if (!gamepad1.a && isPressingA) {
            isPressingA = false;
        }

        if (gamepad1.y) {
            lfMotor.setPower(0);
            rfMotor.setPower(0);
            lbMotor.setPower(0);
            rbMotor.setPower(0);
            grabber.setPosition(GRABBER_OPEN_POSITION);
            requestOpModeStop();
        }

        //Control the base
        if (gamepad1.dpad_left && !isPressingDpadLeft) {
            isPressingDpadLeft = true;
            baseMotor.setPower(0.5);
        } else if (!gamepad1.dpad_left && isPressingDpadLeft) {
            isPressingDpadLeft = false;
            baseMotor.setPower(0);
        }
        if (gamepad1.dpad_right && !isPressingDpadRight) {
            isPressingDpadRight = true;
            baseMotor.setPower(-0.5);
        } else if (!gamepad1.dpad_right && isPressingDpadRight) {
            isPressingDpadRight = false;
            baseMotor.setPower(0);
        }

        //Control the slide motor
        if (gamepad1.left_trigger > 0.1) {
            slideMotor.setPower(-gamepad1.left_trigger * MAX_SLIDE_EXTENDING_SPEED);
        } else if (gamepad1.right_trigger > 0.1) {
            slideMotor.setPower(gamepad1.right_trigger * MAX_SLIDE_EXTENDING_SPEED);
        } else {
            slideMotor.setPower(0);
        }
    }

    public void driveRobot() {
        double y = gamepad1.left_stick_y * 0.5;
        double x = -gamepad1.left_stick_x * 0.5;
        double rx = -gamepad1.right_stick_x * 0.3;

        lfMotor.setPower(y + x + rx);
        rfMotor.setPower(y - x - rx);
        lbMotor.setPower(y - x + rx);
        rbMotor.setPower(y + x - rx);
    }
}
