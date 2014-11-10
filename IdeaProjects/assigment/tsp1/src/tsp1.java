import static java.lang.System.nanoTime;
import static java.lang.System.out;

public class tsp1 {

    int bestTourLength;
    int n=14;   // number of cities
    double[][] coord = new double [n][2];
    double[][] distance = new double [n][n];     // pre-calculated distances

    public static void main(String[] args) {

        final long startTime = nanoTime();   //calculating run time function

        int[] perm = new int[14];

        tsp1 test = new tsp1();
        test.easySolve(perm);

         final long duration = nanoTime() - startTime;  //  run time
         out.println("run time: "+duration);
    }

    public void fillDistance () {
        for ( int i=0; i<n; i++)
            for ( int j=0; j<n; j++) {
                double dx = coord[i][0] - coord[j][0], dy = coord[i][1] - coord[j][1];
                distance[i][j] = (int) (Math.sqrt(dx * dx + dy * dy) + 0.5);
            }
    }

    public void fillCoord () {    // fill up the coordinates

            coord[0][0] = 2868972;
            coord[0][1] = 789594;
            coord[1][0] = 2859098;
            coord[1][1] = 753186;
            coord[2][0] = 2900512;
            coord[2][1] = 778672;
            coord[3][0] = 2817379;
            coord[3][1] = 741988;
            coord[4][0] = 2842471;
            coord[4][1] = 763200;
            coord[5][0] = 2887468;
            coord[5][1] = 762420;
            coord[6][0] = 2827542;
            coord[6][1] = 714041;
            coord[7][0] = 2860737;
            coord[7][1] = 729751;
            coord[8][0] = 2837746;
            coord[8][1] = 740969;
            coord[9][0] = 2849437;
            coord[9][1] = 692353;
            coord[10][0] = 2875162;
            coord[10][1] = 749108;
            coord[11][0] = 2818536;
            coord[11][1] = 701642;
            coord[12][0] = 2887262;
            coord[12][1] = 743972;
            coord[13][0] = 2796999;
            coord[13][1] = 756023;

    }

    public void easySolve(int[] perm) {

        bestTourLength = Integer.MAX_VALUE;
        fillCoord();
        fillDistance();
        
        for (int i = 1; i < (n); i++)
            perm[i] = i;  // fill the permutations array

        search(n - 1, perm);
        System.out.println("the bestTourLength is: "+bestTourLength);
    }


    public void search(int m, int[] perm) {

        if (m == 1) {

            int val = tourLength(perm);
            if (val < bestTourLength) {
                bestTourLength = val;
        }

        }
        else {
            for (int i = 0; i < (m-1); i++) {
                swap(i, m - 1, perm);    //  swap so we get every different combination
                search(m - 1, perm);
                swap(i, m - 1, perm);
            }
        }
    }

    public int tourLength(int[] perm) {   // calculate tour length

        int sum = length(perm[n - 1], perm[0]);

        for (int i = 1; i < n; i++) {
            if (sum >= bestTourLength)
                break;
            else
                sum += length(perm[i - 1], perm[i]);
        }
        return sum;
    }

    public int length(int a, int b) {

       return (int) distance[a][b];
       
    }

    public void swap(int i, int j, int[] perm) {

        int temp = perm[i];

        perm[i] =  perm[j];
        perm[j] = temp ;
    }


}