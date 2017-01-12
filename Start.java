import java.util.*;
import java.io.*;

class Tuple {
    int id;
    double cos;
    Tuple(int id, double cos) {
        this.id = id;
        this.cos = cos;
    }
}

public class Start {
    private static final int numCluster = 50;
    public static void main(String[] args) {
        String dir = "tests/ml-latest-small/";
        String movies = dir + "movies.csv";
        String ratings = dir + "ratings.csv";
        Set<Integer> userSet = new HashSet<Integer>();
        Map<Double, Integer> rateCount = new HashMap<Double, Integer>();
        Map<Integer, String> movieName = new HashMap<Integer, String>();
        Map<Integer, Integer> movieIndex = new HashMap<Integer, Integer>();
        int id = 0, voteCount = 0, movieCount = 0;
        Table tb = null, originalTable = null;
        try {
            BufferedReader movieReader = new BufferedReader(new FileReader(movies));
            String line = movieReader.readLine();
            while ((line = movieReader.readLine()) != null) {
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
        
        while (true) {
            System.out.println("\n1 - Iinput statistics");
            System.out.println("2 - Original table");
            System.out.println("3 - Print table");
            System.out.println("4 - Cluster users and movies");
            System.out.println("5 - Single group info");
            System.out.println("6 - Table statistics");
            System.out.println("Others--Exit");
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
                System.out.println("Enter the name of output file");
                String outfile = sc.nextLine();
                if (outfile.equals("System.out")) {
                    originalTable.printTable();
                }
                if (outfile.length() < 5 || !outfile.substring(outfile.length() - 4).equals(".txt")) {
                    outfile += ".txt";
                }
                originalTable.printTable(outfile);
            }
            else if (choice == 3) {
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
            else if (choice == 4) {
                long start = System.currentTimeMillis();
                clusterTable(tb, true);
                clusterTable(tb, false);
                long end = System.currentTimeMillis();
                System.out.println("Running time: " + (end - start) + "ms");
            }
            else if (choice == 5) {
                System.out.format("Enter the group id(0 - %d).%n", numCluster * numCluster - 1);
                int group = Integer.parseInt(sc.nextLine());
                System.out.println("Enter the name of output file");
                String outfile = sc.nextLine();
                if (outfile.length() < 5 || !outfile.substring(outfile.length() - 4).equals(".txt")) {
                    outfile += ".txt";
                }
                printGroupInfo(tb, group, outfile);
            }
            else if (choice == 6) {
                System.out.println("Enter the name of output file");
                String outfile = sc.nextLine();
                if (outfile.length() < 5 || !outfile.substring(outfile.length() - 4).equals(".txt")) {
                    outfile += ".txt";
                }
                printTableInfo(tb, outfile);
            }
            else {
                break;
            }
        }
    }
    
    private static void printTableInfo(Table tb, String outfile) {
        int rowCount = tb.getRowCount(), colCount = tb.getColCount();
        int rowClusterSize = rowCount / numCluster, rowRedundant = rowCount % numCluster;
        int colClusterSize = colCount / numCluster, colRedundant = colCount % numCluster;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
            StringBuilder summary = new StringBuilder(), individual = new StringBuilder();
            summary.append(String.format("%d clusters, ", numCluster * numCluster));
            summary.append(String.format("%d - %d users and ", rowClusterSize, rowClusterSize + 1));
            summary.append(String.format("%d - %d movies per cluster%n", colClusterSize, colClusterSize + 1));
            double tableSimilarity = 0.0, validClusterCount = 0.0;
            for (int i = 0; i < numCluster; i++) {
                for (int j = 0; j < numCluster; j++) {
                    if (j > 0) {
                        individual.append(',');
                    }
                    int rowStart = i * (rowClusterSize + 1) - (i > rowRedundant ? i - rowRedundant : 0);
                    int rowEnd = rowStart + (i >= rowRedundant ? rowClusterSize - 1 : rowClusterSize);
                    int colStart = j * (colClusterSize + 1) - (j > colRedundant ? j - colRedundant : 0);
                    int colEnd = colStart + (j >= colRedundant ? colClusterSize - 1 : colClusterSize);
                    int vectorLength = colEnd - colStart + 1;
                    double[] center = new double[vectorLength];
                    List<double[]> vectors = new ArrayList<double[]>();
                    for (int r = rowStart; r <= rowEnd; r++) {
                        boolean valid = false;
                        double[] vec = new double[vectorLength];
                        for (int c = colStart; c <= colEnd; c++) {
                            vec[c - colStart] = tb.getSingleIndexRate(r, c);
                            center[c - colStart] += vec[c - colStart];
                            if (!valid && vec[c - colStart] != 0) valid = true;
                        }
                        if (valid) vectors.add(vec);
                    }
                    if (vectors.size() == 0) {
                        //no valid rate in this cluster
                        individual.append("0.000");
                        continue;
                    }
                    validClusterCount++;
                    for (int k = 0; k < center.length; k++) {
                        center[k] /= vectors.size();
                    }
                    double clusterSimilarity = 0.0;
                    for (int k = 0; k < vectors.size(); k++) {
                        clusterSimilarity += calCosVal(center, vectors.get(k));
                    }
                    clusterSimilarity /= vectors.size();
                    individual.append(String.format("%.3f", clusterSimilarity));
                    tableSimilarity += clusterSimilarity;
                }
                individual.append('\n');
            }
            tableSimilarity /= validClusterCount;
            summary.append(String.format("Table similarity is %.3f%n%n", tableSimilarity));
            bw.write(summary.toString(), 0, summary.length());
            bw.write(individual.toString(), 0, individual.length());
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void printGroupInfo(Table tb, int groupId, String outfile) {
        if (groupId < 0 || groupId >= numCluster * numCluster) {
            System.err.println("Invalid group id.");
            return;
        }
        int row = groupId / numCluster, col = groupId % numCluster;
        int rowCount = tb.getRowCount(), colCount = tb.getColCount();
        int rowClusterSize = rowCount / numCluster, rowRedundant = rowCount % numCluster;
        int colClusterSize = colCount / numCluster, colRedundant = colCount % numCluster;
        
        int rowStart = row * (rowClusterSize + 1) - (row > rowRedundant ? row - rowRedundant : 0);
        int rowEnd = rowStart + (row >= rowRedundant ? rowClusterSize - 1 : rowClusterSize);
        int colStart = col * (colClusterSize + 1) - (col > colRedundant ? col - colRedundant : 0);
        int colEnd = colStart + (col >= colRedundant ? colClusterSize - 1 : colClusterSize);
        //System.out.format("rowClusterSize: %d, rowRedundant: %d%n", rowClusterSize, rowRedundant);
        //System.out.format("colClusterSize: %d, colRedundant: %d%n", colClusterSize, colRedundant);
        //System.out.format("rowStart: %d, rowEnd: %d%n", rowStart, rowEnd);
        //System.out.format("colStart: %d, colEnd: %d%n", colStart, colEnd);
        Map<Double, Integer> map = new HashMap<Double, Integer>();
        double rateCount = 0.0, rateSum = 0.0;
        for (int i = rowStart; i <= rowEnd; i++) {
            for (int j = colStart; j <= colEnd; j++) {
                double rate = tb.getSingleIndexRate(i, j);
                if (rate == 0) continue;
                rateCount++;
                rateSum += rate;
                if (map.containsKey(rate)) {
                    map.put(rate, map.get(rate) + 1);
                }
                else {
                    map.put(rate, 1);
                }
            }
        }
        if (rateCount == 0) {
            System.out.println("No rate in this group.");
            return;
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
            StringBuilder sb = new StringBuilder();
            double ave = rateSum / rateCount, var = 0.0;
            PriorityQueue<Double> pq = new PriorityQueue<Double>(map.keySet());
            while (!pq.isEmpty()) {
                Double r = pq.poll();
                int c = map.get(r);
                for (int i = 0; i < c; i++) {
                    var += (r - ave) * (r - ave);
                }
                sb.append(String.format("rate: %.1f, count: %d%n", r, c));
                //System.out.format("rate: %.1f, count: %d%n", r, c);
            }
            double sd = Math.sqrt(var / rateCount);
            sb.append(String.format("number of rates: %.0f, average: %.2f, standard deviation: %.2f%n", rateCount, ave, sd));
            //System.out.format("number of rates: %.0f, average: %.2f, standard deviation: %.2f%n", rateCount, ave, sd);
            bw.write(sb.toString(), 0, sb.length());
            bw.newLine();
            sb = new StringBuilder();
            sb.append("        ");
            for (int i = colStart; i <= colEnd; i++) {
                sb.append(String.format("%-7d", tb.getIndexMovie(i)));
            }
            sb.append('\n');
            for (int i = rowStart; i <= rowEnd; i++) {
                sb.append(String.format("%-8d", tb.getIndexUser(i)));
                for (int j = colStart; j <= colEnd; j++) {
                    double rate = tb.getSingleIndexRate(i, j);
                    if (j > colStart) {
                        sb.append(',');
                    }
                    if (rate != 0) {
                        sb.append(String.format("%-6.1f", rate));
                    }
                    else {
                        sb.append("      ");
                    }
                }
                sb.append('\n');
            }
            bw.write(sb.toString(), 0, sb.length());
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void clusterTable(Table tb, boolean isRow) {
        System.out.format("\nStart clustering %s%n", isRow ? "Users" : "Movies");
        int totalCount = isRow ? tb.getRowCount() : tb.getColCount();
        int clusterSize = totalCount / numCluster;
        Set<Integer> elementSet = isRow ? tb.getUserSet() : tb.getMovieSet();
        System.out.format("cluster size: %d - %d.%n", clusterSize, clusterSize + 1);
        int section = 0, clusteredCount = 0;
        while (!elementSet.isEmpty()) {
            int src = -1;
            for (Integer i : elementSet) {
                src = i;
                break;
            }
            elementSet.remove(src);
            int sectionSize = section < totalCount % numCluster ? clusterSize + 1 : clusterSize;
            double[] srcVector = isRow ? tb.getUserIdRate(src) : tb.getMovieIdRate(src);
            PriorityQueue<Tuple> pq = new PriorityQueue<Tuple>(20, new Comparator<Tuple>(){
                public int compare(Tuple t1, Tuple t2) {
                    return t1.cos < t2.cos ? -1 : 1;
                }
            });
            for (int tar : elementSet) {
                double[] tarVector = isRow ? tb.getUserIdRate(tar) : tb.getMovieIdRate(tar);
                double cos = calCosVal(srcVector, tarVector);
                if (pq.size() < sectionSize - 1) {
                    pq.offer(new Tuple(tar, cos));
                }
                else if (!pq.isEmpty() && cos > pq.peek().cos) {
                    pq.offer(new Tuple(tar, cos));
                    pq.poll();
                }
            }
            if (isRow) {
                tb.swapUser(src, tb.getIndexUser(clusteredCount));
            }
            else {
                tb.swapMovie(src, tb.getIndexMovie(clusteredCount));
            }
            for (int i = 1; i < sectionSize && !pq.isEmpty(); i++) {
                int tar = pq.poll().id;
                if (isRow) {
                    tb.swapUser(tar, tb.getIndexUser(clusteredCount + i));
                }
                else {
                    tb.swapMovie(tar, tb.getIndexMovie(clusteredCount + i));
                }
                elementSet.remove(tar);
            }
            section++;
            clusteredCount += sectionSize;
        }
        System.out.println("done");
    }
    
    public static double calCosVal(double[] v1, double[] v2) {
        if (v1 == null || v2 == null) {
            System.err.println("Null vector.");
            return Double.MIN_VALUE;
        }
        if (v1.length != v2.length) {
            System.err.println("Vector dimension unmatched.");
            return Double.MIN_VALUE;
        }
        double inner = 0.0, uv1 = 0.0, uv2 = 0.0;
        for (int i = 0; i < v1.length; i++) {
            uv1 += v1[i] * v1[i];
            uv2 += v2[i] * v2[i];
            inner += v1[i] * v2[i];
        }
        if (uv1 == 0 || uv2 == 0) return Double.MIN_VALUE;
        return inner / (Math.sqrt(uv1) * Math.sqrt(uv2));
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
