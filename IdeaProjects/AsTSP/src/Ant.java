import java.util.Random;
/**
 * Created by IntelliJ IDEA.
 * User: aris
 * Date: 4/18/11
 * Time: 3:21 PM
 * To change this template use File | Settings | File Templates.
 */

public class Ant implements Runnable{  //ANT object can run as threads

    private int n;
    private int tour_length;
    private int[] tour;
    public int[] index;
    private boolean[] visited = new boolean [n];
    private int[][] distance;
    private double[][] choice_info;
    private int[][] nnList;
    private int nn;
    private int local;
    private Random rand=new Random();

    /*
    The constructor of the ant objects.
    */
    public Ant(int n, int[][] distance, double[][] choice_info, int[][] nnList, int nn, int local) {

        int[] tour_pos = new int [n];
        boolean[] visited = new boolean [n];
        int[] tour = new int [n+1];

        this.n = n;
        this.nnList=nnList;
        this.nn=nn;
        this.local=local;
        this.distance = distance;
        this.choice_info = choice_info;
        this.index=tour_pos;
        this.tour=tour;
        this.visited=visited;
    }

    /*
    Calculates the tour length when called.
    */
    public void cal_tour_length()
    {
        this.tour_length = 0;
        for(int i=0; i < n; i++)
            tour_length += distance[tour[i]][tour[i+1]];
    }

    public final int getTourLength()
    {
        return tour_length;
    }

    public final int getTourCity(int i)
    {
        return tour[i];
    }

    /*
    Construct a new tour for each ant and improve it with local search
    */
    public void ConstructSolutions()
    {
        for (int i=0; i<n; i++)
            visited[i]=false;

        int r = (int) (rand.nextDouble() * n);
        visited[r]=true;
        tour[0]=r;          //choose a random starting city
        index[r]=0;

        for (int i=1; i<n; i++) {
            //ASDecisionRule(i);  // decision rule  which does not uses nnList
            NeighborListASDecisionRule(i); // decision rule  which does not uses nnList for speed up
        }
        tour[n]=tour[0];       //return to starting city
        if (local==1) {        //local optimizations
            Opt2();
            Opt2_5();
        }
    }

    /*
    Prints out the map of the tour.
    */
    public void draw(double [] x, double[] y, int delay)
    {
        DrawTour draw = new DrawTour();
        draw.draw(tour,x,y,0);
    }

    /*
    Decides which city an ant in a current city should go to next.
    */
    public void ASDecisionRule(int step)
    {
        int c, j;
        double sum_probabilities=0.0;
        double[] selection_probability= new double[n];
        double r, p;
        boolean v;

        c = tour[step-1];
        for (j=0; j<n; j++) {
            v =  visited[j];
            if ( v == true )
                selection_probability[j]=0.0;
            else {
                selection_probability[j] = choice_info[c][j];
                sum_probabilities += selection_probability[j];
            }
        }
        r = rand.nextDouble() *  sum_probabilities;
        j=0;
        p = selection_probability[j];
        while (p<r) {
            j++;
            p+=selection_probability[j];
        }

        tour[step] = j;
        visited[j] = true;

    }

    /*
    Decides which city an ant in a current city should go to next. Uses the nearest neighbor list to speed up the decision.
    uses the roulette wheel selection so there is a random chance of choosing a city
    influenced by the amount of choice info on the edges between the city where the ant is currently at and cities within a feasible neighborhood
    */
    public void NeighborListASDecisionRule(int step)
    {
        int c, j;

        double sum_probabilities;
        double[] selection_probability= new double[n];
        double r, p;

        c = tour[step-1];
        sum_probabilities=0.0;

        for (j=0; j<nn; j++) {
            if ( visited[nnList[c][j]] == true )
                selection_probability[j]=0.0;
            else {
                selection_probability[j]=choice_info[c][nnList[c][j]];
                sum_probabilities = sum_probabilities + selection_probability[j];
            }
        }
        if (sum_probabilities==0)
            ChooseBestNext(step);
        else {
            r = rand.nextDouble() *  sum_probabilities;
            j=0;
            p = selection_probability[j];
            while (p<r) {
                j++;
                p = p+selection_probability[j];
            }
            tour[step] = nnList[c][j];
            index[nnList[c][j]]=step;
            visited[nnList[c][j]] =  true;
        }
    }

