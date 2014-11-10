import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import static java.lang.System.exit;
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

public class AsTSP {

    private int n;
    private int m;
    private int nn;
    private int[][] distance;
    private int[][] nnList;
    private double[][] pheromone;
    private double[][] choice_info;
    private double[][] heuristicFactor;
    private int alpha;
    private int beta;
    private int e;
    private int local;
    private Ant[] ants;
    private Ant eliteAnt;
    private int bestTourLength = Integer.MAX_VALUE;
    private int LastbestTourLength = Integer.MAX_VALUE;
    private double[] x;
    private double[] y;
    private int[] bestTour;
    private int optimalTour;
    private double tmax;
    private double tmin;
    private String algorithm;
    private int nntl;  //Nearest Neighbor Tour Length
    private  double p;  //evaporation factor
    private  double q0;
    private Random rand=new Random();
    /*
    Runs the main loop
    */
    public AsTSP(int m, String city_choice, int local, int a, int b, int o, int nn, int iterations, int e, String algorithm, double p, double q0) {
        this.m=m;
        this.local=local;
        this.alpha=a;
        this.beta=b;
        this.optimalTour=o;
        this.nn=nn;
        this.e=e;
        this.algorithm=algorithm;
        this.p=p;
        this.q0=q0;
        InitializeData(city_choice);


        for (int i=0; i<iterations; i++)
        {
            List <Thread> threads = new ArrayList<Thread>();
            for(int k=0; k<m; k++) {
                threads.add(k,new Thread(ants[k]));
                threads.get(k).start();
            }
            for (Thread t: threads)
            {
                try {
                    t.join();
                } catch (InterruptedException d) { d.printStackTrace(); }
            }
            UpdateStatistics(i);
            if (bestTourLength==optimalTour) {
                out.println(" :)     found optimal!!!   :)");
                break;
            }
            ASPheromoneUpdate();
            ComputeChoiceInformation();
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
        int[] bestTour= new int [n+1];
        double[][] heuristicFactor = new double[n][n];
        int[][] nnList = new int [n][nn];

        this.ants=ants;
        this.choice_info=choice_info;
        this.pheromone=pheromone;
        this.distance=distance;
        this.nnList=nnList;
        this.bestTour=bestTour;
        this.heuristicFactor=heuristicFactor;

        ComputeDistances(x, y);
        ComputeNearestNeighborLists();
        nntl = ComputeNearestNeighborTourLength();
        InitializePheromoneTrail();
        InitializeHeuristicFactor();
        ComputeChoiceInformation();
        InitializeAnts();
        InitializeStatistics();
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

            n = instanceScanner.nextInt();
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
        getLogger(AsTSP.class.getName()).log(Level.SEVERE, null, ex);  }
    }

    /*
    Computes a matrix containing every distance to a city j when in a city i using the x and y coordinates
    */
    public void ComputeDistances(double[] x, double[] y)
    {
        for ( int i=0; i<n; i++)
            for ( int j=i+1; j<n; j++) {
                    double dx = x[i] - x[j], dy = y[i] - y[j];
                    distance[i][j] = (int) (Math.sqrt(dx * dx + dy * dy)+0.5);
                    distance[j][i]=distance[i][j];
            }
    }

    /*
    Computes a nearest neighbor list by sorting the distance matrix containing a number of nearest neighbors when in a city i using bubble sort for the sorting.
    */
    public void ComputeNearestNeighborLists()
    {

        int[][] sorted_dis = new int[n][nn];
        boolean[] sorted = new boolean[n];
        boolean  swapped;

        for (int i=0; i<n; i++)
            for(int j=0; j<nn; j++) {
                if (i!=j) {
                    nnList[i][j]=j;
                    sorted_dis[i][j]=distance[i][j];
                    sorted[j]=true;
                }
                else if(i==j && (j+nn)<n){
                    nnList[i][j]=j+nn;
                    sorted_dis[i][j]=distance[i][j+nn];
                    sorted[j+nn]=true;
                }
                else if(i==j && (j+nn)>n){
                    nnList[i][j]=nn-j;
                    sorted_dis[i][j]=distance[i][nn-j];
                    sorted[j-nn]=true;
                }
            }

        for (int i=0; i<n; i++) {
            do {
                swapped = false;
                for(int j=1; j<nn; j++) {
                    if (sorted_dis[i][j-1]>sorted_dis[i][j]) {
                        int temp = sorted_dis[i][j];
                        int temp2 = nnList[i][j];
                        sorted_dis[i][j] = sorted_dis[i][j-1];
                        nnList[i][j] = nnList[i][j-1];
                        sorted_dis[i][j-1] = temp;
                        nnList[i][j-1] = temp2;
                        swapped = true;
                    }
                }
            } while (swapped);
        }

        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                sorted[j]=false;
            }
            for (int j=0; j<nn; j++) {
                sorted[nnList[i][j]]=true;
            }
            sorted[i]=true;

