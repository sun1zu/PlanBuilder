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

    private static void circleVertexes() throws IOException {   //TODO: make a point on each vertex on the contour
        //Finding vertexes!
        //Loading the image
        //Non-working method to convert the BufferedImage to Mat
//        try {
//            original = BufferedImage2Mat(clear);
//        } catch (IOException e){
//            e.printStackTrace();
//        }

        original = Imgcodecs.imread(clearFile.getPath());    //Converting image to mat

        Mat originalGray = new Mat();
        Imgproc.cvtColor(original, originalGray, Imgproc.COLOR_BGR2GRAY);

        //Threshold the image
        Mat threshold = new Mat();
        Imgproc.threshold(original, threshold, 127, 125, 1);

        //Canny
        Mat cannyOutput = new Mat();
        Imgproc.Canny(originalGray, cannyOutput, 100, 200);     //Canny - edge-detection algorithm

        //Find the contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
//        Imgproc.findContours(threshold, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);   //old method to find contours
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        //Drawing contours on the image
        Mat contourImg = new Mat();
        for(int i=0; i < contours.size(); i++){
            Imgproc.drawContours(contourImg, contours, i, new Scalar(255, 255, 255), -1);
        }

        // Get contour index with largest area
        double max_area = -1;
        int index = 0;
        for(int i=0; i< contours.size();i++) {
            if (Imgproc.contourArea(contours.get(i)) > max_area) {
                max_area = Imgproc.contourArea(contours.get(i));
                index = i;
            }
        }

        // Approximate the largest contour
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint2f oriCurve = new MatOfPoint2f( contours.get(index).toArray() );
        Imgproc.approxPolyDP(oriCurve, approxCurve, 6.0, true);

        // Draw contour points on the original image
        Point[] array = approxCurve.toArray();
        for(int i=0; i < array.length;i++) {
            Imgproc.circle(original, array[i], 2, new Scalar(0, 0 ,255), 5);
            System.out.println("Point " + i);
        }

        File debugImage = new File("debug-image.png");
        Imgcodecs.imwrite(debugImage.getPath(), original);
    }



    private static int numColocatedPixelsByColor(int x, int y, Color color){    //метод, который выдает количество пикселей заданного цвета вокруг пикселя на изображени по его координатам
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