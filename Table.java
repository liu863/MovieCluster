import java.util.*;
import java.lang.*;
import java.io.*;

public class Table {
    
    private int rowCount, colCount;
    private Map<Integer, String> movieName;
    private Map<Integer, Integer> movieIndex, indexMovie;
    private Map<Integer, Integer> userIndex, indexUser;
    private List<double[]> ratings;
    
    private Table(int row, int col) {
        rowCount = row;
        colCount = col;
    }
    
    public Table(Map<Integer, String> moviename, Map<Integer, Integer> movieindex) {
        rowCount = 0;
        colCount = moviename.size();
        movieName = new HashMap<Integer, String>(moviename);
        movieIndex = new HashMap<Integer, Integer>(movieindex);
        indexMovie = new HashMap<Integer, Integer>();
        for (Integer i : movieIndex.keySet()) {
            indexMovie.put(movieIndex.get(i), i);
        }
        userIndex = new HashMap<Integer, Integer>();
        indexUser = new HashMap<Integer, Integer>();
        ratings = new ArrayList<double[]>();
    }
    
    public boolean hasUser(int userId) {
        return userIndex.containsKey(userId);
    }
    
    public boolean hasMovie(int movieId) {
        return movieIndex.containsKey(movieId);
    }
    
    public boolean addUser(int userId) {
        if (userIndex.containsKey(userId)) {
            System.err.println("User already exist.");
            return false;
        }
        userIndex.put(userId, rowCount);
        indexUser.put(rowCount, userId);
        ratings.add(rowCount, null);
        rowCount++;
        return true;
    }
    
    public boolean addRate(int userId, int movieId, double rate) {
        if (!userIndex.containsKey(userId)) {
            System.err.println("User does not exist.");
            return false;
        }
        if (!movieIndex.containsKey(movieId)) {
            System.err.println("Movie does not exit.");
            return false;
        }
        ratings.get(userIndex.get(userId))[movieIndex.get(movieId)] = rate;
        return true;
    }
    
    public boolean addAllRate(int userId, double[] rates) {
        if (!userIndex.containsKey(userId)) {
            System.err.println("User does not exist.");
            return false;
        }
        if (rates.length != colCount) {
            System.err.println("Incorrect number of rates");
            return false;
        }
        double[] ratesCopy = new double[colCount];
        System.arraycopy(rates, 0, ratesCopy, 0, colCount);
        int index = userIndex.get(userId);
        ratings.remove(index);
        ratings.add(index, ratesCopy);
        return true;
    }
    
    public double getSingleIdRate(int userId, int movieId) {
        if (!userIndex.containsKey(userId)) {
            System.err.println("User does not exist.");
            return -1.0;
        }
        if (!movieIndex.containsKey(movieId)) {
            System.err.println("Movie does not exit.");
            return -1.0;
        }
        return ratings.get(userIndex.get(userId))[movieIndex.get(movieId)];
    }
    
    public double getSingleIndexRate(int userIndex, int movieIndex) {
        if (userIndex < 0 || userIndex >= rowCount) {
            System.out.println("Invalid user index.");
            return -1.0;
        }
        if (movieIndex < 0 || movieIndex >= colCount) {
            System.out.println("Invalid movie index.");
            return -1.0;
        }
        return ratings.get(userIndex)[movieIndex];
    }
    
    public double[] getUserIdRate(int userId) {
        if (!userIndex.containsKey(userId)) {
            System.err.format("User does not exist. id: %d%n", userId);
            return null;
        }
        double[] ret = new double[colCount];
        System.arraycopy(ratings.get(userIndex.get(userId)), 0, ret, 0, colCount);
        return ret;
    }
    
    public double[] getUserIndexRate(int userIndex) {
        if (userIndex < 0 || userIndex >= rowCount) {
            System.err.format("Invalid user index. index: %d%n", userIndex);
            return null;
        }
        double[] ret = new double[colCount];
        System.arraycopy(ratings.get(userIndex), 0, ret, 0, colCount);
        return ret;
    }
    
    public double[] getMovieIdRate(int movieId) {
        Integer index;
        if ((index = movieIndex.get(movieId)) == null) {
            System.err.format("Movie does not exist. id: %d%n", movieId);
            return null;
        }
        double[] ret = new double[rowCount];
        for (int i = 0; i < rowCount; i++) {
            ret[i] = ratings.get(i)[index];
        }
        return ret;
    }
    
    public double[] getMovieIndexRate(int movieIndex) {
        if (movieIndex < 0 || movieIndex >= colCount) {
            System.err.format("Invalid movieindex. index: %d%n", movieIndex);
            return null;
        }
        double[] ret = new double[rowCount];
        for (int i = 0; i < rowCount; i++) {
            ret[i] = ratings.get(i)[movieIndex];
        }
        return ret;
    }
    
