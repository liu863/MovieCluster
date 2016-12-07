import java.util.*;

public class Test {
    public static void main(String[] args) {
        List<double[]> list = new ArrayList<double[]>();
        double[] d1 = {1.5, 2.5};
        double[] d2 = {2.5, 3.5};
        list.add(d1);
        list.add(d2);
        list.add(null);
        list.add(null);
        list.add(d2);
        System.out.println(list.size());
        System.out.println(list.get(4)[1]);
        list.remove(3);
        list.add(3, d1);
        System.out.println(list.get(3)[0]);
    }
}