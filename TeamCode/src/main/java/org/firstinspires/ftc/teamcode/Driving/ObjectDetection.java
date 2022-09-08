package org.firstinspires.ftc.teamcode.Driving;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.openftc.easyopencv.OpenCvWebcam;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions;
import org.tensorflow.lite.support.image.TensorImage;
import org.opencv.android.Utils;
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

        telemetry.setAutoClear(false);
        try {
            BaseOptions baseOptions = BaseOptions.builder().build();
            ObjectDetectorOptions options = ObjectDetectorOptions.builder().setBaseOptions(baseOptions).build();
            ObjectDetector detector = ObjectDetector.createFromFileAndOptions(hardwareMap.appContext, "UltimateGoal.tflite",
                    options);

            int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id",
                    hardwareMap.appContext.getPackageName());
            WebcamName webcamName = hardwareMap.get(WebcamName.class, "cv");
            OpenCvWebcam camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName, cameraMonitorViewId);
            camera.openCameraDevice();

            camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);

            waitForStart();

            camera.setPipeline(new OpenCvPipeline() {
                boolean isPressingA = true;
                Integer i = 0;

                @Override
                public Mat processFrame(Mat input) {
                    if (gamepad1.a && !isPressingA) {
                        isPressingA = true;
                        Bitmap bmp = Bitmap.createBitmap(input.cols(), input.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(input, bmp);
                        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
                        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
                        String ext = Environment.getExternalStorageDirectory().toString();
                        i++;
                        File f = new File(ext, i.toString() + ".jpg");
                        try {
                            try (FileOutputStream os = new FileOutputStream(f)) {
                                bmp.compress(Bitmap.CompressFormat.JPEG, 85, os);
                            }
                        } catch (Exception e) {
                        }

                        List<Detection> results = detector.detect(TensorImage.fromBitmap(bmp));
                        for (Detection det : results) {
                            print(det.getBoundingBox());
                            for (Category cat : det.getCategories())
                                print("%s, %s, %d, %f", cat.getLabel(), cat.getDisplayName(), cat.getIndex(),
                                        cat.getScore());
                        }
                        print("----------");
                    } else if (!gamepad1.a && isPressingA) {
                        isPressingA = false;
                    }
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
            StringWriter writer = new StringWriter();
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
