package org.firstinspires.ftc.teamcode.Driving;

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

@TeleOp(name = "Marc", group = "---")
@Disabled
public class Marc extends LinearOpMode {
    private DcMotor lfMotor = null;
    private DcMotor rfMotor = null;
    private DcMotor lbMotor = null;
    private DcMotor rbMotor = null;

    @Override
    public void runOpMode() throws InterruptedException {
        lfMotor = hardwareMap.get(DcMotor.class, "lf");
        rfMotor = hardwareMap.get(DcMotor.class, "rf");
        lbMotor = hardwareMap.get(DcMotor.class, "lb");
        rbMotor = hardwareMap.get(DcMotor.class, "rb");

        lfMotor.setDirection(DcMotor.Direction.REVERSE);
        rfMotor.setDirection(DcMotor.Direction.FORWARD);
        lbMotor.setDirection(DcMotor.Direction.FORWARD);
        rbMotor.setDirection(DcMotor.Direction.REVERSE);

        lfMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rfMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lbMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rbMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        waitForStart();

        while (opModeIsActive()) {
            detectGamePad();
        }
    }

    public void detectGamePad() {
        driveRobot();

        if (gamepad1.back) {
            lfMotor.setPower(0);
            rfMotor.setPower(0);
            lbMotor.setPower(0);
            rbMotor.setPower(0);
            requestOpModeStop();
        }
    }

    public void driveRobot() {
        double y = gamepad1.left_stick_y * 0.5;
        double x = gamepad1.left_stick_x * 0.5;
        double rx = -gamepad1.right_stick_x * 0.3;

        lfMotor.setPower(y + x + rx);
        rfMotor.setPower(y - x - rx);
        lbMotor.setPower(y - x + rx);
        rbMotor.setPower(y + x - rx);
    }
}
