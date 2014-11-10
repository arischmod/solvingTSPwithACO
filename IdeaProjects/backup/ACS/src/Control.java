import sun.awt.Symbol;

import static java.lang.Math.*;

/**
 * Created by IntelliJ IDEA.
 * User: aris
 * Date: 4/17/11
 * Time: 8:48 PM
 * To change this template use File | Settings | File Templates.
 */

public class Control{

    private boolean isChange;
    private int n;
    private int m;
    private double[] tour_length;
    int[] bestTour;
    int kali=50000;
    double ultimate=Double.MAX_VALUE;

    public Control(int n, int m, int city_choice) {
        /*

        while (not terminate) {

        } */
        int k, i;
        double[] tour_length = new double[m];
        int[] bestTour = new int[n+1];

        this.n=n;
        this.m=m;
        this.tour_length=tour_length;
        this.bestTour=bestTour;

        InitializeData data = new InitializeData(n,m,city_choice);

        for (i =0; i<kali; i++) {
          if (i==5000 || i==10000 || i==20000 || i==30000 || i==40000) {
               for (k=0; k<m; k++) {
                this.tour_length[k] = data.getAnts(k).tour_length();
            }
            k = 0;
            for (k=0; k<m; k++) {
                this.tour_length[0] = data.getAnts(k).tour_length();
            }
            for (k=0; k<m; k++) {
                System.out.println(" ");
                System.out.println("ant["+(k+1)+"]: tour length:"+tour_length[k]);
            }
            System.out.println("******************** ");
          }

            ConstructSolutions(data);
            /*k = 0;
            for (k=0; k<m; k++) {
                tour_length[0] = data.getAnts(k).tour_length(n);
            }
            for (k=0; k<m; k++) {
                System.out.println(" ");
                System.out.println("ant["+(k+1)+"]: tour length:"+tour_length[k]);
            } */

           /* for (k=0; k<m; k++)
            {
                this.isChange = true;
                while( isChange == true)
                {
                    this.isChange = false;
                    Opt2(k, data);
                }
            }     */
            /*k = 0;
            tour_length[0] = data.getAnts(k).tour_length(n);
            System.out.println(" ");
            System.out.println("ant["+(k+1)+"]: tour length:"+tour_length[k]);

            /*if (i == 49999)
                   for (k=0; k<n; k++) {
                        for (int j=0; j<n; j++) {
                            System.out.println(" ");
                                System.out.println("Choice_info["+(k)+"]["+(j)+"]: "+data.getChoice_info(k,j));
                        }
                    }         */

            ASPheromoneUpdate(data);

        }

        for (k=0; k<m; k++) {
            this.tour_length[k] = data.getAnts(k).tour_length();
        }
        k = 0;
            for (k=0; k<m; k++) {
                this.tour_length[0] = data.getAnts(k).tour_length();
            }
            for (k=0; k<m; k++) {
                System.out.println(" ");
                System.out.println("ant["+(k+1)+"]: tour length:"+tour_length[k]);
            }
        System.out.println("n_neighbor="+data.getNntl()+" ******* ACO="+ultimate);
    }

    /*public static void Terminate()
    {
            demo.Close();                          k
    }*/

    public void ConstructSolutions(InitializeData data)
    {
        int k, r;

        for (k=0; k<m; k++) {
            for (int i=0; i<n; i++) {
                data.getAnts(k).setVisited(i , false);
            }
        }

        int step=0;

        for ( k=0; k<m; k++ ) {
            r = (int) (random() * n);
            data.getAnts(k).setVisited(r , true);
            data.getAnts(k).setTour(step , r);
        }

        while (step<n) {
            step++;
            for (k=0; k<m; k++)
            {
                ACSDecisionRule(k, step, data);
                /*NeighborListASDecisionRule(k, step, data, data.getNN_list());
                int[][] tours = new int[m][n+1];
                for (k=0; k<m; k++) {
                    for (int i=0; i<n+1; i++) {
                        tours[k][i] = data.getAnts(k).getTourCity(i);
                    }
                } */
            }
        }

        for (k=0; k<m; k++) {
            data.getAnts(k).setTour(n, data.getAnts(k).getTourCity(0));
            this.tour_length[k] = data.getAnts(k).tour_length();
        }
    }