    public boolean swapUser(int userA, int userB) {
        if (!userIndex.containsKey(userA) || !userIndex.containsKey(userB)) {
            System.err.println("User does not exist.");
            return false;
        }
        int indexA = userIndex.get(userA);
        int indexB = userIndex.get(userB);
        double[] rateA = ratings.get(indexA);
        double[] rateB = ratings.get(indexB);
        for (int i = 0; i < colCount; i++) {
            double temp = rateA[i];
            rateA[i] = rateB[i];
            rateB[i] = temp;
        }
        userIndex.put(userA, indexB);
        userIndex.put(userB, indexA);
        indexUser.put(indexB, userA);
        indexUser.put(indexA, userB);
        return true;
    }
    
    public boolean swapMovie(int movieA, int movieB) {
        if (!movieIndex.containsKey(movieA) || !movieIndex.containsKey(movieB)) {
            System.err.println("Movie does not exist.");
            return false;
        }
        int indexA = movieIndex.get(movieA);
        int indexB = movieIndex.get(movieB);
        for (int i = 0; i < rowCount; i++) {
            double[] rate = ratings.get(i);
            double temp = rate[indexA];
            rate[indexA] = rate[indexB];
            rate[indexB] = temp;
        }
        movieIndex.put(movieA, indexB);
        movieIndex.put(movieB, indexA);
        indexMovie.put(indexB, movieA);
        indexMovie.put(indexA, movieB);
        return true;
    }
    
    public int getRowCount() {
        return rowCount;
    }
    
    public int getColCount() {
        return colCount;
    }
    
    public int getIndexUser(int userIndex) {
        return indexUser.get(userIndex);
    }
    
    public int getIndexMovie(int movieIndex) {
        return indexMovie.get(movieIndex);
    }
    
    public Map<Integer, Integer> getUserIndexMap() {
        return new HashMap<Integer, Integer>(userIndex);
    }
    
    public Map<Integer, Integer> getIndexUserMap() {
        return new HashMap<Integer, Integer>(indexUser);
    }
    
    public Map<Integer, Integer> getMovieIndexMap() {
        return new HashMap<Integer, Integer>(movieIndex);
    }
    
    public Map<Integer, Integer> getIndexMovieMap() {
        return new HashMap<Integer, Integer>(indexMovie);
    }
    
    public Set<Integer> getUserSet() {
        return new HashSet<Integer>(userIndex.keySet());
    }
    
    public Set<Integer> getMovieSet() {
        return new HashSet<Integer>(movieIndex.keySet());
    }
    
    public Table copy() {
        Table tb = new Table(rowCount, colCount);
        tb.setMovieName(movieName);
        tb.setMovieMapping(movieIndex, indexMovie);
        tb.setUserMapping(userIndex, indexUser);
        tb.setRatings(ratings);
        return tb;
    }
    
    private void setMovieName(Map<Integer, String> moviename) {
        movieName = new HashMap<Integer, String>(moviename);
    }
    
    private void setMovieMapping(Map<Integer, Integer> movieindex, Map<Integer, Integer> indexmovie) {
        movieIndex = new HashMap<Integer, Integer>(movieindex);
        indexMovie = new HashMap<Integer, Integer>(indexmovie);
    }
    
    private void setUserMapping(Map<Integer, Integer> userindex, Map<Integer, Integer> indexuser) {
        userIndex = new HashMap<Integer, Integer>(userindex);
        indexUser = new HashMap<Integer, Integer>(indexuser);
    }
    
    private void setRatings(List<double[]> srcRatings) {
        ratings = new ArrayList<double[]>();
        for (int i = 0; i < srcRatings.size(); i++) {
            double[] rates = new double[colCount];
            System.arraycopy(srcRatings.get(i), 0, rates, 0, colCount);
            ratings.add(rates);
        }
    }
    public void printTable() {
        printTable(null);
    }
    
    public void printTable(String outfile) {
        try {
            BufferedWriter bw = null;
            if (outfile != null) 
                bw = new BufferedWriter(new FileWriter(outfile));
            for (int i = 0; i < rowCount; i++) {
                double[] rates = ratings.get(i);
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < colCount; j++) {
                    if (j > 0) {
                        sb.append(',');
                    }
                    if (rates[j] != 0) {
                        sb.append(String.format("%.1f", rates[j]));
                    }
                    else {
                        sb.append("   ");
                    }
                }
                if (outfile != null) {
                    bw.write(sb.toString(), 0, sb.length());
                    bw.newLine();
                }
                else {
                    System.out.println(sb.toString());
                }
            }
            if (bw != null) bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}