package com.ka9mal6t.vws;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImagesToVideo {

    public static void run(String inputFolder, String outputVideo) {
        int frameRate = 30;

        File folder = new File(inputFolder);
        File[] imageFiles = folder.listFiles();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputVideo, 1280, 720);
        recorder.setFrameRate(frameRate);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
        recorder.setVideoQuality(0);
        Java2DFrameConverter converter = new Java2DFrameConverter();

        try {
            recorder.start();

            for (File imageFile : imageFiles) {
                BufferedImage bufferedImage = ImageIO.read(imageFile);
                recorder.record(converter.getFrame(bufferedImage));
            }

            recorder.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}