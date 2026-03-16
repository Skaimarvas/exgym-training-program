package com.exgym.training.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.exgym.training.dao.TrainingTypeDao;
import com.exgym.training.entity.TrainingTypeEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainingTypeDataInitializer implements ApplicationRunner {

    private static final List<String> DEFAULT_TYPES = List.of("YOGA", "STRENGTH", "CARDIO");

    private final TrainingTypeDao trainingTypeDao;

    @Override
    public void run(ApplicationArguments args) {
        if (trainingTypeDao.count() > 0) {
            return;
        }

        log.info("Seeding default training types: {}", DEFAULT_TYPES);
        List<TrainingTypeEntity> defaultEntities = DEFAULT_TYPES.stream()
                .map(type -> new TrainingTypeEntity(null, type))
                .toList();
        trainingTypeDao.saveAll(defaultEntities);
    }
}
