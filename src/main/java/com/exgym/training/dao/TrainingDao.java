package com.exgym.training.dao;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.exgym.training.entity.Training;
import com.exgym.training.storage.Storage;

@Repository
public class TrainingDao implements Dao<Training> {

    private static final Logger logger = LoggerFactory.getLogger(TrainingDao.class);
    
    private Storage storage;

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
        logger.debug("Storage injected into TrainingDao");
    }

    @Override
    public Optional<Training> get(long id) {
        logger.debug("Getting training with id: {}", id);
        return Optional.ofNullable(storage.getTrainings().get(id));
    }

    @Override
    public Map<Long, Training> getAll() {
        logger.debug("Getting all trainings");
        return Collections.unmodifiableMap(storage.getTrainings());
    }

    @Override
    public void save(Training t) {
        logger.info("Saving training: {}", t.getTrainingName());
        storage.getTrainings().put(t.getId(), t);
    }

    @Override
    public void update(Training t) {
        logger.info("Updating training: {}", t.getTrainingName());
        storage.getTrainings().put(t.getId(), t);
    }

    @Override
    public void delete(Training t) {
        logger.info("Deleting training: {}", t.getTrainingName());
        storage.getTrainings().remove(t.getId());
    }
}
