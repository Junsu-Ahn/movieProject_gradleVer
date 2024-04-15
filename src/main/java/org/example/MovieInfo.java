package org.example;

import java.util.HashMap;
import java.util.Map;

class MovieInfo implements Comparable<MovieInfo> {
    public String title;
    public Map<Integer, Integer> ratings;
    public String[] remainingSeats;

    public MovieInfo(String title) {
        this.title = title;
        this.ratings = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratings.put(i, 0);
        }
        this.remainingSeats = new String[10];
        for (int i = 0; i < remainingSeats.length; i++) {
            remainingSeats[i] = Integer.toString(i + 1);
        }
    }

    public void addRating(int rating) {
        ratings.put(rating, ratings.get(rating) + 1);
    }

    public void minusRating(int rating) {
        ratings.put(rating, ratings.get(rating) - 1);
    }

    public double getRating() {
        double total = 0;
        double sum = 0;
        for (Map.Entry<Integer, Integer> entry : ratings.entrySet()) {
            total += entry.getKey() * entry.getValue();
            sum += entry.getValue();
        }
        return sum == 0 ? 0 : total / sum;
    }

    public String getTitle() {
        return title;
    }

    public String[] getRemainingSeats() {
        return remainingSeats;
    }

    @Override
    public int compareTo(MovieInfo other) {
        return Double.compare(other.getRating(), this.getRating());
    }
}
