package org.firstinspires.ftc.teamcode.Driving;

import java.util.Iterator;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Vuforia Test", group = "---")
public class VuforiaTest extends LinearOpMode {
    private static final String VUFORIA_KEY = "AfRjwKL/////AAABmTrH+bqRs0kUgQpPaGMZ8LI5u4mJVLgnN68jOEH6enly1facvjL+txNmKbljuCR18OGudT/ktzKmOA3kRM2JTAN/E5EGxYUKFuoubhN1TdGuZL9TFBRM/QZR9USMeAhVT0kyoM3SID38qg4Wf75fBOZFB+9iLeGXT5cyZb4yAyedEKg3XoeBN8FewLz/UJ2JLkCXN/Uo1ib8tURE3yHQ1fYpKVX9ZrlQWISTB9Iztm2LptlpU+fiaWx9/DcjMHLN8xIblcnDF5Vgb7sLAZZLnHRRQ4VMlmbsZT5L/XQ7Ner8Hx1M8Yy9JCBsIO0CXltMXJmxaHNhPit1xSXI6h+ik3i0Ycrzmmd4ia+1DjW8GAAD";
    private static final float MM_PER_INCH = 25.4f;
    private static final float TARGET_HEIGHT = 6 * MM_PER_INCH;
    private static final float HALF_FIELD = 72 * MM_PER_INCH;
    private static final float ONE_AND_HALF_TILE = 36 * MM_PER_INCH;

    private static final float CAMERA_FORWARD_DISPLACEMENT = 0.0f * MM_PER_INCH;
    private static final float CAMERA_VERTICAL_DISPLACEMENT = 6.0f * MM_PER_INCH;
    private static final float CAMERA_LEFT_DISPLACEMENT = 0.0f * MM_PER_INCH;

    private void identifyTarget(Iterator<VuforiaTrackable> it, String name, float dx, float dy, float dz, float rx,
            float ry, float rz) {
        VuforiaTrackable target = it.next();
        target.setName(name);
        target.setLocation(OpenGLMatrix.translation(dx, dy, dz).multiplied(
                Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, rx, ry, rz)));
    }

    @Override
    public void runOpMode() throws InterruptedException {
        WebcamName webcam = hardwareMap.get(WebcamName.class, "Webcam");
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id",
                hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraName = webcam;
        parameters.useExtendedTracking = false;
        VuforiaLocalizer vuforia = ClassFactory.getInstance().createVuforia(parameters);
        VuforiaTrackables targets = vuforia.loadTrackablesFromAsset("PowerPlay");

        Iterator<VuforiaTrackable> it = targets.iterator();
        identifyTarget(it, "Red Audience Wall", -HALF_FIELD, -ONE_AND_HALF_TILE, TARGET_HEIGHT, 90, 0, 90);
        identifyTarget(it, "Red Rear Wall", HALF_FIELD, -ONE_AND_HALF_TILE, TARGET_HEIGHT, 90, 0, -90);
        identifyTarget(it, "Blue Audience Wall", -HALF_FIELD, ONE_AND_HALF_TILE, TARGET_HEIGHT, 90, 0, 90);
        identifyTarget(it, "Blue Rear Wall", HALF_FIELD, ONE_AND_HALF_TILE, TARGET_HEIGHT, 90, 0, -90);

        OpenGLMatrix cameraLocation = OpenGLMatrix
                .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
                .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.XZY, AngleUnit.DEGREES, 90,
                        90, 0));
        for (VuforiaTrackable target : targets)
            ((VuforiaTrackableDefaultListener) target.getListener()).setCameraLocationOnRobot(webcam, cameraLocation);

        waitForStart();

        targets.activate();
        while (opModeIsActive()) {
            for (VuforiaTrackable target : targets) {
                if (((VuforiaTrackableDefaultListener) target.getListener()).isVisible()) {
                    telemetry.addData("Visible Target", target.getName());
                    OpenGLMatrix transform = ((VuforiaTrackableDefaultListener) target.getListener())
                            .getRobotLocation();
                    if (transform != null) {
                        VectorF translation = transform.getTranslation();
                        Orientation rotation = Orientation.getOrientation(transform, AxesReference.EXTRINSIC,
                                AxesOrder.XYZ, AngleUnit.DEGREES);
                        telemetry.addData("Position (mm)", translation);
                        telemetry.addData("Rotation (deg)", rotation);
                    } else {
                        telemetry.addData(target.getName(), "has no new transformation");
                    }
                    telemetry.update();
                    break;
                }
            }
        }
    }
}