            for(int j=0; j<n; j++) {
                if (distance[i][j] < sorted_dis[i][nn-1] && sorted[j]==false) {
                    sorted[nnList[i][nn-1]]=false;
                    sorted_dis[i][nn-1]=distance[i][j];
                    nnList[i][nn-1]=j;
                    sorted[j]=true;
                    for (int s=0; s<nn-1; s++) {    //sort function
                        if(sorted_dis[i][s]>sorted_dis[i][nn-1]) {
                            int temp=sorted_dis[i][nn-1];
                            int temp2=nnList[i][nn-1];
                            for (int d=nn-1; d>s; d--) {
                                sorted_dis[i][d]=sorted_dis[i][d-1];
                                nnList[i][d]=nnList[i][d-1];
                            }
                            sorted_dis[i][s]=temp;
                            nnList[i][s]=temp2;
                            break;
                        }
                    }
                }
            }
        }
        /*int[][] sorted_dis = new int[n][n];
        int[][] n_index = new int[n][n];
        int i, j;
        boolean swapped;

        for (i=0; i<n; i++)
            for(j=0; j<n; j++)
                sorted_dis[i][j] = distance[i][j];

        for (i=0; i<n; i++)
            for(j=0; j<n; j++)
                n_index[i][j]=j;

        for (i=0; i<n; i++) {
            do {
                swapped = false;
                for(j=1; j<n; j++) {
                    if (sorted_dis[i][j-1]<sorted_dis[i][j]) {
                        int temp = sorted_dis[i][j];
                        int temp2 = n_index[i][j];
                        sorted_dis[i][j] = sorted_dis[i][j-1];
                        n_index[i][j] = n_index[i][j-1];
                        sorted_dis[i][j-1] = temp;
                        n_index[i][j-1] = temp2;
                        swapped = true;
                    }
                }
            } while (swapped);
        }

        for (i=0; i<n; i++)
            for(j=0; j<nn; j++) {
                nnList[i][j]=n_index[i][j+1];
            }
        */
    }

    /*
    Finds the length of a tour found by using the nearest neighbor algorithm used to compute some other data.
    */
    public int ComputeNearestNeighborTourLength()
    {
        int prev_city=0, next_city, minDis;
        int minCit=0;
        int tourLength=0;
        boolean[] visited = new boolean[n];

        visited[prev_city] = true;

        for (int step=1; step < n; step++)
        {
            minDis=Integer.MAX_VALUE;
            for (int j=0; j<n; j++) {
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
        if (algorithm.equals("a"))
            for (int i=0; i<n; i++)
                for(int j=i+1; j<n; j++) {
                    pheromone[i][j] = (double)m / (double)nntl;
                    pheromone[j][i] = pheromone[i][j];
                }
        else if (algorithm.equals("e"))
            for (int i=0; i<n; i++)
                for(int j=i+1; j<n; j++) {
                    this.pheromone[i][j] = (double)(e+m) / (p * (double)nntl);
                    pheromone[j][i] = pheromone[i][j];
                }
        else if (algorithm.equals("m"))
            for (int i=0; i<n; i++)
                for(int j=i+1; j<n; j++) {
                    this.pheromone[i][j] = (double)1/(p*(double)nntl);
                    pheromone[j][i] = pheromone[i][j];
                }
    }

    /*
    Computes a heuristicFactor matrix containing all heuristic information for all edges.
    */
    public void InitializeHeuristicFactor()
    {
        for (int i=0; i<n; i++)
            for(int j=i+1; j<n; j++) {
                heuristicFactor[i][j] =  1.0 / (distance[i][j]+0.001);
                heuristicFactor[j][i]=heuristicFactor[i][j];
            }
    }

    /*
    Computes the ChoiceInformation for every edge using the pheromone matrix and the heuristicFactor matrix to calculate the choice info.
    */
    public void ComputeChoiceInformation()
    {
        for (int i=0; i<n; i++)
            for(int j=i+1; j<n; j++) {
                choice_info[i][j] = Math.pow(pheromone[i][j], alpha) * Math.pow(heuristicFactor[i][j], beta);
                choice_info[j][i] = choice_info[i][j];
            }
    }

    /*
    Creates a number of new ant objects equal to the size of m. Each ant is given the number of cities, the distance matrix and the choice info matrix.
     */
    public void InitializeAnts()
    {
        for (int i=0; i<m; i++)
            ants[i] = new Ant(n, distance, choice_info, nnList, nn, local);
    }

    /*
    Creates an eliteAnt object containing the same information as a normal ant.
    */
    public void InitializeStatistics()
    {
        Ant eliteAnt = new Ant(n, distance, choice_info, nnList, nn, local);
        this.eliteAnt=eliteAnt;
    }

    /*
    Checks whether each ant have found a better tourLength than the bestTourLength. If so the bestTourLength is set equal to the new best tourLengths value. Also the elite ant is set equal to the ant who found the best tour, BestTourLength, current iteration and deviation from the gap is printed.
    */
    public void UpdateStatistics(int i)
    {
        int IterationCount=0;
        for (int k=0; k<m; k++) {
            ants[k].cal_tour_length();
            if (LastbestTourLength>ants[k].getTourLength()) {
                eliteAnt=ants[k];
                LastbestTourLength=eliteAnt.getTourLength();
            }
        }
        if (bestTourLength>LastbestTourLength) {
            bestTourLength=LastbestTourLength;
            if (algorithm.equals("m")) {

                bestTour=eliteAnt.getTour();
                tmax=1.0/(p*bestTourLength);
                tmin= tmax*( Math.pow(0.05, 1.0/n))/(((n/2)-1)*Math.pow(0.05, 1.0/n));
                IterationCount=0;
            }
            out.println("Best tour length="+bestTourLength+"  after: "+(i+1)+"   gap = "+((double)(bestTourLength-optimalTour)*100.0)/(double)optimalTour+"%");
            eliteAnt.draw(x,y,0);
        }
        else if (algorithm.equals("m")) {

            IterationCount++;

            if (IterationCount>60) {
                IterationCount=0;
                InitializePheromoneTrail();
            }
        }
        LastbestTourLength=Integer.MAX_VALUE;
    }

    /*
    Evaporates the pheromone by 50% on every arc at each iteration.
    */
    public void Evaporate()
    {
        for (int i=0; i<n; i++)
            for (int j=i+1; j<n; j++) {
                pheromone[i][j] = (1.0-p) * pheromone[i][j];
                pheromone[j][i] = pheromone[i][j];
            }
    }

    /*
    Deposit pheromone on the arcs making up the tour of an ant. This is done for each ant in every iteration after finishing the tour construction.
    */
    public void DepositPheromone(int k)
    {
        int j,l;
        double trail = 1.0/(double)ants[k].getTourLength();

        for (int i=0; i<n; i++) {
            j = ants[k].getTourCity(i);
            l = ants[k].getTourCity(i+1);
            pheromone[j][l] = pheromone[j][l]+trail;
            pheromone[l][j] = pheromone[j][l];
        }
    }

    /*
    Deposit extra pheromone on the best so far tours arcs. It deposit pheromone a number of times equal to a variable e which stands for eliteAnts.
    */
    public void EliteDeposit()
    {
        int i, j, l;
        double trail = (double)e/(double)eliteAnt.getTourLength();
        for (i=0; i<n; i++) {
            j = eliteAnt.getTourCity(i);
            l = eliteAnt.getTourCity(i+1);
            pheromone[j][l] = pheromone[j][l]+trail;
            pheromone[l][j] = pheromone[j][l];
        }
    }

    public void MMEvaporate()
        {
            for (int i=0; i<n; i++)
                for (int j=i+1; j<n; j++) {
                    pheromone[i][j] = (1.0-p) * pheromone[i][j];
                    if (pheromone[i][j]<tmin)
                        pheromone[i][j]=tmin;
                    if (pheromone[i][j]>tmax)
                        pheromone[i][j]=tmax;
                    pheromone[j][i] = pheromone[i][j];
                }
        }

    public void MMDeposit()
    {
        int i, j, l;
        double q=Math.random();
        if (q0<q) {
            double trail = (double)1/(double)eliteAnt.getTourLength();
            for (i=0; i<n; i++) {
                j = eliteAnt.getTourCity(i);
                l = eliteAnt.getTourCity(i+1);
                pheromone[j][l] = pheromone[j][l]+trail;
                /*if (pheromone[j][l]>tmax)
                    pheromone[j][l]=tmax;*/
                pheromone[l][j] = pheromone[j][l];
            }
        }
        else {
            double trail = (double)1/(double)bestTourLength;
            for (i=0; i<n; i++) {
                j = bestTour[i];
                l = bestTour[i+1];
                pheromone[j][l] = pheromone[j][l]+trail;
                /*if (pheromone[j][l]>tmax)
                    pheromone[j][l]=tmax;  */
                pheromone[l][j] = pheromone[j][l];
            }
        }
    }
    /*
    Calls the evaporate and the depositPheromone.
    */
    public void ASPheromoneUpdate()
    {
        if (algorithm.equals("e") || algorithm.equals("k")) {
            Evaporate();
            for (int k=0; k<m; k++)
                DepositPheromone(k);
        }
        if (algorithm.equals("e"))
            EliteDeposit();
        if (algorithm.equals("m")) {
            MMEvaporate();
            MMDeposit();
        }
    }

    /*
    The main class. Creates a new object of the control class, parse the parameters and calculates runtime.
    */
    public static void main(String[] args)
    {
        final long startTime = nanoTime();   //calculating run time function
        parse_args(args);
        final long duration = nanoTime() - startTime;  //  run time
            out.println("run time: "+duration/1000000000+" sec");
        terminate();
    }

    public static void parse_args(String[] args)
    {
        String city_choice = "d198.tsp", algorithm="a";
        int m=100, local=0, a=1, b=5, nn=20, o=0, iterations=100000, e=m;
        double p=0.5;   //for the MMAS algorithm p=0.02 is suggested
        double q0=0.9;

        for (int i=0; i<args.length; i++) {
            if (args[i].contains("-i"))
                city_choice = args[i+1];
            else if (args[i].contains("-m"))
                m=Integer.parseInt(args[i+1]);
            else if (args[i].contains("-l"))
                local=Integer.parseInt(args[i+1]);
            else if (args[i].contains("-a"))
                a=Integer.parseInt(args[i+1]);
            else if (args[i].contains("-b"))
                b=Integer.parseInt(args[i+1]);
            else if (args[i].contains("-o"))
                o=Integer.parseInt(args[i+1]);
            else if (args[i].contains("-nn"))
                nn=Integer.parseInt(args[i+1]);
            else if (args[i].contains("-t"))
                iterations=Integer.parseInt(args[i+1]);
            else if (args[i].contains("-e"))
                e=Integer.parseInt(args[i+1]);
            else if (args[i].contains("-c"))
                algorithm=args[i+1];
            else if (args[i].contains("-p"))
                p=Double.parseDouble(args[i+1]);
            else if (args[i].contains("-q"))
                q0=Double.parseDouble(args[i+1]);
            else if (args[i].contains("-h")) {
                out.println("Help menu");
                terminate();
            }
        }
        AsTSP demo = new AsTSP(m,city_choice, local, a, b, o, nn, iterations, e, algorithm, p, q0);
    }

    public static void terminate()
    {
        exit(0);
    }

}
