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
                case "TRAINER":
                    Trainer trainer = Trainer.builder()
                        .id(Long.parseLong(parts[1].trim()))
                        .firstName(parts[2].trim())
                        .lastName(parts[3].trim())
                        .userName(parts[4].trim())
                        .password(parts[5].trim())
                        .isActive(Boolean.parseBoolean(parts[6].trim()))
                        .address(parts[7].trim())
                        .dateOfBirth(LocalDate.parse(parts[8].trim()))
                        .build();
                    storage.getTrainers().put(trainer.getId(), trainer);
                    logger.debug("Loaded trainer: {}", trainer.getUserName());
                    break;
                    
                case "TRAINEE":
                    Trainee trainee = Trainee.builder()
                        .id(Long.parseLong(parts[1].trim()))
                        .firstName(parts[2].trim())
                        .lastName(parts[3].trim())
                        .userName(parts[4].trim())
                        .password(parts[5].trim())
                        .isActive(Boolean.parseBoolean(parts[6].trim()))
                        .specialization(parts[7].trim())
                        .build();
                    storage.getTrainees().put(trainee.getId(), trainee);
                    logger.debug("Loaded trainee: {}", trainee.getUserName());
                    break;
                    
                case "TRAINING":
                    Training training = new Training(
                        Long.parseLong(parts[1].trim()),
                        Long.parseLong(parts[2].trim()),
                        Long.parseLong(parts[3].trim()),
                        parts[4].trim(),
                        TrainingType.valueOf(parts[5].trim()),
                        parts[6].trim(),
                        Integer.parseInt(parts[7].trim())
                    );
                    storage.getTrainings().put(training.getId(), training);
                    logger.debug("Loaded training: {}", training.getTrainingName());
                    break;
                    
                default:
                    logger.warn("Unknown entity type: {}", type);
            }
        } catch (Exception e) {
            logger.error("Failed to parse line: {}", line, e);
        }
    }
}
