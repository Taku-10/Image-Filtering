import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.Color;
import java.util.Scanner;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MeanFilterParallel extends RecursiveAction 
{
	
    private static final int sequential_Cut_Off = 10_000;
    private final int windowWidth;
    private final int start_y_axis;
    private final int end_y_axis;
    private final int start_x_axis;
    private final int end_x_axis;
    public static BufferedImage img;
    public static BufferedImage img1;
    
    public MeanFilterParallel(int start_y_axis, int start_x_axis, int end_y_axis, int end_x_axis, int window_width) 
    {
        this.windowWidth = window_width;
        this.start_y_axis = start_y_axis;
        this.start_x_axis = start_x_axis;
        this.end_y_axis = end_y_axis;
        this.end_x_axis = end_x_axis;
    }

    @Override
    protected void compute() 
    
    {
        
    	// Width ofn the image of the area that has to be filtred
        int width = end_x_axis - start_x_axis;
        int height = end_y_axis - start_y_axis;
        
        int tPixels = height * width;
        
        if (tPixels < 100000) 
        {
            computeDirectly();
            return;
            
        } 
        else 
        {
        	// Divide the image
            int eighth = (((end_x_axis - start_x_axis) / 8) + start_x_axis);
            int quarter = (((2*(end_x_axis - start_x_axis)) / 8) + start_x_axis);;
            int third =(((3*(end_x_axis - start_x_axis)) / 8) + start_x_axis);
            int half =(((4*(end_x_axis - start_x_axis)) / 8) + start_x_axis);
            int fifth =(((5*(end_x_axis - start_x_axis)) / 8) + start_x_axis);
            int sixth =(((6*(end_x_axis - start_x_axis)) / 8) + start_x_axis);
            int seventh =(((7*(end_x_axis - start_x_axis)) / 8) + start_x_axis);
            
           // Create my 8 threads
            MeanFilterParallel t1 = new MeanFilterParallel(start_y_axis, start_x_axis, end_y_axis, eighth, windowWidth);
            MeanFilterParallel t2 = new MeanFilterParallel(start_y_axis, eighth , end_y_axis, quarter, windowWidth);
            MeanFilterParallel t3 = new MeanFilterParallel(start_y_axis, quarter , end_y_axis, third, windowWidth);
            MeanFilterParallel t4 = new MeanFilterParallel(start_y_axis, third , end_y_axis, half, windowWidth);
            MeanFilterParallel t5 = new MeanFilterParallel(start_y_axis,half, end_y_axis, fifth, windowWidth);
            MeanFilterParallel t6 = new MeanFilterParallel(start_y_axis, fifth , end_y_axis, sixth, windowWidth);
            MeanFilterParallel t7 = new MeanFilterParallel(start_y_axis, sixth , end_y_axis, seventh, windowWidth);
            MeanFilterParallel t8 = new MeanFilterParallel(start_y_axis, seventh , end_y_axis, end_x_axis, windowWidth);

            invokeAll(t1, t2,t3,t4,t5,t6,t7,t8);
        }
    }
    

    public static void main(String[] args) throws IOException, InterruptedException 
    {
    	
        Scanner input = new Scanner(System.in);
        
        File firstImageFile;
        
        int windowWidth;
        String imageOut;
        
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
        
        if (windowWidth > 2 && windowWidth % 2 == 1) 
        {
            blurImage(firstImage, secondImage, imageOut, windowWidth);
            
        } 
        else 
        {
            System.out.println("Window width must be greater than or equal to 3 and odd");
        }

    }

    
    protected void computeDirectly() 
    
    {
        int total = (int)Math.pow(windowWidth, 2) - 1;
        
        int winY = start_y_axis - (windowWidth - 1) / 2 - 1;
        int imageHeight = img.getHeight();
        int imageWidth = img.getWidth();

        for(int yxis = start_y_axis; yxis < end_y_axis; ++yxis) 
        {
            ++winY;
            int winX = start_x_axis - (windowWidth - 1) / 2 - 1;

            for(int xxis = start_x_axis; xxis < end_x_axis; ++xxis) {
                ++winX;
                int endY = windowWidth + winY;


                int tRed = 0, tGreen = 0, tBlue = 0;
                
                int poss=0;
                
                for(int k = winY; k < endY; ++k) 
                {
                    int endiIndex = windowWidth + winX;

                    for(int i = winX; i < endiIndex; ++i) 
                    {
                        if ((xxis != i || yxis != k) && i < imageWidth && k < imageHeight) 
                        {
                            poss = MeanFilterParallel.img.getRGB(i, k);
                            tRed += ((poss >> 16) & 0xff);
                            tGreen += ((poss >> 8)&0xff);
                            tBlue += ((poss & 0xff));
                        }
                    }
                }

                int cRed = tRed / total;
                int cGreen = tGreen / total;
                int cBlue = tBlue / total;
                poss=(cRed << 16) |(cGreen << 8) | cBlue;

                img1.setRGB(xxis, yxis, poss);
            }
        }

    }

 

    public static void blurImage (BufferedImage srcImage, BufferedImage copyImage, String imOut, int windowWidth) throws IOException, InterruptedException 
    {
    	
        int height = srcImage.getHeight();
        int width = srcImage.getWidth();
        int tPixels = height * width;
       
        int proc = Runtime.getRuntime().availableProcessors();
        
        System.out.println("Available processors: " + proc);
        
        int boarder = (windowWidth - 1) / 2;
        int endY = height - boarder;
        int endX = width - boarder;
        
        img1 = copyImage;
        img = srcImage;
        
        MeanFilterParallel mean = new MeanFilterParallel(boarder, boarder, endY, endX, windowWidth);
        ForkJoinPool pool = new ForkJoinPool();
        
        long start = System.currentTimeMillis();
        pool.invoke(mean);
        
        File file = new File(imOut);
        ImageIO.write(img1, "jpg", file);
        System.out.println("Image filtering done");
        
        long end = System.currentTimeMillis();
        
        long runTime = end - start;
        
        System.out.println("Mean image filtering parallel took" + ((double) runTime / 1000) + " seconds.");
    }
}