    /*
    Used by the NeighborListASDecisionRule to make the ant go to the city with the highest choice information
    */
    public void ChooseBestNext(int step)
    {
        int c, j, nc=0;
        double u=0.0;

        c = tour[step-1];
        for(j=0; j<n; j++)
            if ( visited[j] == false )
                if (choice_info[c][j] > u ) {
                    nc = j;
                    u = choice_info[c][j];
                }
        tour[step] = nc;
        index[nc]=step;
        visited[nc] = true;
    }

    /*
    Performs localSearch to the found tour by the ant.
    */
    public void Opt2_5()
    {
        double maxGain;
        double gain;
        boolean isChange;
        boolean[] dontLookBit = new boolean[n];

        for (int j=0; j<n; j++)
            dontLookBit[j]=false;

        do {
            isChange=false;

            for (int i=1; i<n-1; i++) {  //for every city try 2-opt
                if (dontLookBit[i-1]==false) {
                    int a = tour[i-1];
                    int b = tour[i];
                    int e = tour[i+1];
                    int c,d;
                    maxGain = 0;
                    gain = 0;
                    int dPos=0;
                    for (int j=i+2; j<n-1; j++) {
                        if ( (j!=i) && (j != (i-1)) && (j != (i+1)) && ((j-1)!=i) && ((j-1) != (i-1)) && ((j-1) != (i+1)) )
                        {
                            c = tour[j];
                            d = tour[j-1];
                        //  calculate if there is gain

                            gain = ( distance[a][b] + distance[b][e] + distance[d][c] ) - ( distance[a][e] + distance[d][b] + distance[b][c] );

                            if (gain > maxGain) {    // find the most gainful move
                                maxGain = gain;
                                dPos = j-1;
                            }
                        } //end if
                    } //end for j
                    if (maxGain > 0 ) {
                        if (i < dPos)
                            moveLeft(i, dPos);
                        isChange = true;
                        dontLookBit[i-1]=false;
                        dontLookBit[i]=false;
                        dontLookBit[i+1]=false;
                        dontLookBit[dPos]=false;
                        dontLookBit[dPos+1]=false;
                    } //end if
                    else
                        dontLookBit[i-1]=true;
                }
            }// end for i
        }while (isChange==true);
    }

    public void moveLeft (int bPos, int dPos)
    {
        int temp=tour[bPos];
        for(int i=bPos; i<dPos; i++) {
            tour[i]=tour[i+1];
        }
        tour[dPos]=temp;

    }

    /*
    Performs localSearch to the found tour by the ant.
    */
    public void Opt2()
    {
        double maxGain;
        double gain;
        boolean isChange;
        boolean[] dontLookBit = new boolean[n+1];

        for (int j=0; j<=n; j++)
            dontLookBit[j]=false;

        do {
            isChange=false;

            for (int i=1; i<n; i++) {  //for every city try 2-opt
                if (dontLookBit[i-1]==false) {
                    int a = tour[i-1];
                    int b = tour[i];
                    int c,d;
                    maxGain = 0;
                    gain = 0;
                    int maxEdge = 0;
                    for (int j=1; j<=n; j++) {
                        if ( (j!=i) && ((j-1)!=i) && ((j-1) != (i-1)) && (j != (i-1)))
                        {
                            c = tour[j];
                            d = tour[j-1];
                            //  calculate if there is gain

                            gain = ( distance[a][b] + distance[c][d] ) - ( distance[a][d] + distance[b][c] );

                            if (gain > maxGain) {    // find the most gainful move
                                maxGain = gain;
                                maxEdge = j-1;
                            }
                        } //end if
                    } //end for j
                    if (maxGain > 0 ) {     // if there is gain make the 2-opt move (flip 2 cities)
                        int u;
                        int v;

                        for ( u = maxEdge, v = i; u>=i;  u--, v++)
                        {
                            if (v < u)         //  change also the order of the cities (u--)
                                flip(v, u);
                        }
                        isChange = true;
                        dontLookBit[i-1]=false;
                        dontLookBit[i]=false;
                        dontLookBit[maxEdge]=false;
                        dontLookBit[maxEdge+1]=false;
                    } //end if
                else
                    dontLookBit[i-1]=true;
                }
            }// end for
        }while (isChange==true);
    }

    // flip 2 cities in the tour array. Used by the 2-opt method.
    public void flip (int i, int j)
    {
        int temp1, temp2;
        int a=tour[i], b=tour[j];

        temp1 = tour[i];
        temp2 = index[a];

        tour[i] = tour[j];
        index[a] = index[b];

        tour[j] = temp1;
        index[b] = temp2;
    }

    public void run() {   //code that will be executed whet a thread starts
        ConstructSolutions();
    }
}