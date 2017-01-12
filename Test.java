import java.util.*;
import java.io.*;

public class Test {
    static Table tb = null, originalTable = null;
    public static void main(String[] args) {
        String dir = "tests/ml-latest-mytest/";
        String movies = dir + "movies.csv";
        String ratings = dir + "ratings.csv";
        Set<Integer> userSet = new HashSet<Integer>();
        Map<Double, Integer> rateCount = new HashMap<Double, Integer>();
        Map<Integer, String> movieName = new HashMap<Integer, String>();
        Map<Integer, Integer> movieIndex = new HashMap<Integer, Integer>();
        int id = 0, voteCount = 0, movieCount = 0;
        try {
            BufferedReader movieReader = new BufferedReader(new FileReader(movies));
            String line = movieReader.readLine();
            while ((line = movieReader.readLine()) != null) {
                //System.out.println(line);
                String[] val = line.split(",");
                int movieId = Integer.parseInt(val[0]);
                movieName.put(movieId, val[1]);
                movieIndex.put(movieId, movieCount);
                movieCount++;
            }
            tb = new Table(movieName, movieIndex);
            
            BufferedReader rateReader = new BufferedReader(new FileReader(ratings));
            line = rateReader.readLine();
            double[] rates = null;
            while ((line = rateReader.readLine()) != null) {
                //System.out.println(line);
                String[] val = line.split(",");
                int nextId = Integer.parseInt(val[0]);
                if (userSet.add(nextId)) {
                    tb.addUser(nextId);
                    if (rates != null) {
                        tb.addAllRate(id, rates);
                    }
                    rates = new double[tb.getColCount()];
                    id = nextId;
                }
                int movieId = Integer.parseInt(val[1]);
                if (movieIndex.containsKey(movieId)) {
                    double rate = Double.parseDouble(val[2]);
                    rates[movieIndex.get(movieId)] = rate;
                    
                    if (rateCount.containsKey(rate)) {
                        rateCount.put(rate, rateCount.get(rate) + 1);
                    }
                    else {
                        rateCount.put(rate, 1);
                    }
                    voteCount++;
                }
            }
            if (rates != null) {
                tb.addAllRate(id, rates);
            }
            movieReader.close();
            rateReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (tb == null) {
            System.err.println("No table exist");
            System.exit(1);
        }
        originalTable = tb.copy();
        /*
        System.out.format("movie: %d, user: %d, vote: %d%n", movieName.size(), userSet.size(), voteCount);
        PriorityQueue<Double> pq = new PriorityQueue<Double>(rateCount.keySet());
        while (!pq.isEmpty()) {
            Double d = pq.poll();
            int i = rateCount.get(d);
            double p = (double)i / voteCount * 100;
            System.out.format("rate: %.1f, count: %d, percentage: %.2f%%%n", d, i, p);
        }
        */
        //tests
        //test0();
        //test1();
        test2();
        /*
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
         */
    }
    
    private static void test0() {
        System.out.println("\n\ntest0");
        double[] rates;
        /*
        rates = tb.getMovieIdRate(0);
        System.out.println(Arrays.toString(rates));
        rates = tb.getMovieIdRate(1);
        System.out.println(Arrays.toString(rates));
        rates = tb.getMovieIdRate(12);
        System.out.println(Arrays.toString(rates));
        rates = tb.getMovieIdRate(30);
        System.out.println(Arrays.toString(rates));
        rates = tb.getMovieIdRate(31);
        System.out.println(Arrays.toString(rates));
        */
        rates = tb.getMovieIndexRate(-1);
        System.out.println(Arrays.toString(rates));
        rates = tb.getMovieIndexRate(0);
        System.out.println(Arrays.toString(rates));
        rates = tb.getMovieIndexRate(15);
        System.out.println(Arrays.toString(rates));
        rates = tb.getMovieIndexRate(29);
        System.out.println(Arrays.toString(rates));
        rates = tb.getMovieIndexRate(30);
        System.out.println(Arrays.toString(rates));
    }
    
    private static void test1() {
        System.out.println("\n\ntest1");
        double[] rates;
        Set<Integer> userSet = tb.getUserSet();
        for (Integer i : userSet) {
            System.out.println(Arrays.toString(tb.getUserIdRate(i)));
        }
    }
    
    private static void test2() {
        double[] d1 = {1.0, 1.0, 2.0};
        double[] d2 = {1.0, 2.0, 2.0};
        double[] d3 = {2.0, -2.0, 2.0};
        double[] d4 = {1.0, 1.0};
        double[] d5 = {-1.0, 0.0};
        System.out.println(Start.calCosVal(d1, d2));
        System.out.println(Start.calCosVal(d2, d1));
        System.out.println(Start.calCosVal(d2, d3));
        System.out.println(Start.calCosVal(d1, d3));
        System.out.println(Start.calCosVal(d4, d5));
    }
}