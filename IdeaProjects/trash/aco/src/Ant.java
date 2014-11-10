import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: aris
 * Date: 4/18/11
 * Time: 3:21 PM
 * To change this template use File | Settings | File Templates.
 */

public class Ant implements Runnable{

    private int n;
    private int tour_length;
    private int[] tour;
    private boolean[] visited;
    private int[][] distance;
    private double[][] choice_info;
    private Random rand=new Random();
    int step, nn;
    int[][] nn_list;
    double q0;

    /*
    The constructor of the ant objects.
    */
    public Ant(int n, int[][] distance, double[][] choice_info) {

        this.n = n;

        this.distance = distance;
        this.choice_info = choice_info;
        boolean[] visited = new boolean [n];
        this.visited = visited;
        int[] tour = new int [n+1];
        this.tour = tour;
    }

    /*
    Calculates the tour length when called.
    */
    public void cal_tour_length()
    {
        this.tour_length = 0;
        for(int i=0; i < n; i++) {
            tour_length += distance[tour[i]][tour[i+1]];
        }
    }

    /*
    Getter for the tourLength.
    */
    public final int getTourLength()
    {
        return tour_length;
    }

    /*
    Getter for specific tour[i]
    */
    public final int getTourCity(int i)
    {
        return tour[i];
    }

    /*
    Sets city i visited to false or true in the visited array
    */
    public void setVisited(int i, boolean state)
    {
        visited[i] = state;
    }

    /*
    Place city to position i in the tour array.
    */
    public void setTourCity(int i, int city)
    {
        tour[i] = city;
    }

    /*
    Prints out the map of the tour.
    */
    public void draw(double[] x, double[] y, int delay)
    {
        DrawTour draw = new DrawTour();
        draw.draw(tour,x,y,0);
    }

    public void setPseudoRandomDecisionRuleVal(int step, int[][] nn_list, int nn, double q0) {
        this.step = step;
        this.nn_list=nn_list;
        this.nn = nn;
        this.q0 = q0;
    }

    /*
    Decides which city an ant in a current city should go to next in the ACS algorithm using a pseudorandom decisionrule.
    */
    public void PseudoRandomDecisionRule(int step, int[][] nn_list, int nn, double q0)
    {
        int c, j;
        double sum_probabilities;
        double[] selection_probability= new double[n];
        double r, p, q;

        q = rand.nextDouble();

        if(q<=q0) {
            ChooseBestNext(step);
        }
        else {
            c = tour[step-1];
            sum_probabilities=0.0;
            for (j=0; j<nn; j++) {
                if ( visited[nn_list[c][j]] == true ) {
                    selection_probability[j]=0.0;
                }
                else {
                    selection_probability[j]=choice_info[c][nn_list[c][j]];
                    sum_probabilities = sum_probabilities + selection_probability[j];
                }
            }
            if (sum_probabilities==0) {
                ChooseBestNext(step);
            }
            else {
                r = rand.nextDouble() *  sum_probabilities;
                j=0;
                p = selection_probability[j];
                while (p<r) {
                    j++;
                    p = p+selection_probability[j];
                }
                tour[step] = nn_list[c][j];
                visited[nn_list[c][j]] =  true;
            }
        }
    }

    /*
    Used by the PseudoRandomDecisionRule to make the ant go to the city with the highest choice information when in a current city.
    */
    public void ChooseBestNext(int step)
    {
        int c, j, nc=0;
        double u=0.0;

        c = tour[step-1];
        for(j=0; j<n; j++) {
            if ( visited[j] == false )
                if (choice_info[c][j] > u ) {
                    nc = j;
                    u = choice_info[c][j];
                }
        }
        tour[step] = nc;
        visited[nc] = true;
    }

    /*
    Performs localSearch to the found tour by the ant.
    */
    public boolean Opt2()
    {
        double maxGain=0;
        double gain=0;
        boolean isChange=false;

        for (int i=1; i<=n; i++) {  //for every city try 2-opt
            int a = tour[i-1];
            int b = tour[i];
            int c,d;
            maxGain = 0;
            gain = 0;
            int maxEdge1 = 0;

            for (int j=1; j<=n; j++) {
                if ( (j!=i) && ((j-1)!=i) && ((j-1) != (i-1)) && (j != (i-1)))
                {
                    c = tour[j];
                    d = tour[j-1];
                    //  calculate if there is gain

                    gain = ( distance[a][b] + distance[c][d] ) - ( distance[a][d] + distance[b][c] );

                    if (gain > maxGain) {    // find the most gainful move
                        maxGain = gain;
                        maxEdge1 = j-1;
                    }
                } //end if
            } //end for j
            if (maxGain > 0 ) {     // if there is gain make the 2-opt move (flip 2 cities)
                int u;
                int v;

                for ( u = maxEdge1, v = i; u>=i;  u--, v++)
                {
                    if (v < u) {        //  change also the order of the cities (u--)
                        flip(v, u);
                    }
                }
                isChange = true;
            } //end if
        }// end for i
        return isChange;
    }

    // flip 2 cities in the tour array. Used by the 2-opt method.
    public void flip (int a, int b)
    {
        int temp;

        temp = tour[a];
        tour[a] = tour[b];
        tour[b] = temp;
    }



    public void run() {
        PseudoRandomDecisionRule(step,nn_list,nn,q0);
    }
}
