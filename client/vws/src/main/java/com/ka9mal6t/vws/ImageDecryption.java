package com.ka9mal6t.vws;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageDecryption {

    public static String decrypt(String origFile2, String encFile) throws IOException {
        BufferedImage im2 = ImageIO.read(new File(origFile2));
        BufferedImage im3 = ImageIO.read(new File(encFile));

        int width = im2.getWidth();
        int height = im2.getHeight();

        int[][] code = {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},

                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},

                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},

                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},

                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},

                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},

                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},

                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        };



        int k = 0;
        List<Integer> data = new ArrayList<>();
        int countBlock = 0;
        List<Integer> arr = new ArrayList<>();
        int count = 0;

        for (int i = 0; i < width - 31; i += 32) {
            for (int j = 0; j < height - 31; j += 32) {
                int proxima = 0;
                for (int l1 = 0; l1 < 32; l1++) {
                    for (int l2 = 0; l2 < 32; l2++) {
                        int[] rgb2 = getRGB(im2.getRGB(i + l1, j + l2));
                        int[] rgb3 = getRGB(im3.getRGB(i + l1, j + l2));
                        int[] yCbCr2 = convertRGBtoYCbCr(rgb2);
                        int[] yCbCr3 = convertRGBtoYCbCr(rgb3);
                        proxima += code[l1][l2] * (yCbCr3[0] - yCbCr2[0]);
                    }
                }
                if (proxima >= 0) {
                    arr.add(1);
                    countBlock++;
                } else {
                    arr.add(-1);
                    countBlock++;
                }
                if (countBlock == 31) {
                    countBlock = 0;
                    if (countOnes(arr) > countNegOnes(arr)) {
                        data.add(1);
                    } else {
                        data.add(-1);
                    }
                    count++;
                    arr.clear();
                }
                if (data.size() == 24) {
                    break;
                }
            }
            if (data.size() == 24) {
                break;
            }
        }



        StringBuilder newData = new StringBuilder();
        for (int value : data) {
            newData.append(value == 1 ? '1' : '0');
        }

        StringBuilder finalData = new StringBuilder();
        for (int i = 0; i < newData.length(); i += 8) {
            String byteStr = newData.substring(i, i + 8);
            finalData.append((char) Integer.parseInt(byteStr, 2));
        }

        return finalData.toString();
    }

    private static int[] getRGB(int pixel) {
        int[] rgb = new int[3];
        rgb[0] = (pixel >> 16) & 0xff; // red
        rgb[1] = (pixel >> 8) & 0xff;  // green
        rgb[2] = pixel & 0xff;         // blue
        return rgb;
    }

    private static int[] convertRGBtoYCbCr(int[] rgb) {
        int R = rgb[0];
        int G = rgb[1];
        int B = rgb[2];

        int Y = (int) (0.299 * R + 0.587 * G + 0.114 * B);
        int Cb = (int) ((B - Y) * 0.564 + 128);
        int Cr = (int) ((R - Y) * 0.713 + 128);

        return new int[]{Y, Cb, Cr};
    }
    private static int convertYCbCrtoRGB(int[] ycbcr) {
        int Y = ycbcr[0];
        int Cb = ycbcr[1];
        int Cr = ycbcr[2];

        int R = (int) (Y + 1.402 * (Cr - 128));
        int G = (int) (Y - 0.344136 * (Cb - 128) - 0.714136 * (Cr - 128));
        int B = (int) (Y + 1.772 * (Cb - 128));

        // Clip the values to [0, 255]
        R = Math.min(255, Math.max(0, R));
        G = Math.min(255, Math.max(0, G));
        B = Math.min(255, Math.max(0, B));

        return (R << 16) | (G << 8) | B;
    }

    private static int countOnes(List<Integer> list) {
        return (int) list.stream().filter(val -> val == 1).count();
    }

    private static int countNegOnes(List<Integer> list) {
        return (int) list.stream().filter(val -> val == -1).count();
    }

    public static String allDecrypt(String directory) throws IOException {
        List<String> pngFiles = new ArrayList<>();
        String info = "";

        for (String filename : new File(directory).list()) {
            if (filename.toLowerCase().endsWith(".png")) {
                pngFiles.add(filename);
            }
        }

        if (pngFiles.size() > 0 && pngFiles.size() % 2 == 1) {
            pngFiles.remove(pngFiles.size() - 1);
        }

        for (int i = 0; i < pngFiles.size(); i += 2) {
            info += decrypt(directory + pngFiles.get(i), directory + pngFiles.get(i + 1));
            if (info.contains("01234")) {
                break;
            }
        }

        return info.substring(0, info.length() - 6);
    }
}
