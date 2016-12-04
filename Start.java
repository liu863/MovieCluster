import java.util.*;
import java.io.*;

public class Start {
    public static void main(String[] args) {
        String csvFile = "tests/ml-latest/ratings.csv";
        Set<Integer> userSet = new HashSet<Integer>();
        Map<Double, Integer> rateCount = new HashMap<Double, Integer>();
        int id = 0, total = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                String[] val = line.split(",");
                id = Integer.parseInt(val[0]);
                userSet.add(id);
                double rate = Double.parseDouble(val[2]);
                if (rateCount.containsKey(rate)) {
                    rateCount.put(rate, rateCount.get(rate) + 1);
                }
                else {
                    rateCount.put(rate, 1);
                }
                total++;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("id: " + id + ", size: " + userSet.size());
        System.out.format("user: %d, vote: %d%n", userSet.size(), total);
        PriorityQueue<Double> pq = new PriorityQueue<Double>(rateCount.keySet());
        while (!pq.isEmpty()) {
            Double d = pq.poll();
            int i = rateCount.get(d);
            double p = (double)i / total * 100;
            System.out.format("rate: %.1f, count: %d, percentage: %.2f%%%n", d, i, p);
        }
    }
}
