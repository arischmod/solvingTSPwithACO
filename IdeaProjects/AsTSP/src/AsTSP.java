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

    private int n;     //number of cities
    private int m;     //number of Ants
    private int nn;
    private int[][] distance;
    private int[][] nnList;
    private double[][] pheromone;
    private double[][] choice_info;
    private double[][] heuristicFactor;
    private int alpha;  //weight given to pheromone factor
    private int beta;   //weight given to heuristic factor
    private int e;      //weight given to the best tour
    private int local;
    private Ant[] ants;
    private Ant eliteAnt;
    private int bestTourLength = Integer.MAX_VALUE;
    private int LastbestTourLength = Integer.MAX_VALUE;
    private double[] x;  //coordinates
    private double[] y;
    private int[] bestTour;
    private int optimalTour;
    private double tmax;    //pheromone limits used in the MMas
    private double tmin;
    private char algorithm;
    private int nntl;  //Nearest Neighbor Tour Length
    private  double p;  //evaporation factor
    private  double q0;
    private Random rand=new Random();

    /*
    Runs the main loop
    */
    public AsTSP(int m, String city_choice, int local, int a, int b, int o, int nn, int iterations, int e, char algorithm, double p, double q0) {
        this.m=m;
        this.local=local;
        this.alpha=a;
        this.beta=b;
        this.optimalTour=o;
        this.nn=nn;
        this.e=e;
        this.algorithm=algorithm;
        this.q0=q0;
        if (algorithm=='m')
            this.p=0.02;
        else
            this.p=p;
        InitializeData(city_choice);


        for (int i=0; i<iterations; i++)
        {
            List <Thread> threads = new ArrayList<Thread>();   //an arayList with all the threads
            for(int k=0; k<m; k++) {
                threads.add(k,new Thread(ants[k]));      //for every ant create a new thread and start it (construct tours)
                threads.get(k).start();
            }
            for (Thread t: threads)
            {
                try {
                    t.join();          //wait until all the treads are finished
                } catch (InterruptedException d) { d.printStackTrace(); }
            }
            UpdateStatistics(i);   //find the new best tour length
            if (bestTourLength<=optimalTour) {
                out.println(" :)     found optimal!!!   :)");
                break;           //if the optimal is found the program terminates
            }
            ASPheromoneUpdate();  //update the pheromones
            ComputeChoiceInformation();    //compute the choice info with the new pheromone values
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
        InitializeAnts();
        InitializeStatistics();
        InitializePheromoneTrail();
        InitializeHeuristicFactor();
        ComputeChoiceInformation();
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
                    distance[i][j] = (int) (Math.sqrt(dx * dx + dy * dy)+0.5);  //round the distance to the closest int
                    distance[j][i]=distance[i][j];
            }
    }

    /*
    Computes the nnList which contains the nn nearest city for each city.
    */
    public void ComputeNearestNeighborLists()
    {

        int[][] sorted_dis = new int[n][nn];  //an array with the nn smaller distances
        boolean[] sorted = new boolean[n];    //saws if the city i is already at the sorted_dis
        boolean  swapped;

        for (int i=0; i<n; i++)
            for(int j=0; j<nn; j++) {
                if (i!=j) {
                    nnList[i][j]=j;        //  nnList is the index that saws which city each distance represents
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

        for (int i=0; i<n; i++) {       //sort the sorted_dis array
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

            for(int j=0; j<n; j++) {            //for every city
                if (distance[i][j] < sorted_dis[i][nn-1] && sorted[j]==false) {  //it checks if the distance is smaller and this city is not yet sorted
                    sorted[nnList[i][nn-1]]=false;
                    sorted_dis[i][nn-1]=distance[i][j];
                    nnList[i][nn-1]=j;
                    sorted[j]=true;
                    for (int s=0; s<nn-1; s++) {    //sort function
                        if(sorted_dis[i][s]>sorted_dis[i][nn-1]) {   //add the city to the sorted_did array to the rigth place
                            int temp=sorted_dis[i][nn-1];            //and delete the largest distance
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
        if (algorithm=='a')
            for (int i=0; i<n; i++)
                for(int j=i+1; j<n; j++) {
                    pheromone[i][j] = (double)m / (double)nntl;
                    pheromone[j][i] = pheromone[i][j];
                }
        else if (algorithm=='e')
            for (int i=0; i<n; i++)
                for(int j=i+1; j<n; j++) {
                    this.pheromone[i][j] = (double)(e+m)/(p * (double)nntl);
                    pheromone[j][i] = pheromone[i][j];
                }
        else if (algorithm=='m')
            for (int i=0; i<n; i++)
                for(int j=i+1; j<n; j++) {
                    this.pheromone[i][j] = tmax;
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
                heuristicFactor[i][j] =  1.0 / (distance[i][j]+0.000001);  //divide with a non zero value
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
        tmax=1.0/(p*nntl);
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
            if (algorithm=='m') {
                for (int j=0; j<n+1; j++)
                    bestTour[j]=eliteAnt.getTourCity(j);
                tmax=1.0/(p*bestTourLength);
                tmin= tmax*( Math.pow(0.05, 1.0/n))/(((n/2)-1)*Math.pow(0.05, 1.0/n));
                IterationCount=0;
            }
            out.printf("Best tour length = %d, after: %d iterations,  gap = %2.3f%%\n", bestTourLength, i, ((double)(bestTourLength-optimalTour)*100.0)/(double)optimalTour);
            eliteAnt.draw(x,y,0);  //print the map of the best so far tour
        }
        else if (algorithm=='m') {

            IterationCount++;

            if (IterationCount>60) {         //if after 60 iteration not a better tour been found re-initialize the pheromone matrix
                IterationCount=0;
                InitializePheromoneTrail();
            }
        }
        LastbestTourLength=Integer.MAX_VALUE;
    }

    /*
    Calls the evaporate and the deposit Pheromone.
    */
    public void ASPheromoneUpdate()
    {
        if (algorithm=='a' || algorithm=='e') {
        Evaporate();
        for (int k=0; k<m; k++)
            DepositPheromone(k);
        }
        if (algorithm=='e')
            EliteDeposit();   //the elite ants deposits extra pheromone toString() its args
        if (algorithm=='m') {
            MMEvaporate();
            MMDeposit();
        }
    }

    /*
    Evaporates some of the pheromone on every edge at each iteration.
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
    Deposit pheromone on the tour edges an ant an ant has been throw.
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

    public void MMDeposit()
    {
        int i, j, l;
        double q;             // here a pseudorandom rule is implemented
        q=rand.nextDouble();  //produce a random number between 0 an 1
        if (q0<q) {        //if that number is smaller than the parameter q0 the best-iteration-ant deposit pheromone
            double trail = 1.0/(double)eliteAnt.getTourLength();
            for (i=0; i<n; i++) {
                j = eliteAnt.getTourCity(i);
                l = eliteAnt.getTourCity(i+1);
                pheromone[j][l] = pheromone[j][l]+trail;
                if (pheromone[j][l]>tmax)       //the pheromones cant go higher than tmax
                    pheromone[j][l]=tmax;
                pheromone[l][j] = pheromone[j][l];
            }
        }
        else {                     //the best-so-far ant deposit pheromone
            double trail = 1.0/(double)bestTourLength;
            for (i=0; i<n; i++) {
                j = bestTour[i];
                l = bestTour[i+1];
                pheromone[j][l] = pheromone[j][l]+trail;
                if (pheromone[j][l]>tmax)
                    pheromone[j][l]=tmax;
                pheromone[l][j] = pheromone[j][l];
            }
        }
    }

    public void MMEvaporate()
    {
        for (int i=0; i<n; i++)
            for (int j=i+1; j<n; j++) {
                pheromone[i][j] = (1.0-p) * pheromone[i][j];

                if (pheromone[i][j]<tmin)   //the pheromones cant go higher than tmin
                    pheromone[i][j]=tmin;
                pheromone[j][i] = pheromone[i][j];
            }
    }

    /*
    The main class, also parse the parameters and calculates runtime.
    */
    public static void main(String[] args)
    {
        final long startTime = nanoTime();   //calculating run time function
        if(args.length==0) {
            out.println("No parameters!  -h for help");
            terminate();
        }
        parse_args(args);     //import the parameters
        final long duration = nanoTime() - startTime;  //  run time
            out.println("run time: "+duration/1000000000+" sec");
        terminate();
    }

    /*
    Initialize all the parameters by searching for '-'symbol
     */
    public static void parse_args(String[] args)
    {                //set the defaultt parameters
        //char[] city =  {'d','1','9','8','.','t','s','p'};
        String city_choice=new String();
        char algorithm='e';
        char[] param;
        String parameter;
        int m=50, local=1, a=1, b=5, nn=20, o=0, iterations=1000, e=m;
        double p=0.5;   //for the MMAS algorithm p=0.02 is suggested
        double q0=0.9;

        for (int i=0; i<args.length; i++) {
            if (args[i].contains("-i")) {
                param = new char[args[i].length()-2];
                args[i].getChars(2, args[i].length(), param, 0);
                city_choice = new String(param);
            }
            else if (args[i].contains("-m")) {
                param = new char[args[i].length()-2];
                args[i].getChars(2, args[i].length(), param, 0);
                parameter=new String(param);
                m=Integer.parseInt(parameter);
            }
            else if (args[i].contains("-l")) {
                param = new char[args[i].length()-2];
                args[i].getChars(2, args[i].length(), param, 0);
                parameter=new String(param);
                local=Integer.parseInt(parameter);
            }
            else if (args[i].contains("-a"))  {
                param = new char[args[i].length()-2];
                args[i].getChars(2, args[i].length(), param, 0);
                parameter=new String(param);
                a=Integer.parseInt(parameter);
            }
            else if (args[i].contains("-b"))  {
                param = new char[args[i].length()-2];
                args[i].getChars(2, args[i].length(), param, 0);
                parameter=new String(param);
                b=Integer.parseInt(parameter);
            }
            else if (args[i].contains("-o"))  {
                param = new char[args[i].length()-2];
                args[i].getChars(2, args[i].length(), param, 0);
                parameter=new String(param);
                o=Integer.parseInt(parameter);
            }
            else if (args[i].contains("-nn"))  {
                param = new char[args[i].length()-3];
                args[i].getChars(3, args[i].length(), param, 0);
                parameter=new String(param);
                nn=Integer.parseInt(parameter);
            }
            else if (args[i].contains("-t"))  {
                param = new char[args[i].length()-2];
                args[i].getChars(2, args[i].length(), param, 0);
                parameter=new String(param);
                iterations=Integer.parseInt(parameter);
            }
            else if (args[i].contains("-e"))  {
                param = new char[args[i].length()-2];
                args[i].getChars(2, args[i].length(), param, 0);
                parameter=new String(param);
                e=Integer.parseInt(parameter);
            }
            else if (args[i].contains("-c"))
                algorithm=args[i].charAt(2);

            else if (args[i].contains("-p"))  {
                param = new char[args[i].length()-2];
                args[i].getChars(2, args[i].length(), param, 0);
                parameter=new String(param);
                p=Double.parseDouble(parameter);
            }
            else if (args[i].contains("-q"))  {
                param = new char[args[i].length()-2];
                args[i].getChars(2, args[i].length(), param, 0);
                parameter=new String(param);
                q0=Double.parseDouble(parameter);
            }
            else if (args[i].contains("-h")) {
                out.println("Parameters list:\n-i&file name\n-m&numberof ants\n-l& 0 or 1 to activate local surch\n-a&pheromone factor weight\n-b&(1-5) heuristic factor weight\n-o&otimum tour length\n-nn&number of nearest neighbors\n-t&number of iterations\n-p&evaporation factor value\n-e&elite and wheight\n-a&choose algorithm (a-simpleAS, e-EAS, m-MMAS)\n-q0 for pseudorandom factor");
                terminate();
            }
        }
        if (city_choice.length()==0)
            city_choice=new String("d198.tsp");
        AsTSP demo = new AsTSP(m,city_choice, local, a, b, o, nn, iterations, e, algorithm, p, q0);
    }

    public static void terminate()
    {
        exit(0);
    }

}
