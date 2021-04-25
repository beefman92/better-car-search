package com.my.car.crawler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Utils {
    private final static Set<String> vinSet = new HashSet<>();
    private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final static String vinSetPath = "data/vin-set.json";
    private final static Random random = new Random();

    public static void initVinSet() throws IOException {
        try (FileReader reader = new FileReader(vinSetPath)) {
            List<String> vinList = (List<String>)gson.fromJson(reader, List.class);
            vinSet.addAll(vinList);
        }
    }

    public static void saveVinSet() throws IOException {
        try (FileWriter writer = new FileWriter(vinSetPath)) {
            gson.toJson(vinSet, writer);
        }
    }

    public static synchronized boolean addVin(String vin) {
        return vinSet.add(vin);
    }

    public static long getRandomSleepOffset(int min, int max) {
        return random.nextInt(max - min) + min;
    }
}
