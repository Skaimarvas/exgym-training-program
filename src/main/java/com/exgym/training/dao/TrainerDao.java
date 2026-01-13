package com.exgym.training.dao;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.exgym.training.entity.Trainer;
import com.exgym.training.storage.Storage;

@Repository
public class TrainerDao implements Dao<Trainer> {

    private static final Logger logger = LoggerFactory.getLogger(TrainerDao.class);
    
    private Storage storage;

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
        logger.debug("Storage injected into TrainerDao");
    }

    @Override
    public Optional<Trainer> get(long id) {
        logger.debug("Getting trainer with id: {}", id);
        return Optional.ofNullable(storage.getTrainers().get(id));
    }

    @Override
    public Map<Long, Trainer> getAll() {
        logger.debug("Getting all trainers");
        return Collections.unmodifiableMap(storage.getTrainers());
    }

    @Override
    public void save(Trainer t) {
        logger.info("Saving trainer: {}", t.getUserName());
        storage.getTrainers().put(t.getId(), t);
    }

    @Override
    public void update(Trainer t) {
        logger.info("Updating trainer: {}", t.getUserName());
        storage.getTrainers().put(t.getId(), t);
    }

    @Override
    public void delete(Trainer t) {
        logger.info("Deleting trainer: {}", t.getUserName());
        storage.getTrainers().remove(t.getId());
    }
}