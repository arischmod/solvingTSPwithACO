import java.io.File;
import java.util.Scanner;

/**
 * Created by IntelliJ IDEA.
 * User: Asger
 * Date: 11-05-11
 * Time: 18:57
 * To change this template use File | Settings | File Templates.
 */
public class DrawTour {

/* Written by Keld Helsgaun, November 2010 */

    /** Draws a tour after a given time delay
     * @param t the tour (a permutation of the integers 0 to n-1)
     * @param x x-coordinates of the cities
     * @param y y-coordinates of the cities
     * @param delay the delay (in milliseconds)
     */


    public static void draw(int[] t, double[] x, double[] y, int delay) {
        int n = x.length;
            double xMin = x[0], xMax = x[0], yMin = y[0], yMax = y[0];
            for (int i = 1; i < n; i++) {
                xMin = Math.min(xMin, x[i]);
                xMax = Math.max(xMax, x[i]);
                yMin = Math.min(yMin, y[i]);
                yMax = Math.max(yMax, y[i]);
            }
            final int size = 800;
            int width = size;
            int height = (int) (width * (yMax - yMin) / (xMax - xMin));
            if (height > size) {
                width *= ((double) size) / height;
                height = size;
            }
            StdDraw.setCanvasSize(width, height);
            StdDraw.show(0);
            StdDraw.clear();
            StdDraw.setPenColor();
            StdDraw.setPenRadius();
            StdDraw.setXscale(xMin, xMax);
            StdDraw.setYscale(yMin, yMax);
            if (t != null) {
                for (int i = 1; i < t.length; i++)
                    StdDraw.line(x[t[i - 1]], y[t[i - 1]], x[t[i]], y[t[i]]);
                StdDraw.line(x[t[n - 1]], y[t[n - 1]], x[t[0]], y[t[0]]);
            }
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.setPenRadius(0.005);
            for (int i = 0; i < n; i++)
                StdDraw.point(x[i], y[i]);
            StdDraw.show(delay);
    }


        /** Draws a tour without delay */
        public static void draw(int[]t, double[]x, double[]y) {
            draw(t, x, y, 0);
        }

        /** Plots cities */
        public static void draw(double[]x, double[]y) {
            draw(null, x, y, 0);
        }

        public static void main(String[]args) throws Exception {
            if (args.length != 2) {
                System.out.
                    println("Usage: java DrawTour instance_file tour_file");
                System.exit(1);
            }
            Scanner instanceScanner = new Scanner(new File(args[0]));
            int n = instanceScanner.nextInt();
            double[] x = new double[n], y = new double[n];
            for (int i = 0; i < n; i++) {
                int j = instanceScanner.nextInt() - 1;
                /* Swap x and y for geographical instances */
                y[j] = instanceScanner.nextDouble();
                x[j] = instanceScanner.nextDouble();
            }
            Scanner tourScanner = new Scanner(new File(args[1]));
            int[] tour = new int[n];
            for (int i = 0; i < n; i++)
                tour[i] = tourScanner.nextInt() - 1;
            draw(tour, x, y, 0);
        }
}