    public void NeighborListASDecisionRule(int k, int step, InitializeData data, int[][] nn_list)
    {
        int c, j;

        double sum_probabilities=0.0;
        double[] selection_probability= new double[n];
        double r, p;
        boolean v;

        c = data.getAnts(k).getTourCity(step-1);
        for (j=0; j<n; j++) {
            v =  data.getAnts(k).getVisited(nn_list[c][j]);
            if ( v == true ) {
                selection_probability[j]=0.0;
            }
            else {
                selection_probability[j]=data.getChoice_info(c, nn_list[c][j]);
                sum_probabilities+=selection_probability[j];
            }
        }
        if (sum_probabilities==0) {
            ChooseBestNext( k, step, data);
        }
        else {
            r = (random() * sum_probabilities);
            j=0;
            p = selection_probability[j];
            while (p<r) {
                j++;
                p+=selection_probability[j];
            }
            data.getAnts(k).setTour(step, nn_list[c][j]);
            data.getAnts(k).setVisited(nn_list[c][j], true);
        }
    }

    public void ChooseBestNext(int k, int step, InitializeData data)
    {
        int c, j, nc=0;
        double u=0;

        c = data.getAnts(k).getTourCity(step-1);
        for(j=0; j<n; j++) {
            boolean v =  data.getAnts(k).getVisited(j);
            if ( v != true )
                if (data.getChoice_info(c,j) > u ) {
                    nc = j;
                    u = data.getChoice_info(c,j);
                }
        }
        data.getAnts(k).setTour(step,nc);
        data.getAnts(k).setVisited(nc, true);
    }

    public void ASDecisionRule(int k, int step, InitializeData data)
    {
        int c, j;
        double sum_probabilities=0.0;
        double[] selection_probability= new double[n];
        double r, p;
        boolean v;

        c = data.getAnts(k).getTourCity(step-1);
        for (j=0; j<n; j++) {
            v =  data.getAnts(k).getVisited(j);
            if ( v == true ) {
                selection_probability[j]=0.0;
            }
            else {
                selection_probability[j] = data.getChoice_info(c, j);
                sum_probabilities += selection_probability[j];
            }
        }
        r = (random() * sum_probabilities);
        j=0;
        p = selection_probability[j];
        while (p<r) {
            j++;
            p+=selection_probability[j];
        }
        data.getAnts(k).setTour(step, j);
        data.getAnts(k).setVisited(j, true);
    }

    public void ACSDecisionRule(int k, int step, InitializeData data)
    {
        int c, j;
        double sum_probabilities=0.0;
        double[] selection_probability= new double[n];
        double r, p;
        double q0=0.9;
        double q;
        double argmax=0;
        int maxcity=0;
        boolean v;

        c = data.getAnts(k).getTourCity(step-1);
        q = random();
        if (q<=q0) {
            for (j=0; j<n; j++) {
                if (data.getChoice_info(c, j)>argmax && data.getAnts(k).getVisited(j)==false) {
                    argmax=data.getChoice_info(c, j);
                    maxcity=j;
                }
            }
            data.getAnts(k).setTour(step, maxcity);
            data.getAnts(k).setVisited(maxcity, true);
        }
        else {

        for (j=0; j<n; j++) {
            v =  data.getAnts(k).getVisited(j);
            if ( v == true ) {
                selection_probability[j]=0.0;
            }
            else {
                selection_probability[j] = data.getChoice_info(c, j);
                sum_probabilities += selection_probability[j];
            }
        }
        r = (random() * sum_probabilities);
        j=0;
        p = selection_probability[j];
        while (p<r) {
            j++;
            p+=selection_probability[j];
        }
        data.getAnts(k).setTour(step, j);
        data.getAnts(k).setVisited(j, true);
        Evaporate(data, c, j);
        }
    }

