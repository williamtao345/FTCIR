package org.firstinspires.ftc.teamcode.Driving;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import android.graphics.Bitmap;

@TeleOp(name = "Object Detection", group = "---")
public class ObjectDetection extends LinearOpMode {
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
        telemetry.setAutoClear(false);

        final OpenCvWebcam camera;
        final ObjectDetector detector;

        try {
            BaseOptions baseOptions = BaseOptions.builder().build();
            ObjectDetectorOptions options = ObjectDetectorOptions.builder().setBaseOptions(baseOptions).build();
            detector = ObjectDetector.createFromFileAndOptions(hardwareMap.appContext,
                    "model.tflite", options);

            int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id",
                    hardwareMap.appContext.getPackageName());
            WebcamName webcamName = hardwareMap.get(WebcamName.class, "cv");
            camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName, cameraMonitorViewId);
            camera.openCameraDevice();
            camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
        } catch (Exception e) {
            printException(e);
            return;
        } finally {
            waitForStart();
        }

        try {
            camera.setPipeline(new OpenCvPipeline() {
                boolean isPressingA = true;

                @Override
                public Mat processFrame(Mat input) {
                    if (gamepad1.a && !isPressingA) {
                        isPressingA = true;
                        Bitmap bmp = Bitmap.createBitmap(input.cols(), input.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(input, bmp);

                        List<Detection> results = detector.detect(TensorImage.fromBitmap(bmp));
                        for (Detection det : results) {
                            print(det.getBoundingBox());
                            for (Category cat : det.getCategories())
                                print(cat);
                        }
                        print("----------");
                    } else if (!gamepad1.a && isPressingA) {
                        isPressingA = false;
                    }
                    return input;
                }
            });
        } catch (Exception e) {
            printException(e);
        } finally {
            while (opModeIsActive())
                ;
        }
    }
}
