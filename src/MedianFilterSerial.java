import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Scanner;
import java.util.ArrayList;

public class MedianFilterSerial
{
    public static void main(String[] args) throws IOException 
    {
        Scanner input = new Scanner(System.in);

        File file, firstImageFile;
        int windowWidth;
        
        String imageOut = null;
        
        if (args.length == 0) 
        {
            String fName = input.nextLine();
            String[] arg = fName.split(" ");

            firstImageFile = new File(arg[0]);
            imageOut = arg[1];
            windowWidth = Integer.parseInt(arg[2]);
            
        } 
        else 
        {
            firstImageFile = new File(args[0]);
            imageOut = args[1];
            windowWidth = Integer.parseInt(args[2]);
        }
        
        BufferedImage firstImage = ImageIO.read(firstImageFile);
        BufferedImage secondImage = ImageIO.read(firstImageFile);
        
        int imageWidth = firstImage.getWidth();
        int imageHeight = firstImage.getHeight();
        
        ArrayList<Integer> red = new ArrayList<Integer>();
        ArrayList<Integer> blue = new ArrayList<Integer>();
        ArrayList<Integer> green = new ArrayList<Integer>();
        
        int r = 0; 
        int g = 0;
        int b = 0;
        int po = 0;
        
        int redAvg, greenAvg, blueAvg;
        
        if (windowWidth >= 3 && windowWidth % 2 == 1) 
        {
            long start = System.currentTimeMillis();
            int boarder = windowWidth / 2;
            
            int total = (int) Math.pow(windowWidth, 2) - 1;
            
            int window_yxis = -1;
            int window_xxis;
            int endyxis;
            int endiyxis;

            int mid = (total - 1) / 2;


            for (int y_axis = boarder; y_axis < imageHeight - boarder; y_axis++) {
                window_yxis++;//window Index for y-axis. for (width * width)
                window_xxis = -1;

                for (int x_axis = boarder; x_axis < imageWidth - boarder; x_axis++) {

                    window_xxis++; //window Index for x-axis. for (width * width)

                    endyxis = windowWidth + window_yxis;
                    
                    red.clear();
                    green.clear();
                    blue.clear();
                    
                    po = 0;
                    r = 0;
                    g = 0;
                    b = 0;
                    
                    /*
                     * Loop through the neigbouring coordinates of the pixel taht needs to be filtered
                     */
                    for (int k = window_yxis; k < endyxis; k++) 
                    {
                       
                        endiyxis = windowWidth + window_xxis;
                        
                        for (int i = window_xxis; i < endiyxis; i++) 
                        {
                            po = firstImage.getRGB(i, k);
                            r = ((po >> 16) & 0xff);
                            g = ((po >> 8) & 0xff);
                            b = (po & 0xff);

                            red.add(r);
                            green.add(g);
                            blue.add(b);

                        }

                    }

                    Collections.sort(red);
                    Collections.sort(green);
                    Collections.sort(blue);
                    
                    redAvg = red.get(mid);
                    greenAvg = green.get(mid);
                    blueAvg = blue.get(mid);

                    po = (redAvg << 16) | (greenAvg << 8) | blueAvg;
                    secondImage.setRGB(x_axis, y_axis, po);

                }

            }
   
            file = new File(imageOut);
            ImageIO.write(secondImage, "jpg", file);
            System.out.println("Done filtering");

            long end = System.currentTimeMillis();

            long runTime = end - start;
            
            System.out.println("Image meanFilterSerial took " + ((double)runTime / 1000) + " seconds" );
            
        } 
        else 
        {
            System.out.println("Window width must be gretaer than or equal to 3 and odd");
        }
    }

}