import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;

import static java.util.logging.Logger.getLogger;

/**
 * Created by IntelliJ IDEA.
 * User: aris
 * Date: 4/18/11
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */

public class InitializeData{

    private int a = 1;
    private int b = 2;
    private int n;
    private int m;
    private Ant[] ants;
    private double[][] choice_info;
    private double[][] pheromone;
    private double[][] distance;
    private int[][] nnList;
    private double nntl;  //Nearest Neighbor Tour Length

    public InitializeData(int n, int m, int selection) {

        this.n = n;
        this.m = m;
        Ant[] ants = new Ant[m];
        double[][] choice_info = new double [n][n];
        double[][] pheromone = new double[n][n];
        double[][] distance = new double[n][n];
        int[][] nnList = new int [n][n];

        this.ants=ants;
        this.choice_info=choice_info;
        this.pheromone=pheromone;
        this.distance=distance;
        this.nnList=nnList;

        ReadInstance(selection);
        ComputeNearestNeighborLists();
        this.nntl = ComputeNearestNeighborTourLength();
        ComputePheromoneTrail();
        ComputeChoiceInformation();
        InitializeAnts();
    }

    public void ReadInstance(int selection)
    {

        double[] x = new double[n];
        double[] y = new double[n];

        for (int i=0; i<n; i++)
            for(int j=0; j<n; j++)
                this.nnList[i][j] = j;

        if( selection == 0)  //  read the luxemburg citise from file
                    {
                        File file = new File("qa194.tsp");
                        try {
                            //
                            // Create a new Scanner object which will read the data
                            // from the file passed in. To check if there are more
                            // line to read from it we check by calling the
                            // scanner.hasNextLine() method. We then read line one
                            // by one till all line is read.
                            //
                            Scanner instanceScanner = new Scanner(file);

                            int g = instanceScanner.nextInt();

                            for (int i = 0; i < n; i++) {
                                int j = instanceScanner.nextInt() - 1;

                                x[j] = instanceScanner.nextDouble();   //reading coordinates
                                y[j] = instanceScanner.nextDouble();
                            }
                            ComputeDistances( x, y);
                        } catch (FileNotFoundException ex) {
                            getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);  }
                    }
                    else if( selection == 1 )     // user chooses the number of cities
                    {
                        for (int  i=0 ; i<n; i++)
                        {
                            x[i] = (Math.random() * 20000) + 40000;  // random coordinates for each city
                            y[i] = (Math.random() * 1000) + 5000;
                        }
                        ComputeDistances( x, y);
                    }
                    else {
                        x[0] = 2868972;
                        y[0] = 789594;
                        x[1] = 2859098;
                        y[1] = 753186;
                        x[2] = 2900512;
                        y[2] = 778672;
                        x[3] = 2817379;
                        y[3] = 741988;
                        x[4] = 2842471;
                        y[4] = 763200;
                        x[5] = 2887468;
                        y[5] = 762420;
                        x[6] = 2827542;
                        y[6] = 714041;
                        x[7] = 2860737;
                        y[7] = 729751;
                        x[8] = 2837746;
                        y[8] = 740969;
                        x[9] = 2849437;
                        y[9] = 692353;
                        x[10] = 2875162;
                        y[10] = 749108;
                        x[11] = 2818536;
                        y[11] = 701642;
                        x[12] = 2887262;
                        y[12] = 743972;
                        x[13] = 2796999;
                        y[13] = 756023;
                        ComputeDistances(x, y);
        }

    }

    public void ComputeDistances(double[] x, double[] y)
    {
        for ( int i=0; i<n; i++)
            for ( int j=0; j<n; j++) {
                if (i==j) {
                    this.distance[i][j] = Double.MAX_VALUE;
                }
                else {
                    double dx = x[i] - x[j], dy = y[i] - y[j];
                    this.distance[i][j] = (int) (Math.sqrt(dx * dx + dy * dy) + 0.5);
                }
            }
    }

    public double getDistance(int a, int b)
    {
        return distance[a][b];

    }

    public void ComputeNearestNeighborLists()
    {
        double[][] sorted_dis = new double[n][n];

        for (int i=0; i<n; i++)
            for(int j=0; j<n; j++)
                sorted_dis[i][j] = distance[i][j];

        for (int i=0; i<n; i++) {
            for(int j=1; j<n; j++) {
               for(int r=0; r<n-j; r++)
                   if (sorted_dis[i][r+1]<sorted_dis[i][r])
                   {
                       double temp2 = sorted_dis[i][r];
                       int temp = nnList[i][r];
                       sorted_dis[i][r] = sorted_dis[i][r+1];
                       this.nnList[i][r] = nnList[i][r+1];
                       sorted_dis[i][r+1] = temp2;
                       this.nnList[i][r+1] = temp;
                   }
            }
        }
    }

    public double ComputeNearestNeighborTourLength()
    {
        double tourLength=0;
        int saveNN=0;
        boolean[] visited = new boolean[n];
        visited[0] = true;
        int i=0;
        int j=0;

        do {
                if (visited[nnList[saveNN][j]]==false) {
                    tourLength += distance[saveNN][nnList[saveNN][j]];
                    visited[nnList[saveNN][j]]=true;
                    saveNN=nnList[saveNN][j];
                    j=0;
                    i++;
                }
                else if (j<n-1) {
                    j++;
                }
        } while (i<n-1);
            tourLength += distance[saveNN][0];
        System.out.println(tourLength);
        return tourLength;
    }

    public int[][] getNN_list()
    {
        return nnList;
    }

    public void ComputePheromoneTrail()
    {
        for (int i=0; i<n; i++)
            for(int j=0; j<n; j++) {
                this.pheromone[i][j] = 1/(n*nntl);
             }
    }

    public double getNntl()
    {
        return nntl;
    }

    public void setPheromone(int i, int j, double trail)
    {
        this.pheromone[i][j] = trail;

    }

    public double getPheromone(int i, int j)
    {
        return pheromone[i][j];
    }

    public void setChoiceInformation()
    {
        ComputeChoiceInformation();
    }

    public void ComputeChoiceInformation()
    {
        double heuristicFactor;

        for (int i=0; i<n; i++) {
            for(int j=0; j<n; j++) {
                if (i != j) {
                    heuristicFactor =  1 / distance[i][j];
                } else {
                    heuristicFactor = Double.MIN_VALUE;
                }
                this.choice_info[i][j] = Math.pow(pheromone[i][j], a) * Math.pow(heuristicFactor, b);
            }
        }
    }

    public double getChoice_info (int i, int j) {
        return choice_info[i][j];
    }

    public void InitializeAnts()
    {
        for (int i=0; i<m; i++)
            this.ants[i] = new Ant(n, distance);
    }

    public final Ant getAnts(int i)
    {
        return ants[i];
    }

    public void InitializeStatistics()
    {

    }

}
