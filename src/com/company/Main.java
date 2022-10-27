package com.company;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    private static BufferedImage contrasted;
    private static BufferedImage source;
    private static BufferedImage test;


    public static void main(String[] args) {
        try {

            File inputImageFile = new File("input.jpg");
            source = ImageIO.read(inputImageFile);
            contrasted = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
            test = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());

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
                /*int blue = color.getBlue();
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
//                    test.setRGB(x,y,Color.WHITE.getRGB());
//                } else if (contrasted.getRGB(x,y)==Color.black.getRGB()){
//                    test.setRGB(x,y,Color.RED.getRGB());
//                } else {
//                    test.setRGB(x,y,Color.WHITE.getRGB());
//                }
//                if(contrasted.getRGB(x,y)==Color.black.getRGB()&&numColocatedPixelsByColor(x,y,Color.black)==4) {
//                    test.setRGB(x,y,Color.WHITE.getRGB());
//                }
//            }
//        }

        for(int x=0;x<contrasted.getWidth();x++){   //черный фильтр
            for(int y=0;y< contrasted.getHeight();y++){
                if(contrasted.getRGB(x,y)==Color.black.getRGB()&&numColocatedPixelsByColor(x,y,Color.black)==0) {
                    test.setRGB(x,y,Color.WHITE.getRGB());
                } else if (contrasted.getRGB(x,y)==Color.black.getRGB()){
                    test.setRGB(x,y,Color.BLACK.getRGB());
                } else {
                    test.setRGB(x,y,Color.WHITE.getRGB());
                }
                if(contrasted.getRGB(x,y)==Color.black.getRGB()&&numColocatedPixelsByColor(x,y,Color.black)==4) {
                    test.setRGB(x,y,Color.BLACK.getRGB());
                }
            }
        }


        File contrastedFile = new File("contrasted.png");
        try (FileOutputStream outputStream = new FileOutputStream(contrastedFile)){
            ImageIO.write(contrasted, "png", outputStream);
        } catch (Exception e){
            System.out.print("Ошибка!\n");
            e.printStackTrace();
        }

        File testFile = new File("test.png");
        try (FileOutputStream outputStream = new FileOutputStream(testFile)){
            ImageIO.write(test, "png", outputStream);
        } catch (Exception e){
            System.out.print("Ошибка!\n");
            e.printStackTrace();
        }

    }

    private static int numColocatedPixelsByColor(int x, int y, Color color){
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
                System.out.println(xLocal + " " + yLocal + "; " + (x+xLocal) + " " + (y+yLocal) + " " + result + "; " + contrasted.getRGB(x + yLocal, y + yLocal) + " = " + color.getRGB());
                xLocal++;
                if (xLocal > 1) {
                    yLocal++;
                    xLocal = -1;
                }
            }
            System.out.println("counter: " + counter);
            return counter;
        } else {
            System.out.println("numColocatedPixelsByColor: out of bounds!");
            return -1;
        }
    }
    public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMWRITE_PNG_STRATEGY_DEFAULT/*CV_LOAD_IMAGE_UNCHANGED*/);
    }
}