    public void Evaporate(InitializeData data, int i, int j)
    {
        double x = 0.1;
        double initialPheromone=1/(n*data.getNntl());

                data.setPheromone(i, j, (1-x) * data.getPheromone(i,j)+x*initialPheromone);
                data.setPheromone(j, i, data.getPheromone(i,j));
    }

    public void DepositPheromone( InitializeData data)
    {
        int i,j,l;
        double p = 0.1;
        double trail;
        trail = 1/ultimate;

        for (i=0; i<n; i++) {
            j = bestTour[i];
            l = bestTour[i+1];
            data.setPheromone(j,l,((1-p)*data.getPheromone(j,l)+p*trail));
            data.setPheromone(l,j,data.getPheromone(j,l));
        }
    }

    public void ASPheromoneUpdate(InitializeData data)
    {
        int k, bestAnt=0;
        for (k=0; k<m; k++) {
            this.tour_length[0] = data.getAnts(k).tour_length();
        }
        for (k=1; k<m; k++) {
            if (tour_length[k] < tour_length[bestAnt]) {
                    bestAnt = k;
            }
        }
        if (tour_length[bestAnt]<ultimate) {
            ultimate = tour_length[bestAnt];
            System.out.println(" ########           yea! dude!  ****  "+ultimate);
            for (int i=0; i<n; i++) {
                bestTour[i]=data.getAnts(bestAnt).getTourCity(i);
            }
        }
        DepositPheromone(data);
    }

    public void Opt2(int k, InitializeData data)
    {
                double maxGain=0;
                double gain=0;

                for (int i=1; i<(n-1); i++) {  //for every city try 2-opt

                    int a = data.getAnts(k).getTourCity(i);
                    int b = data.getAnts(k).getTourCity(i-1);
                    int c,d;
                    maxGain = 0;
                    gain = 0;
                    int maxEdgeA = 0;
                    int maxEdgeB = 0;
                    int j;

                    for (j=0; j<(n-3); j++) {
                        if ( (j!=i) && ((j+1)!=i) && ((j+1) != (i-1)) && (j != (i-1)))
                        {
                            c = data.getAnts(k).getTourCity(j);
                            d = data.getAnts(k).getTourCity(j+1);
                             //  calculate if there is gain

                            gain = ( data.getDistance(a,b) + data.getDistance(c,d) ) - ( data.getDistance(a,d) + data.getDistance(b,c) );

                            if (gain > maxGain) {    // find the most gainful move
                                maxGain = gain;
                                maxEdgeA = b;
                                maxEdgeB = d;
                                //System.out.println("ant:"+k+" = ");
                            }
                        } //end if
                    } //end for j

                    if (maxGain > 0 ) {     // if there is gain make the 2-opt move (flip 2 cities)
                        int u;
                        int v;
                        //System.out.println("2 opt found");
                        for ( u = maxEdgeA, v = maxEdgeB; u>=maxEdgeB;  u--, v++)
                        {
                            if (v < u) {        //  change also the order of the cities (u--)
                                int temp=data.getAnts(k).getTourCity(v);
                                data.getAnts(k).setTour(v,data.getAnts(k).getTourCity(u));
                                data.getAnts(k).setTour(u,temp);
                                //swap(data, k, v, u);
                                this.isChange = true;
                                //System.out.println("after swap:  a="+data.getAnts(k).getTourCity(i-1)+"  b="+data.getAnts(k).getTourCity(i)+"  c="+data.getAnts(k).getTourCity(maxEdgeA)+"  d="+data.getAnts(k).getTourCity(maxEdgeB));
                            }
                        }
                    } //end if
                    else {
                        this.isChange = false;
                        //System.out.println("no 2-opt");
                    }
                }// end for i
    }


    public void LocalSearch()
    {

    }

    public void UpdateStatistics()
    {

    }

}
