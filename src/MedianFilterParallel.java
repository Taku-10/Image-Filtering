import java.util.Scanner;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;
import java.util.Collections;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;

//import static java.awt.SystemColor.window;

public class MedianFilterParallel extends RecursiveAction 
{
    private static BufferedImage firstImage;
    private static int   sum;
    private int windowWidth;
    private int start_x_axis;
    private int end_x_axis;
    private int start_y_axis;
    private int end_y_axis;
    private static BufferedImage secondImage;
    private static final int sequential_CutOff = 10000;
    
    public MedianFilterParallel(int start_y_axis, int start_x_axis, int end_y_axis, int end_x_axis, int windowWidth) 
    {
        this.start_y_axis = start_y_axis;
        this.start_x_axis = start_x_axis;
        this.end_y_axis = end_y_axis;
        this.end_x_axis = end_x_axis;
        this.windowWidth = windowWidth;

    }
    

    protected void computeDirectly() 
    
    {

        int window_y_axis = start_y_axis - ((windowWidth - 1) / 2) - 1;//to start before the boundary pixels with surroundings
        int window_x_axis;
        int end_y; 
        int end_x; 
        
        int height = secondImage.getHeight();
        int width = secondImage.getWidth();
        
        int mid = (sum - 1) / 2;
        int mid2 = mid + 1;
        
        for (int yxis = start_y_axis; yxis < end_y_axis; yxis++)
        {
            window_y_axis++;
            window_x_axis = start_x_axis - ((windowWidth - 1) / 2) - 1; 

            for (int xis = start_x_axis; xis < end_x_axis; xis++)
            {

                window_x_axis++; 
                
                end_y = windowWidth + window_y_axis;
                
                ArrayList<Integer> red = new ArrayList<Integer>();
                ArrayList<Integer> green = new ArrayList<Integer>();
                ArrayList<Integer> blue = new ArrayList<Integer>();
                
                int r = 0, g = 0, b = 0;
                int pos = 0;
                
                /*
                 * Loop through the image to find the neigbouring pixels of the current pixel that need to be
                 * filtred
                 */
                for (int j = window_y_axis; j < end_y; j++)
                {
                   
                    end_x = windowWidth + window_x_axis;

                    for (int k = window_x_axis; k < end_x; k++) {

                        if (xis != k || yxis != j)
                        {
                        	//to check if the window coordinates are within range
                            if (k < width && j < height) 
                            {
                                
                                pos = MedianFilterParallel.secondImage.getRGB(k, j);
                                r = ((pos >> 16) & 0xff);
                                g = ((pos >> 8) & 0xff);
                                b = (pos & 0xff);

                                red.add(r);
                                green.add(g);
                                blue.add(b);

                            }
//
                        }

                    }

                }

                Collections.sort(red);
                Collections.sort(green);
                Collections.sort(blue);
                
                int redAvg = (red.get(mid) + red.get(mid2)) / 2;
                int greenAvg = (green.get(mid) + green.get(mid2)) / 2;
                int blueAvg = (blue.get(mid) + blue.get(mid2)) / 2;

                pos = (redAvg << 16) | (greenAvg << 8) | blueAvg;
                
                MedianFilterParallel.firstImage.setRGB(xis, yxis, pos);

            }
        }

    }

    
    @Override
    protected void compute()
    {
        //endY is the end y coordinate of a pixel of the image which can be filtered
        //startY is the start y coordinate of a pixel of the image which can be filtered
        //startX is the start x coordinate of a pixel of the image which can be filtered
        //endX is the end x coordinate of a pixel of the image which can be filtered

        int imageHeight = end_y_axis - start_y_axis;//height
        int imageWidth = end_x_axis - start_x_axis;//width

        int tPixels = imageHeight * imageWidth;

        if (tPixels < sequential_CutOff) {
            computeDirectly();
            return;
        }
        
        int eighth = (((end_x_axis - start_x_axis) / 8) + start_x_axis);
        int quarter = (((2 * (end_x_axis - start_x_axis)) / 8) + start_x_axis);;
        int third = (((3 * (end_x_axis - start_x_axis)) / 8) + start_x_axis);
        int half = (((4 * (end_x_axis - start_x_axis)) / 8) + start_x_axis);
        int fifth = (((5 * (end_x_axis - start_x_axis)) / 8) + start_x_axis);
        int sixth = (((6 * (end_x_axis - start_x_axis)) / 8) + start_x_axis);
        int seventh = (((7 * (end_x_axis - start_x_axis)) / 8) + start_x_axis);
        
        
        MedianFilterParallel mnf = new MedianFilterParallel(start_y_axis, start_x_axis, end_y_axis, eighth, windowWidth);
        MedianFilterParallel mnf1 = new MedianFilterParallel(start_y_axis, eighth, end_y_axis, quarter, windowWidth);
        MedianFilterParallel mnf2 = new MedianFilterParallel(start_y_axis, quarter, end_y_axis, third, windowWidth);
        MedianFilterParallel mnf3 = new MedianFilterParallel(start_y_axis, third, end_y_axis, half, windowWidth);
        MedianFilterParallel mnf4 = new MedianFilterParallel(start_y_axis, half, end_y_axis, fifth, windowWidth);
        MedianFilterParallel mnf5 = new MedianFilterParallel(start_y_axis, fifth, end_y_axis, sixth, windowWidth);
        MedianFilterParallel mnf6 = new MedianFilterParallel(start_y_axis, sixth, end_y_axis, seventh, windowWidth);
        MedianFilterParallel mnf7 = new MedianFilterParallel(start_y_axis, seventh, end_y_axis, end_x_axis, windowWidth);

        invokeAll(mnf, mnf1, mnf2, mnf3, mnf4, mnf5, mnf6, mnf7);

    }

    public static void main(String[] args) throws IOException, InterruptedException 
    {
        Scanner input = new Scanner(System.in);

        String imageOut;
        int windowWidth;
        File firstImageFile;
       
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
        
        BufferedImage imageWidth = ImageIO.read(firstImageFile);
        BufferedImage secondImage = ImageIO.read(firstImageFile);
        
        if (windowWidth > 2 && windowWidth % 2 == 1)
        {

            int imageHeight = imageWidth.getHeight();
            int w = imageWidth.getWidth();
            int tPixels = imageHeight * w;

            int proc = Runtime.getRuntime().availableProcessors();
            System.out.println("Available processors: " + proc);
            
            // Find the region of the pixel that can be filtered
            int boarder = (windowWidth - 1) / 2;
            int y_axis_start = boarder;
            int y_axis_end = imageHeight - boarder;
            int x_axis_start = boarder;
            int x_axis_end = w - boarder;
            
            MedianFilterParallel.firstImage = secondImage;
            MedianFilterParallel.secondImage = imageWidth;
            MedianFilterParallel.sum= (int) (Math.pow(windowWidth, 2) - 1);

            MedianFilterParallel medianFilter = new MedianFilterParallel(y_axis_start, x_axis_start, y_axis_end, x_axis_end, windowWidth);

            ForkJoinPool pool = new ForkJoinPool();

            long start = System.currentTimeMillis();

            pool.invoke(medianFilter);

            File file = new File(imageOut);
            ImageIO.write(MedianFilterParallel.firstImage, "jpg", file);
            System.out.println("Image Filtering done");

            long end = System.currentTimeMillis();
            
            long runTime = end - start;

            System.out.println("Median Image Parallel Filtering took " + ((double) runTime/1000) +" seconds for the window width " + windowWidth);

        } 
        else 
        {
            System.out.println("Window width must be greater than or equal to 3 and odd");
        }


    }

  
}