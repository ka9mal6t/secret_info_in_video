package com.ka9mal6t.vws;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

public class VideoToFrames {

    public static void run(String videoPath, String outputFolder) {

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
        try {
            grabber.start();

            int frameNumber = 100000;
            Frame frame;

            while ((frame = grabber.grab()) != null) {
                String outputFilePath = outputFolder + "frame" + frameNumber + ".png";
                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                Mat mat = converter.convert(frame);

                if (mat != null) {
                    imwrite(outputFilePath, mat);
                }

                frameNumber++;
            }

            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception: " + e.getMessage());
        }

    }
}
