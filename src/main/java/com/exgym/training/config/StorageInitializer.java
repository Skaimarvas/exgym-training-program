package com.exgym.training.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import java.time.LocalDate;
import com.exgym.training.entity.Training;
import com.exgym.training.enums.TrainingType;
import com.exgym.training.storage.Storage;

import jakarta.annotation.PostConstruct;

@Component
public class StorageInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(StorageInitializer.class);
    
    private final Storage storage;
    
    @Value("${storage.init.file.path}")
    private String filePath;
    
    public StorageInitializer(Storage storage) {
        this.storage = storage;
    }
    
    @PostConstruct
    public void init() {
        logger.info("Initializing storage from file: {}", filePath);
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            lines.filter(line -> !line.trim().isEmpty())
                 .forEach(this::parseLine);
            logger.info("Storage initialized successfully. Trainers: {}, Trainees: {}, Trainings: {}", 
                storage.getTrainers().size(), 
                storage.getTrainees().size(), 
                storage.getTrainings().size());
        } catch (IOException e) {
            logger.error("Failed to initialize storage from file: {}", filePath, e);
            throw new RuntimeException("Failed to initialize storage from file: " + filePath, e);
        }
    }
    
    private void parseLine(String line) {
        try {
            String[] parts = line.split(",");
            String type = parts[0].trim();

            switch (type) {
                case "TRAINER": {
                    Long id = Long.parseLong(parts[1].trim());
                    String firstName = parts[2].trim();
                    String lastName = parts[3].trim();
                    String userName = parts[4].trim();
                    String password = parts[5].trim();
                    Boolean isActive = Boolean.parseBoolean(parts[6].trim());
                    String address = parts[7].trim();
                    LocalDate dateOfBirth = LocalDate.parse(parts[8].trim());

                    com.exgym.training.entity.User user = com.exgym.training.entity.User.builder()
                        .id(id)
                        .firstName(firstName)
                        .lastName(lastName)
                        .userName(userName)
                        .password(password)
                        .build();

                    Trainer trainer = Trainer.builder()
                        .id(id)
                        .user(user)
                        .isActive(isActive)
                        .build();
                    // Set additional fields if needed (address, dateOfBirth)
                    trainer.setSpecialization(""); // or parse if available
                    storage.getTrainers().put(trainer.getId(), trainer);
                    logger.debug("Loaded trainer: {}", trainer.getUser().getUserName());
                    break;
                }
                case "TRAINEE": {
                    Long id = Long.parseLong(parts[1].trim());
                    String firstName = parts[2].trim();
                    String lastName = parts[3].trim();
                    String userName = parts[4].trim();
                    String password = parts[5].trim();
                    Boolean isActive = Boolean.parseBoolean(parts[6].trim());
                    String specialization = parts[7].trim();

                    com.exgym.training.entity.User user = com.exgym.training.entity.User.builder()
                        .id(id)
                        .firstName(firstName)
                        .lastName(lastName)
                        .userName(userName)
                        .password(password)
                        .build();

                    Trainee trainee = Trainee.builder()
                        .id(id)
                        .user(user)
                        .isActive(isActive)
                        .build();
                    // Set additional fields if needed (specialization)
                    storage.getTrainees().put(trainee.getId(), trainee);
                    logger.debug("Loaded trainee: {}", trainee.getUser().getUserName());
                    break;
                }
                case "TRAINING": {
                    Long id = Long.parseLong(parts[1].trim());
                    Long trainerId = Long.parseLong(parts[2].trim());
                    Long traineeId = Long.parseLong(parts[3].trim());
                    String trainingName = parts[4].trim();
                    TrainingType trainingType = TrainingType.valueOf(parts[5].trim());
                    java.util.Date trainingDate = java.sql.Date.valueOf(parts[6].trim());
                    int trainingDuration = Integer.parseInt(parts[7].trim());

                    Trainer trainer = storage.getTrainers().get(trainerId);
                    Trainee trainee = storage.getTrainees().get(traineeId);

                    Training training = new Training(
                        id,
                        trainer,
                        trainee,
                        trainingName,
                        trainingType,
                        trainingDate,
                        trainingDuration
                    );
                    storage.getTrainings().put(training.getId(), training);
                    logger.debug("Loaded training: {}", training.getTrainingName());
                    break;
                }
                default:
                    logger.warn("Unknown entity type: {}", type);
            }
        } catch (Exception e) {
            logger.error("Failed to parse line: {}", line, e);
        }
    }
}
