package org.firstinspires.ftc.teamcode.Driving;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions;
import org.tensorflow.lite.support.image.TensorImage;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;

@TeleOp(name = "OD Tet", group = "---")
public class ODTest extends LinearOpMode {
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

            // TODO: Crash here
            var detector = ObjectDetector.createFromFileAndOptions(hardwareMap.appContext, "UltimateGoal.tflite",
                    options);

            // var inputStream = hardwareMap.appContext.getAssets().open("image.jpg");
            // var image = TensorImage.fromBitmap(BitmapFactory.decodeStream(inputStream));
            // var results = detector.detect(image);

            // print("----- ***** -----");
            // for (var det : results) {
            // print(det.getBoundingBox());
            // for (var cat : det.getCategories())
            // print("%s, %s, %d, %f", cat.getLabel(), cat.getDisplayName(), cat.getIndex(),
            // cat.getScore());
            // }
            // print("----- ***** -----");
            // print("hardwareMap.appContext is %s", hardwareMap.appContext.getClass());
            // var cameraMonitorViewId =
            // hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId",
            // "id",
            // hardwareMap.appContext.getPackageName());
            // var webcamName = hardwareMap.get(WebcamName.class, "cv");
            // var camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName,
            // cameraMonitorViewId);
            // camera.openCameraDevice();
            // print("Camera is opened");
            // camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
            waitForStart();
            // camera.setPipeline(new OpenCvPipeline() {
            // boolean isPressingA = true;
            // Integer i = 0;

            // @Override
            // public Mat processFrame(Mat input) {
            // if (gamepad1.a && !isPressingA) {
            // isPressingA = true;
            // var bmp = Bitmap.createBitmap(input.cols(), input.rows(),
            // Bitmap.Config.ARGB_8888);
            // Utils.matToBitmap(input, bmp);
            // int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
            // bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
            // bmp.getHeight());
            // print("%d, %d, %d, %d", pixels[1], pixels[20], pixels[50], pixels[700]);
            // var ext = Environment.getExternalStorageDirectory().toString();
            // ++i;
            // var f = new File(ext, i.toString() + ".jpg");
            // try {
            // try (var os = new FileOutputStream(f)) {
            // bmp.compress(Bitmap.CompressFormat.JPEG, 85, os);
            // }
            // } catch (Exception e) {
            // }

            // var results = detector.detect(TensorImage.fromBitmap(bmp));
            // for (var det : results) {
            // print(det.getBoundingBox());
            // for (var cat : det.getCategories())
            // print("%s, %s, %d, %f", cat.getLabel(), cat.getDisplayName(), cat.getIndex(),
            // cat.getScore());
            // }
            // print("----------");
            // } else if (!gamepad1.a && isPressingA) {
            // isPressingA = false;
            // }
            // return input;
            // }
            // });
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
            // throw new InterruptedException();
        } finally {
            waitForStart();
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
