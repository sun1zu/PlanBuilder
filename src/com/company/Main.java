package com.company;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static int maxCorners = 2000;
    private static BufferedImage contrasted;
    private static BufferedImage source;
    private static BufferedImage clear;
    private static Mat original;
    private static File clearFile = new File("clear.png");


    public static void main(String[] args) throws IOException {
        System.loadLibrary("opencv_java460");
        circleVertexes();
    }

    private static void run() throws IOException {
        try {
            File inputImageFile = new File("input.jpg");        //создаем файлы в буфере
            source = ImageIO.read(inputImageFile);
            contrasted = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
            clear = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());

        } catch (Exception e){
            System.out.print("Ошибка!\n");
            e.printStackTrace();
            return;
        }

        float[] hsv = new float[3];
        for(int x=0;x<source.getWidth();x++){
            //Cycle x

            for(int y=0;y<source.getHeight();y++){  //Cycle y

                Color color = new Color(source.getRGB(x,y));
                Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);
                if(hsv[2] < 0.45f) {
                    contrasted.setRGB(x,y,0);
                }else{
                    contrasted.setRGB(x,y,0xFFFFFFFF);
                }
                /*int blue = color.getBlue();   //старый алгоритм поиска и записи пикселей с цветом оттенка серого
                int red = color.getRed();
                int green = color.getGreen();
                int amp = Math.abs(blue-red)+Math.abs(red-green)+Math.abs(green-blue);

                if(blue<40&&red<40&&green<40 && amp<54){
                    contrasted.setRGB(x,y,new Color(red, green, blue).getRGB());
                }else{
                    contrasted.setRGB(x,y,new Color(255,255,255).getRGB());
                }*/

            }
        }

//        for(int x=0;x<contrasted.getWidth();x++){  //красные контуры
//            for(int y=0;y< contrasted.getHeight();y++){
//                if(contrasted.getRGB(x,y)==Color.black.getRGB()&&numColocatedPixelsByColor(x,y,Color.black)==0) {
//                    clear.setRGB(x,y,Color.WHITE.getRGB());
//                } else if (contrasted.getRGB(x,y)==Color.black.getRGB()){
//                    clear.setRGB(x,y,Color.RED.getRGB());
//                } else {
//                    clear.setRGB(x,y,Color.WHITE.getRGB());
//                }
//                if(contrasted.getRGB(x,y)==Color.black.getRGB()&&numColocatedPixelsByColor(x,y,Color.black)==4) {
//                    clear.setRGB(x,y,Color.WHITE.getRGB());
//                }
//            }
//        }

        for(int x=0;x<contrasted.getWidth();x++){   //черный фильтр (очищаем изображение от лишних пикселей)
            for(int y=0;y< contrasted.getHeight();y++){
                if(contrasted.getRGB(x,y)==Color.black.getRGB()&&numColocatedPixelsByColor(x,y,Color.black)==0) {
                    clear.setRGB(x,y,Color.WHITE.getRGB());
                } else if (contrasted.getRGB(x,y)==Color.black.getRGB()){
                    clear.setRGB(x,y,Color.BLACK.getRGB());
                } else {
                    clear.setRGB(x,y,Color.WHITE.getRGB());
                }
                if(contrasted.getRGB(x,y)==Color.black.getRGB()&&numColocatedPixelsByColor(x,y,Color.black)==4) {
                    clear.setRGB(x,y,Color.BLACK.getRGB());
                }
            }
        }


        File contrastedFile = new File("contrasted.png");   //запись изображения в contrasted
        try (FileOutputStream outputStream = new FileOutputStream(contrastedFile)){
            ImageIO.write(contrasted, "png", outputStream);
        } catch (Exception e){
            System.out.print("Ошибка!\n");
            e.printStackTrace();
        }

        try (FileOutputStream outputStream = new FileOutputStream(clearFile)){      //запись "чистого" изображения (очищенного от лишних пикселей)
            ImageIO.write(clear, "png", outputStream);
        } catch (Exception e){
            System.out.print("Ошибка!\n");
            e.printStackTrace();
        }
        circleVertexes();
    }

    private static void circleVertexes() throws IOException {
        original = Imgcodecs.imread(clearFile.getPath());    //Converting image to mat
        maxCorners = Math.max(maxCorners, 1);

        MatOfPoint corners = new MatOfPoint();
        double qualityLevel = 0.01;
        double minDistance = 10;
        int blockSize = 7, gradientSize = 3;
        boolean useHarrisDetector = false;
        double k = 0.04;
        Mat srcGray = new Mat();
        Imgproc.cvtColor(original, srcGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.goodFeaturesToTrack(srcGray, corners, maxCorners, qualityLevel, minDistance, new Mat(),
                blockSize, gradientSize, useHarrisDetector, k);
        System.out.println("** Number of corners detected: " + corners.rows());
        int[] cornersData = new int[(int) (corners.total() * corners.channels())];
        corners.get(0, 0, cornersData);
        int radius = 2;
        for (int i = 0; i < corners.rows(); i++) {
            Imgproc.circle(original, new Point(cornersData[i * 2], cornersData[i * 2 + 1]), radius,
                    new Scalar(0, 0, 255), Core.FILLED);
        }

        File debugImage = new File("debug-image.png");
        Imgcodecs.imwrite(debugImage.getPath(), original);
    }



    private static int numColocatedPixelsByColor(int x, int y, Color color){    //алгоритм фильтрации изображения от шумов
        if(!(x<1||y<1||x>contrasted.getWidth()-2||y> contrasted.getHeight()-2)) {
            int counter = 0;
            int xLocal = -1;
            int yLocal = -1;

            for (int i = 0; i < 9; i++) {
                boolean result = false;
                if ((contrasted.getRGB(x + yLocal, y + yLocal) == color.getRGB())&&(yLocal!=0&&xLocal!=0)){
                    counter++;
                    result = true;
                }
                System.out.println(xLocal + " " + yLocal + "; " + (x+xLocal) + " " + (y+yLocal) + " " + result + "; " + contrasted.getRGB(x + yLocal, y + yLocal) + " = " + color.getRGB());    //отладка
                xLocal++;
                if (xLocal > 1) {
                    yLocal++;
                    xLocal = -1;
                }
            }
            System.out.println("counter: " + counter);
            return counter;
        } else {
            System.out.println("numCollocatedPixelsByColor: out of bounds!");
            return -1;
        }
    }

//    public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {   //превращаем картинку в Map для операций в opencv
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        ImageIO.write(image, "jpg", byteArrayOutputStream);
//        byteArrayOutputStream.flush();
//        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMWRITE_PNG_STRATEGY_DEFAULT/*CV_LOAD_IMAGE_UNCHANGED*/);
//    }
}