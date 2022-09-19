package org.firstinspires.ftc.teamcode.Driving;

import java.util.List;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "TensorFlow Test", group = "---")
public class TensorFlowTest extends LinearOpMode {
    private static final String TFOD_MODEL_ASSET = "PowerPlay.tflite";
    private static final String VUFORIA_KEY = "AfRjwKL/////AAABmTrH+bqRs0kUgQpPaGMZ8LI5u4mJVLgnN68jOEH6enly1facvjL+txNmKbljuCR18OGudT/ktzKmOA3kRM2JTAN/E5EGxYUKFuoubhN1TdGuZL9TFBRM/QZR9USMeAhVT0kyoM3SID38qg4Wf75fBOZFB+9iLeGXT5cyZb4yAyedEKg3XoeBN8FewLz/UJ2JLkCXN/Uo1ib8tURE3yHQ1fYpKVX9ZrlQWISTB9Iztm2LptlpU+fiaWx9/DcjMHLN8xIblcnDF5Vgb7sLAZZLnHRRQ4VMlmbsZT5L/XQ7Ner8Hx1M8Yy9JCBsIO0CXltMXJmxaHNhPit1xSXI6h+ik3i0Ycrzmmd4ia+1DjW8GAAD";
    private static final String[] LABELS = {
            "1 Bolt",
            "2 Bulb",
            "3 Panel"
    };

    private VuforiaLocalizer initVuforia() {
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraName = hardwareMap.get(WebcamName.class, "Webcam");
        return ClassFactory.getInstance().createVuforia(parameters);
    }

    private TFObjectDetector initTfod(VuforiaLocalizer vuforia) {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.75f;
        tfodParameters.isModelTensorFlow2 = true;
        tfodParameters.inputSize = 300;
        TFObjectDetector tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABELS);
        return tfod;
    }

    @Override
    public void runOpMode() throws InterruptedException {
        VuforiaLocalizer vuforia = initVuforia();
        TFObjectDetector tfod = initTfod(vuforia);
        tfod.activate();
        tfod.setZoom(1.0, 16.0 / 9.0);

        waitForStart();

        while (opModeIsActive()) {
            List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
            if (updatedRecognitions != null) {
                telemetry.addData("Number of recognitions", updatedRecognitions.size());

                for (Recognition recognition : updatedRecognitions) {
                    double col = (recognition.getLeft() + recognition.getRight()) / 2;
                    double row = (recognition.getTop() + recognition.getBottom()) / 2;
                    double width = Math.abs(recognition.getRight() - recognition.getLeft());
                    double height = Math.abs(recognition.getTop() - recognition.getBottom());

                    telemetry.addData("Image", "%s (%.0f %% Conf.)", recognition.getLabel(),
                            recognition.getConfidence() * 100);
                    telemetry.addData("- Position (Row/Col)", "%.0f / %.0f", row, col);
                    telemetry.addData("- Size (Width/Height)", "%.0f / %.0f", width, height);
                }
                telemetry.update();
            }
        }
    }
}
