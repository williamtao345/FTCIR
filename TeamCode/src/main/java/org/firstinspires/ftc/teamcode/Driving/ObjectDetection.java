package org.firstinspires.ftc.teamcode.Driving;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions;
import org.tensorflow.lite.support.image.TensorImage;

import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;

@TeleOp(name = "Object Detection", group = "---")
public class ObjectDetection extends LinearOpMode {
    private native void dumpEvents();

    private void print(Object o) {
        telemetry.addData("+", o);
        telemetry.update();
    }

    /* private */ public void print(String fmt, Object... o) {
        telemetry.addData("+", fmt, o);
        telemetry.update();
    }

    @Override
    public void runOpMode() throws InterruptedException {
        // System.loadLibrary("mouse_events");
        telemetry.setAutoClear(false);
        try {
            var baseOptions = BaseOptions.builder().build();
            var options = ObjectDetectorOptions.builder().setBaseOptions(baseOptions).build();
            var detector = ObjectDetector.createFromFileAndOptions(hardwareMap.appContext, "model.tflite", options);
            // var inputStream = hardwareMap.appContext.getAssets().open("image.png");
            // var image = TensorImage.fromBitmap(BitmapFactory.decodeStream(inputStream));
            // var results = detector.detect(image);
            print("hardwareMap.appContext is %s", hardwareMap.appContext.getClass());
            var cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id",
                    hardwareMap.appContext.getPackageName());
            var webcamName = hardwareMap.get(WebcamName.class, "cv");
            var camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName, cameraMonitorViewId);
            camera.openCameraDevice();
            print("Camera is opened");
            camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
            waitForStart();
            camera.setPipeline(new OpenCvPipeline() {
                @Override
                public Mat processFrame(Mat input) {
                    var bmp = Bitmap.createBitmap(input.cols(), input.rows(), Bitmap.Config.ARGB_8888);
                    var results = detector.detect(TensorImage.fromBitmap(bmp));
                    for (var det : results) {
                        print(det.getBoundingBox());
                        for (var cat : det.getCategories())
                            print(cat);
                    }
                    print("----------");
                    return input;
                }
            });
            // camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            // @Override
            // public void onOpened() {
            // }

            // @Override
            // public void onError(int errorCode) {
            // print("Open camera failed: %d", errorCode);
            // }
            // });
        } catch (Exception e) {
            var writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            print(e);
            print(writer.toString());
            throw new InterruptedException();
        }

        // var thread = new Thread(() -> {
        // dumpEvents();
        // });
        // thread.start();
        while (opModeIsActive())
            ;
        // thread.stop();
    }
}
