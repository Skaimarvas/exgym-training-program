package com.exgym.training.util;

import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.Training;
import com.exgym.training.entity.TrainingTypeEntity;
import com.exgym.training.entity.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestDataLoader {
    private static final String DATA_FILE = "/data/init-data.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static List<Trainee> loadTrainees() {
        List<Trainee> trainees = new ArrayList<>();
        for (String[] row : loadRows("TRAINEE")) {
            User user = User.builder()
                    .firstName(row[2])
                    .lastName(row[3])
                    .userName(row[4])
                    .password(row[5])
                    .isActive(Boolean.parseBoolean(row[6]))
                    .build();
            Trainee trainee = Trainee.builder()
                    .id(Long.parseLong(row[1]))
                    .user(user)
                    .address(row[7])
                    .dateOfBirth(parseDate(row[8]))
                    .build();
            trainees.add(trainee);
        }
        return trainees;
    }

    public static List<Trainer> loadTrainers() {
        List<Trainer> trainers = new ArrayList<>();
        for (String[] row : loadRows("TRAINER")) {
            User user = User.builder()
                    .firstName(row[2])
                    .lastName(row[3])
                    .userName(row[4])
                    .password(row[5])
                    .isActive(Boolean.parseBoolean(row[6]))
                    .build();
            Trainer trainer = Trainer.builder()
                    .id(Long.parseLong(row[1]))
                    .user(user)
                    .specialization(row[7])
                    .build();
            trainers.add(trainer);
        }
        return trainers;
    }

    public static List<Training> loadTrainings(List<Trainer> trainers, List<Trainee> trainees) {
        List<Training> trainings = new ArrayList<>();
        Map<Long, Trainer> trainerMap = new HashMap<>();
        Map<Long, Trainee> traineeMap = new HashMap<>();
        for (Trainer t : trainers) trainerMap.put(t.getId(), t);
        for (Trainee t : trainees) traineeMap.put(t.getId(), t);
        for (String[] row : loadRows("TRAINING")) {
            Training training = new Training();
            training.setId(Long.parseLong(row[1]));
            training.setTrainer(trainerMap.get(Long.parseLong(row[2])));
            training.setTrainee(traineeMap.get(Long.parseLong(row[3])));
            training.setTrainingName(row[4]);
            TrainingTypeEntity trainingType = new TrainingTypeEntity(null, row[5]);
            training.setTrainingType(trainingType);
            training.setTrainingDate(parseDate(row[6]));
            training.setTrainingDuration(Integer.parseInt(row[7]));
            trainings.add(training);
        }
        return trainings;
    }

    private static List<String[]> loadRows(String prefix) {
        List<String[]> rows = new ArrayList<>();
        try (InputStream is = TestDataLoader.class.getResourceAsStream(DATA_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(prefix)) {
                    rows.add(line.split(","));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test data", e);
        }
        return rows;
    }

    private static Date parseDate(String s) {
        try {
            return DATE_FORMAT.parse(s);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date: " + s, e);
        }
    }
}
