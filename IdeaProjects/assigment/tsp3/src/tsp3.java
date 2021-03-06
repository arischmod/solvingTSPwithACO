/**
 * Created by IntelliJ IDEA.
 * User: aris
 * Date: 3/24/11
 * Time: 4:09 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.File;
import java.util.logging.Level;
import static java.lang.System.*;
import static java.util.logging.Logger.*;

public class tsp3 {

        double bestTourLength;
        static int n=980;
        int city = 0;
        int minCit = 0;
        double[][] coordinates = new double [n][2];
        double[][] distances = new double [n][n];
        int[] edge1 = new int [2];
        int[] edge2 = new int [2];
        int[] edge3 = new int [2];
        int[] edge4 = new int [2];
        int[] maxEdge2 = new int [2];
        int[] tour = new int [n];
        boolean[] visited = new boolean[n];
        double minDis = Integer.MAX_VALUE;
        int gainExit = 1;
        int flagExit = 1;

        public static void main(String[] args) {

            final long startTime = nanoTime();   //calculating run time function

            System.out.println("****   TSP   ****");    // print menu
            System.out.println("enter 1. for lu980");
            System.out.println("enter 2. for n city random tour");

            Scanner in = new Scanner(System.in);
            int selection = in.nextInt();

            if( selection == 1)  //  read the luxemburg citise from file
            {
                File file = new File("lu980.tsp");
                try {
                    //
                    // Create a new Scanner object which will read the data
                    // from the file passed in. To check if there are more
                    // line to read from it we check by calling the
                    // scanner.hasNextLine() method. We then read line one
                    // by one till all line is read.
                    //
                    Scanner instanceScanner = new Scanner(file);

                    int n = instanceScanner.nextInt();
                    double[] x = new double[n];
                    double[] y = new double[n];

                    for (int i = 0; i < n; i++) {
                        int j = instanceScanner.nextInt() - 1;

                        x[j] = instanceScanner.nextDouble();   //reading coordinates
                        y[j] = instanceScanner.nextDouble();
                    }

                    tsp3 start = new tsp3();
                    start.solve(x, y);
                } catch (FileNotFoundException ex) {
                    getLogger(tsp3.class.getName()).log(Level.SEVERE, null, ex);  }
            }
            else if( selection == 2 )     // user chooses the number of cities
            {
                System.out.println("Enter number of cities:");
                n = in.nextInt();

                double[] x = new double[n];
                double[] y = new double[n];

                for (int  i=0 ; i<n; i++)
                {
                    x[i] = (Math.random() * 20000) + 40000;  // random coordinates for each city
                    y[i] = (Math.random() * 1000) + 5000;
                }

                tsp3 start = new tsp3();
                start.solve(x, y);
            }
            else
                System.out.println("Wrong input!");

            final long duration = nanoTime() - startTime;  //  run time
            out.println("run time: "+duration);

        }

        public void solve(double[] x, double[] y) {
            int i;

            fillCoordinates(x, y);
            fillDistances();  // pre-calculate the distances between cities
            tour[n-1]= 1;    // return to start city
            visited[0] = true;

            for(i=1; i<n; i++)   {
                minDis = Integer.MAX_VALUE;
                search(i);  // nearest neighbor algorithm
            }

            bestTourLength=0;
            tourLength();
            out.println("**nearest neighbor:  "+bestTourLength);

            for (i=0; i<n; i++)
                visited[i] = true;

            while (gainExit==1) {
                opt2();  //2-opt algorithm (until there is no improvements)
            }

            bestTourLength=0;
            tourLength();
            out.println("**2-opt: "+bestTourLength);

        }

        public void opt2() {

            gainExit = 0; //flag for while loop
            double maxGain;
            double gain;

            for (int i=1; i<(n-1); i++) {  //for every city try 2-opt
                if (visited[prev(i)] == true)
                {
                    edge1[0] = prev(i);
                    edge1[1] = i;
                    maxGain = 0;
                    maxEdge2[0] = 0;
                    maxEdge2[1] = 0;
                    int j;

                    for (j=i+1; j<(i+201) && j<(n-4); j++) {
                        if ((j != i) && (next(j) != i) && (next(j) != next(i))) {

                            edge2[0] = j;
                            edge2[1] = next(j);
                            edge3[0] = prev(i);
                            edge3[1] = j;
                            edge4[0] = i;
                            edge4[1] = next(j);
                                                                 //  calculate if there is gain
                            gain = ( edgeLength(edge1) + edgeLength(edge2) ) - ( edgeLength(edge3) + edgeLength(edge4) );

                            if (gain > maxGain) {    // find the most gainful move
                                maxGain = gain;
                                maxEdge2[0] = edge2[0];
                                maxEdge2[1] = edge2[1];
                            }
                        } //end if
                    } //end for j

                    if (maxGain > 0 ) {     // if there is gain make the 2-opt move (flip 2 cities)
                        int u;
                        int v;
                        for ( u = maxEdge2[0], v = edge1[1]; u>=edge1[1];  u--, v++)
                        {
                            if (v < u) {        //  change also the order of the cities (u--)
                                flip(v, u);
                                visited[edge1[0]] = true;
                                visited[edge1[1]] = true;
                                visited[edge2[0]] = true;
                                visited[edge2[1]] = true;
                                visited[edge3[0]] = true;
                                visited[edge3[1]] = true;
                                visited[edge4[0]] = true;
                                visited[edge4[1]] = true;
                                gainExit = 1; }
                        }
                    } //end if
                    else
                        visited[edge1[0]] = false;
                }
            }// end for i
        }// end opt


        public void flip (int a, int b)       // flip 2 cities in the tour array
        {
            int temp;

            temp = tour[a];
            tour[a] = tour[b];
            tour[b] = temp;
        }


        public double edgeLength(int[] edge)  //  calculate the length of one edge
        {
           return distances[tour[edge[0]]][tour[edge[1]]];
        }

        public int next( int v) // get the city next to v
        {
            return v+1;
        }

        public int prev( int v)  // get the city before v
        {
            return v-1;
        }

        public void search(int i)  // find the nearest neighbor
        {

            for (int j=0; j < n; j++) {
                if ( city==j )
                    continue;
                else if (visited[j] == false && distances[city][j] < minDis) {
                    minDis = distances[city][j];
                    minCit=j;
                }
            }
            city = minCit;
            tour[i-1]=city;
            visited[city] = true;
        }

        public void fillDistances() {
            for ( int i=0; i<n; i++)
                for ( int j=0; j<n; j++) {
                    double dx = coordinates[i][0] - coordinates[j][0], dy = coordinates[i][1] - coordinates[j][1];
                    distances[i][j] = (Math.sqrt(dx * dx + dy * dy) + 0.5);
                }
        }

        public void fillCoordinates (double[] x, double[] y) {
            for (int i=0; i<n; i++) {
                coordinates[i][0] = x[i];
                coordinates[i][1] = y[i];
            }
        }

        public void tourLength() {   // calculate best tour length
            for(int i=0; i<(n-1); i++) {
                bestTourLength += distances[tour[i]][tour[i+1]];
            }
        }

} // end of tsp3

