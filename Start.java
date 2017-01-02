import java.util.*;
import java.io.*;

public class Start {
    public static void main(String[] args) {
        String dir = "tests/ml-latest-mytest/";
        String movies = dir + "movies.csv";
        String ratings = dir + "ratings.csv";
        Set<Integer> userSet = new HashSet<Integer>();
        Map<Double, Integer> rateCount = new HashMap<Double, Integer>();
        Map<Integer, String> movieName = new HashMap<Integer, String>();
        Map<Integer, Integer> movieIndex = new HashMap<Integer, Integer>();
        int id = 0, voteCount = 0, movieCount = 0;
        Table tb = null;
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
        
        while (true) {
            System.out.println("\n\n1--Print statistics\n2--Print table\n3--Cluster users and movies\n4--Get single group info\nOthers--Exit");
            Scanner sc = new Scanner(System.in);
            int choice = Integer.parseInt(sc.nextLine());
            if (choice == 1) {
                System.out.format("movie: %d, user: %d, vote: %d%n", movieName.size(), userSet.size(), voteCount);
                PriorityQueue<Double> pq = new PriorityQueue<Double>(rateCount.keySet());
                while (!pq.isEmpty()) {
                    Double d = pq.poll();
                    int i = rateCount.get(d);
                    double p = (double)i / voteCount * 100;
                    System.out.format("rate: %.1f, count: %d, percentage: %.2f%%%n", d, i, p);
                }
            }
            else if (choice == 2) {
                if (tb == null) {
                    System.out.println("No valid table");
                    continue;
                }
                System.out.println("Enter the name of output file");
                String outfile = sc.nextLine();
                if (outfile.equals("System.out")) {
                    tb.printTable();
                }
                if (outfile.length() < 5 || !outfile.substring(outfile.length() - 4).equals(".txt")) {
                    outfile += ".txt";
                }
                tb.printTable(outfile);
            }
            else if (choice == 3) {
                if (tb == null) {
                    System.out.println("No valid table");
                    continue;
                }
                int numCluster = 12;
                int rowCount = tb.getRowCount(), colCount = tb.getColCount();
                int numUser = rowCount / numCluster + 1;
                System.out.format("%d users in a cluster.%n", numUser);
                int section = 0;
                while (!userSet.isEmpty()) {
                    int srcUser = -1;
                    for (int i : userSet) {
                        srcUser = i;
                        break;
                    }
                    userSet.remove(srcUser);
                    double[] srcVector = tb.getUserIdRate(srcUser);
                    List<Double> cosVal = new ArrayList<Double>();
                    List<Integer> tarUsers = new ArrayList<Integer>();
                    for (int tarUser : userSet) {
                        double[] tarVector = tb.getUserIdRate(tarUser);
                        double cos = calCosVal(srcVector, tarVector);
                        //double cos = calSimilarity(srcVector, tarVector);
                        for (int i = 0; i <= cosVal.size(); i++) {
                            if (i == cosVal.size() || cos > cosVal.get(i)) {
                                cosVal.add(i, cos);
                                tarUsers.add(i, tarUser);
                                break;
                            }
                        }
                    }
                    tb.swapUser(srcUser, tb.getIndexUser(section * numUser));
                    for (int i = 1; i < numUser && !tarUsers.isEmpty(); i++) {
                        int tarUser = tarUsers.remove(0);
                        tb.swapUser(tarUser, tb.getIndexUser(section * numUser + i));
                        userSet.remove(tarUser);
                    }
                    section++;
                }
                System.out.println("done");
            }
            else if (choice == 4) {
            
            }
            else {
                break;
            }
        }
    }
    
    public static double calCosVal(double[] v1, double[] v2) {
        if (v1.length != v2.length) {
            System.err.println("vector dimension unmatched.");
            return Double.MIN_VALUE;
        }
        double inner = 0.0, uv1 = 0.0, uv2 = 0.0;
        for (int i = 0; i < v1.length; i++) {
            uv1 += v1[i] * v1[i];
            uv2 += v2[i] * v2[i];
            inner += v1[i] * v2[i];
        }
        if (uv1 == 0 || uv2 == 0) return Double.MAX_VALUE;
        else return inner / (Math.sqrt(uv1) * Math.sqrt(uv2));
    }
    
    public static double calSimilarity(double[] v1, double[] v2) {
        if (v1.length != v2.length) {
            System.err.println("vector dimension unmatched.");
            return Double.MIN_VALUE;
        }
        double sum = 0.0;
        for (int i = 0; i < v1.length; i++) {
            if (v1[i] != 0 && v2[i] != 0) {
                sum += Math.abs(v1[i] - v2[i]);
            }
        }
        return sum;
    }
}
