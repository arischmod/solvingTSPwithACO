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

public class tsp4 {

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

                    tsp4 start = new tsp4();
                    start.solve(x, y);
                } catch (FileNotFoundException ex) {
                    getLogger(tsp4.class.getName()).log(Level.SEVERE, null, ex);  }
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

                tsp4 start = new tsp4();
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

            while (flagExit==1) {
                opt4();   //2-opt algorithm (until there is no improvements)
            }

            bestTourLength=0;
            tourLength();
            out.println("**4-opt: "+bestTourLength);
        }

        public void opt4() {

            int i, j, flag = 1;
            bestTourLength=0;
            tourLength();
            double gainTourLength = bestTourLength;
            int[] randCity = new int [4];

            flagExit = 0;    // exit if there is no 4-opt move

            for(j = 0; j<n; j++) {
                //choose 4 random cities
                int randCity1 = (int) (Math.random() *  (n-1));
                int randCity2 = (int) (Math.random() *  (n-1));
                int randCity3 = (int) (Math.random() *  (n-1));
                int randCity4 = (int) (Math.random() *  (n-1));

                randCity[0] = randCity1;
                randCity[1] = randCity2;
                randCity[2] = randCity3;
                randCity[3] = randCity4;

                while (flag==1){
                    flag = 0;
                    for (i=0; i<3; i++)
                    {
                        if (randCity[i]>randCity[i+1])
                        {
                            int temp = randCity[i];
                            randCity[i] = randCity[i+1];
                            randCity[i+1] = temp;
                            flag = 1;
                        }
                    }
                }

                edge1[0] = randCity[0];
                edge1[1] = next(randCity[0]);
                edge2[0] = randCity[1];
                edge2[1] = next(randCity[1]);
                edge3[0] = randCity[2];
                edge3[1] = next(randCity[2]);
                edge4[0] = randCity[3];
                edge4[1] = next(randCity[3]);

                if (randCity1 != randCity2 && next(randCity1) != randCity2
                    && randCity1 != randCity3 && next(randCity1) != randCity3
                    && randCity1 != randCity4 && next(randCity1) != randCity4
                    && next(randCity2) != randCity1
                    && randCity2 != randCity3 && next(randCity2) != randCity3
                    && randCity2 != randCity4 && next(randCity2) != randCity4
                    && next(randCity3) != randCity1
                    && next(randCity3) != randCity2
                    && randCity3 != randCity4 && next(randCity3) != randCity4
                    && next(randCity4) != randCity1
                    && next(randCity4) != randCity2
                    && next(randCity4) != randCity3)
                {   //if the cities is not next to each other

                    int[] tourcopy = new int [n];
                    System.arraycopy(tour, 0,tourcopy, 0, n);   //make a copy of the original tour

                    flip(edge2[1],edge4[1]);
                    flip(edge3[0],edge1[0]); //flip the edges and run 2-opt

                    gainExit = 1;

                    for (i=0; i<n; i++)
                        visited[i] = true;

                    while (gainExit==1) {
                        opt2();
                    }

                    bestTourLength=0;
                    tourLength();
                    if (bestTourLength<gainTourLength)  // if there is gain keep the changed tour
                    {
                        System.arraycopy(tour, 0, tourcopy, 0, n);
                        flagExit = 1;
                        gainTourLength = bestTourLength;
                        //System.out.println("FOUND 4-opt!: "+bestTourLength); // print the best tour length after every 4-opt
                    }
                    else
                        System.arraycopy(tourcopy, 0, tour, 0, n);
                }   // end if
            } // end for
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

} // end of tsp4
