package org.firstinspires.ftc.teamcode.Driving;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Test", group = "---")
public class Test extends OpMode {
    Servo shoulderServo, elbowServo;
    CRServo spinnerServo;
    DcMotor baseMotor;
    double shoulderServoPosition = 0.5;
    double elbowServoPosition = 0.5;
    double spinnerServoPower = 0.0;

    private boolean isPressingA = false;
    private boolean isPressingB = false;
    private boolean isPressingY = false;
    private boolean isPressingX = false;
    private boolean isPressingDpadLeft = false;
    private boolean isPressingDpadRight = false;
    private boolean isPressingDpadUp = false;
    private boolean isPressingDpadDown = false;


    @Override
    public void init() {
        shoulderServo = hardwareMap.get(Servo.class, "shoulder");
        elbowServo = hardwareMap.get(Servo.class, "elbow");
        spinnerServo = hardwareMap.get(CRServo.class, "spinner");
        baseMotor = hardwareMap.get(DcMotor.class, "base");
    }

    @Override
    public void loop() {
        telemetry.addData("Shoulder", shoulderServoPosition);
        telemetry.addData("Elbow", elbowServoPosition);
        telemetry.addData("Spinner", spinnerServoPower);
        telemetry.update();

        if (gamepad1.back) {
            requestOpModeStop();
        }

        //Detect A
        if (gamepad1.a && !isPressingA) {
            isPressingA = true;
            //----------
            spinnerServoPower += 0.01;
            spinnerServo.setPower(spinnerServoPower);
            //----------
        } else if (!gamepad1.a && isPressingA) {
            isPressingA = false;
        }

        //Detect B
        if (gamepad1.b && !isPressingB) {
            isPressingB = true;

        } else {
            isPressingB = false;
        }

        if (gamepad1.left_bumper)
            baseMotor.setPower(-0.4);
        else if (gamepad1.right_bumper)
            baseMotor.setPower(0.4);
        else
            baseMotor.setPower(0);

        //Detect Y
        if (gamepad1.y && !isPressingY) {
            isPressingY = true;
            //----------
            spinnerServoPower -= 0.01;
            spinnerServo.setPower(spinnerServoPower);
            //----------
        } else if (!gamepad1.y && isPressingY) {
            isPressingY = false;
        }

        //Detect X
        if (gamepad1.x && !isPressingX) {
            isPressingX = true;

        } else if (!gamepad1.x && isPressingX) {
            isPressingX = false;
        }

        //Detect Dpad
        if (gamepad1.dpad_up && !isPressingDpadUp) {
            isPressingDpadUp = true;
            //----------
            shoulderServoPosition += 0.01;
            shoulderServo.setPosition(shoulderServoPosition);
            //----------
        } else if (!gamepad1.dpad_up && isPressingDpadUp) {
            isPressingDpadUp = false;
        }

        if (gamepad1.dpad_down && !isPressingDpadDown) {
            isPressingDpadDown = true;
            //----------
            shoulderServoPosition -= 0.01;
            shoulderServo.setPosition(shoulderServoPosition);
            //----------
        } else if (!gamepad1.dpad_down && isPressingDpadDown) {
            isPressingDpadDown = false;
        }

        if (gamepad1.dpad_left && !isPressingDpadLeft) {
            isPressingDpadLeft = true;
            //----------
            elbowServoPosition += 0.01;
            elbowServo.setPosition(elbowServoPosition);
            //----------
        } else if (!gamepad1.dpad_left && isPressingDpadLeft) {
            isPressingDpadLeft = false;
        }

        if (gamepad1.dpad_right && !isPressingDpadRight) {
            isPressingDpadRight = true;
            //----------
            elbowServoPosition -= 0.01;
            elbowServo.setPosition(elbowServoPosition);
            //----------
        } else if (!gamepad1.dpad_right && isPressingDpadRight) {
            isPressingDpadRight = false;
        }
    }
}
