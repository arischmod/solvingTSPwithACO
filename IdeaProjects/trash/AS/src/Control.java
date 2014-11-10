import java.util.Random;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import static java.lang.System.nanoTime;
import static java.lang.System.out;
import static java.util.logging.Logger.getLogger;

/**
 * Created by IntelliJ IDEA.
 * User: aris
 * Date: 4/17/11
 * Time: 8:48 PM
 * To change this template use File | Settings | File Templates.
 */

public class Control{

    private int n;
    private int m;
    private int nn=20;
    private int[][] distance;
    private int[][] nnList;
    private double[][] pheromone;
    private double[][] choice_info;
    private double[][] heuristicFactor;
    private int iterations=50000;
    private int a = 1;
    private int b = 5;
    private Ant[] ants;
    private int bestTourLength = Integer.MAX_VALUE;
    private double[] x;
    private double[] y;
    private boolean terminate=true;
    private int optimalTour;
    private int nntl;  //Nearest Neighbor Tour Length

    /*
    Runs the main loop
    */
    public Control(int m, String city_choice, int local) {
        int i;
        this.m=m;
        InitializeData(city_choice);

        while (terminate) {
            for (i=0; i<iterations; i++)
            {
                ConstructSolutions();
                if (local==1)
                    LocalSearch();
                UpdateStatistics(i);
                if (bestTourLength==optimalTour) {
                    out.println(" :)     found optimal!!!   :)");
                    terminate = false;
                    break;
                }
                ASPheromoneUpdate();
                ComputeChoiceInformation();
            }
            terminate = false;
        }
    }

    /*
    Initialize all data to its initial value needed for the ants to construct the tour.
    */
    public void InitializeData(String fileName) {

        ReadInstance(fileName);
        Ant[] ants = new Ant[m];
        double[][] choice_info = new double [n][n];
        double[][] pheromone = new double[n][n];
        int[][] distance = new int[n][n];
        double[][] heuristicFactor = new double[n][n];
        int[][] nnList = new int [n][nn];

        this.ants=ants;
        this.choice_info=choice_info;
        this.pheromone=pheromone;
        this.distance=distance;
        this.nnList=nnList;
        this.heuristicFactor=heuristicFactor;

        ComputeDistances(x, y);
        ComputeNearestNeighborLists();
        this.nntl = ComputeNearestNeighborTourLength();
        out.println(nntl);
        InitializePheromoneTrail();
        InitializeheuristicFactor();
        ComputeChoiceInformation();
        InitializeAnts();
    }

