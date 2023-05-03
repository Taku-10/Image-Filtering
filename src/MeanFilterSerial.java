import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;
import java.awt.*;

public class MeanFilterSerial
{
	
    public static void main(String[] args) throws IOException
    
{
        Scanner input = new Scanner(System.in);

        File file, imgFile;
        int windowWidth;
        String imageOut;
        
        if (args.length == 0) 
        {
            String fName = input.nextLine();
            String[] arg = fName.split(" ");

            imgFile = new File(arg[0]);
            imageOut = arg[1];
            windowWidth = Integer.parseInt(arg[2]);
            
        } 
        else 
        {
            imgFile = new File(args[0]);
            imageOut = args[1];
            windowWidth = Integer.parseInt(args[2]);
        }
        
        // Read Image
        BufferedImage fImage = ImageIO.read(imgFile);
        BufferedImage secondImage = ImageIO.read(imgFile);
        
        // Check if the window length is valid and if so get image measurements
        
        if (windowWidth >= 3 && windowWidth % 2 == 1) 
        	
        {
            int imageWidth = fImage.getWidth();
            int imageHeight = fImage.getHeight();
            
            int boarder = windowWidth / 2;
            int total = (int) Math.pow(windowWidth, 2) - 1;

            int window_y = -1;
            int window_x = -1;
            
            int end_x;
            int end_y;
            
            long start = System.currentTimeMillis();

            for (int yxis = boarder; yxis < imageHeight - boarder; yxis++) 
            {
            	
                window_y++;
                window_x = -1;

                for (int xxis = boarder; xxis < imageWidth - boarder; xxis++) 
                {

                    window_x++; 

                    
                    end_x = windowWidth + window_y;
                    int totalRed = 0, totalGreen = 0, totalBlue = 0; 
                    int poss = 0;
                    
                    /*
                     * Loop through to find the neigbouring coordinates of the current pixel that needs
                     * to be filtered
                     */
                    for (int j = window_y; j < end_x; j++) {
                        
                        end_y = windowWidth + window_x;
                        
                        for (int i = window_x; i < end_y; i++) {

                            if (xxis != i || yxis != j) {
                              
                                    poss = fImage.getRGB(i, j);
                                    totalRed += ((poss>>16)&0xff);
                                    totalGreen += ((poss>>8)&0xff);
                                    totalBlue += ((poss&0xff));
                             

                            }

                        }

                    }


                    int red = totalRed / total;
                    int green = totalGreen / total;
                    int blue = totalBlue / total;
                    
                    poss = (red<<16) |(green<<8)|blue;

                    secondImage.setRGB(xxis, yxis, poss);

                }

            }
            
            file = new File(imageOut);
            ImageIO.write(secondImage, "jpg", file);
            
            System.out.println("Image Filtering done");
            
            long end = System.currentTimeMillis();
            
            long runTime = end - start;

            System.out.println("Mean image filtering serial took " + ((double) runTime/ 1000)+ " seconds");
            
        }
        
        else
        	
         {
            System.out.println("Window width must be an odd number and greater than or equal to 3");
        }
    }

}