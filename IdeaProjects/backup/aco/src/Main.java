/**
 * Created by IntelliJ IDEA.
 * User: aris
 * Date: 4/18/11
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    public static void main(String[] args)
    {
        int city_choice = Integer.parseInt(args[0]);
        int n=280;   // number of cities ***change!!!!!!!***
        int m=6; // number of Ants

        Control demo = new Control(n,m,city_choice);
    }
}