    /*
    Uses the scanner to read the instance file. What is read from the file is the x and y coordinates for the instance and the number of cities.
    */
    public void ReadInstance(String fileName)
    {
        File file = new File(fileName);
        try {
            //
            // Create a new Scanner object which will read the data
            // from the file passed in. To check if there are more
            // line to read from it we check by calling the
            // scanner.hasNextLine() method. We then read line one
            // by one till all line is read.
            //
            Scanner instanceScanner = new Scanner(file);

            this.n = instanceScanner.nextInt();
            this.optimalTour = instanceScanner.nextInt();
            double[] x = new double[n];
            double[] y = new double[n];

            this.x = x;
            this.y = y;

            for (int i = 0; i < n; i++) {
                int j = instanceScanner.nextInt() - 1;

                x[j] = instanceScanner.nextDouble();   //reading coordinates
                y[j] = instanceScanner.nextDouble();
            }

        } catch (FileNotFoundException ex) {
        getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);  }
    }

    /*
    Computes a matrix containing every distance to a city j when in a city i using the x and y coordinates
    */
    public void ComputeDistances(double[] x, double[] y)
    {
        int maxDist=9000000;
        for ( int i=0; i<n; i++) {
            for ( int j=0; j<n; j++) {
                if (i!=j) {
                    double dx = x[i] - x[j], dy = y[i] - y[j];
                    this.distance[i][j] = (int) (Math.sqrt(dx * dx + dy * dy)+0.5);
                }
                else
                    distance[i][j] = maxDist;
            }
        }
    }

    /*
    Computes a nearest neighbor list by sorting the distance matrix containing a number of nearest neighbors when in a city i using bubble sort for the sorting.
    */
    public void ComputeNearestNeighborLists()
    {
        int[][] sorted_dis = new int[n][n];
        int[][] n_index = new int[n][n];
        int i, j;

        for (i=0; i<n; i++)
            for(j=0; j<n; j++)
                sorted_dis[i][j] = distance[i][j];

        for (i=0; i<n; i++)
            for(j=0; j<n; j++)
                n_index[i][j]=j;

        for (i=0; i<n; i++) {
            for(j=0; j<n-1; j++) {
                   if (sorted_dis[i][j+1]<sorted_dis[i][j])
                   {
                       int temp = sorted_dis[i][j];
                       int temp2 = n_index[i][j];
                       sorted_dis[i][j] = sorted_dis[i][j+1];
                       n_index[i][j] = n_index[i][j+1];
                       sorted_dis[i][j+1] = temp;
                       n_index[i][j+1] = temp2;
                       j=-1;
                   }
            }
        }
        for (i=0; i<n; i++) {
            for(j=0; j<nn; j++) {
                this.nnList[i][j]=n_index[i][j];
            }
        }
    }

    /*
    Finds the length of a tour found by using the nearest neighbor algorithm used to compute some other data.
    */
    public int ComputeNearestNeighborTourLength()
    {
        int step,j, prev_city, next_city, minDis;
        int minCit=0;
        int tourLength=0;
        boolean[] visited = new boolean[n];

        prev_city=0;
        visited[prev_city] = true;

        for (step=1; step < n; step++)
        {
            minDis=Integer.MAX_VALUE;
                for (j=0; j<n; j++) {
                    next_city = j;
                    if ( prev_city==next_city )
                        continue;
                    else if (visited[next_city] == false && distance[prev_city][next_city] < minDis) {
                        minDis = distance[prev_city][next_city];
                        minCit=next_city;
                    }
                }

            tourLength += distance[prev_city][minCit];
            prev_city=minCit;
            visited[prev_city]=true;
        }
        tourLength += distance[prev_city][0];

        return tourLength;
    }

    /*
    Initialize the amount of pheromone on all edges to 1 divided by the nearest neighbor tourlength.
    */
    public void InitializePheromoneTrail()
    {
        double p=0.5;
        for (int i=0; i<n; i++)
            for(int j=i; j<n; j++) {
                this.pheromone[i][j] = (double)m / (double)nntl;
                this.pheromone[j][i] = this.pheromone[i][j];
             }
    }

    /*
    Computes a heuristicFactor matrix containing all heuristic information for all edges.
    */
    public void InitializeheuristicFactor()
    {
        for (int i=0; i<n; i++) {
            for(int j=i; j<n; j++) {
                if (i != j) {
                    heuristicFactor[i][j] =  1.0 / distance[i][j];
                }
                else
                    heuristicFactor[i][j] = Double.MIN_VALUE;
            }
        }
    }

    /*
    Computes the ChoiceInformation for every edge using the pheromone matrix and the heuristicFactor matrix to calculate the choice info.
    */
    public void ComputeChoiceInformation()
    {
        for (int i=0; i<n; i++) {
            for(int j=i; j<n; j++) {
                this.choice_info[i][j] = Math.pow(pheromone[i][j], a) * Math.pow(heuristicFactor[i][j], b);
                this.choice_info[j][i] = this.choice_info[i][j];
            }
        }
    }

    /*
    Creates a number of new ant objects equal to the size of m. Each ant is given the number of cities, the distance matrix and the choice info matrix.
     */
    public void InitializeAnts()
    {
        for (int i=0; i<m; i++)
            this.ants[i] = new Ant(n, distance, choice_info);
    }

    /*
    Checks whether each ant have found a better tourLength than the bestTourLength. If so the bestTourLength is set equal to the new best tourLengths value. Also the elite ant is set equal to the ant who found the best tour, BestTourLength, current iteration and deviation from the gap is printed.
    */
    public void UpdateStatistics(int i)
    {
        for (int k=0; k<m; k++) {
            ants[k].cal_tour_length();
            if (bestTourLength>ants[k].getTourLength()) {
                bestTourLength=ants[k].getTourLength();
                out.println("Best tour length="+bestTourLength+"  after: "+i+"   gap = "+((double)(bestTourLength-optimalTour)*100.0)/(double)optimalTour+"%");
                ants[k].draw(x,y,0);
            }
        }
    }

    /*
    Construct a new tour for each ant. Also reset the visited array for every ant and place them on a new random city.
    */
    public void ConstructSolutions()
    {
        int k, r;

        for (k=0; k<m; k++) {
            for (int i=0; i<n; i++) {
                ants[k].setVisited(i,false);
            }
        }

        int step=0;

        for ( k=0; k<m; k++ ) {
            Random rand = new Random();
            r = (int) (rand.nextDouble() * n);
            ants[k].setVisited(r,true);
            ants[k].setTourCity(step,r);
        }

        while (++step<n) {

            for (k=0; k<m; k++)
            {
                //ants[k].ASDecisionRule(step);
                ants[k].NeighborListASDecisionRule(step, nnList, nn);
            }
        }

        for (k=0; k<m; k++) {
            ants[k].setTourCity(step, ants[k].getTourCity(0));
        }
    }

    /*
    Evaporates the pheromone by 50% on every arc at each iteration.
    */
    public void Evaporate()
    {
            double p = 0.5;

            for (int i=0; i<n; i++) {
                for (int j=i; j<n; j++) {
                    pheromone[i][j] = (1.0-p) * pheromone[i][j];
                    pheromone[j][i] = pheromone[i][j];
                }
            }
    }

    /*
    Deposit pheromone on the arcs making up the tour of an ant. This is done for each ant in every iteration after finishing the tour construction.
    */
    public void DepositPheromone(int k)
    {
            int i,j,l,s;
            double trail = 1.0/(double)ants[k].getTourLength();

            for (i=0; i<n; i++) {
                j = ants[k].getTourCity(i);
                l = ants[k].getTourCity(i+1);
                pheromone[j][l] = pheromone[j][l]+trail;
                pheromone[l][j] = pheromone[j][l];
            }
        }

    /*
    Calls the evaporate and the depositPheromone.
    */
    public void ASPheromoneUpdate()
    {
            Evaporate();
            for (int k=0; k<m; k++) {
                DepositPheromone(k);
            }
    }

    /*
    Performs a localSearch using 2-opt on each tour the ants have found.
    */
    public void LocalSearch()
    {
        boolean isChange;

        for (int k=0; k<m; k++) {
            do {
                isChange=false;
                isChange=ants[k].Opt2();
            }while (isChange==true);
        }
    }

    /*
    The main class. Creates a new object of the control class, parse the parameters and calculates runtime.
    */
    public static void main(String[] args)
    {
        final long startTime = nanoTime();   //calculating run time function

        int m=Integer.parseInt(args[0]); // number of Ants
        String city_choice = args[1];
        int local=Integer.parseInt(args[2]); // activate local search;

        Control demo = new Control(m,city_choice, local);
        final long duration = nanoTime() - startTime;  //  run time
            out.println("run time: "+duration/1000000000);
        //terminate program
    }

}
