import com.sun.deploy.net.proxy.StaticProxyManager;

/**
 * Created by IntelliJ IDEA.
 * User: aris
 * Date: 4/18/11
 * Time: 3:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class Ant{

    private int n;
    private double[][] distances;
    private boolean[] visited;
    private int[] tour;

    public Ant(int n, double[][] distances) {

        this.n = n;
        this.distances = distances;
        boolean[] visited = new boolean [n];
        this.visited = visited;
        int[] tour = new int [n+1];
        this.tour = tour;
    }

    public double tour_length()
    {
        double tourLength=0;
        for(int i=0; i < n; i++) {
                tourLength += distances[tour[i]][tour[i+1]];
        }
        return tourLength;
    }

    public boolean getVisited(int i)
    {
        return visited[i];
    }

    public void setVisited(int i, boolean statement)
    {
        this.visited[i] = statement;
    }

    public void setTour(int i, int city)
    {
        this.tour[i] = city;
    }

    public int getTourCity(int i)
    {
        int city = tour[i];
        return city;
    }
}
