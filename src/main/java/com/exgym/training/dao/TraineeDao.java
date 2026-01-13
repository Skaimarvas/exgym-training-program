package com.exgym.training.dao;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.exgym.training.entity.Trainee;
import com.exgym.training.storage.Storage;

@Repository
public class TraineeDao implements Dao<Trainee> {

    private static final Logger logger = LoggerFactory.getLogger(TraineeDao.class);
    
    private Storage storage;

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
        logger.debug("Storage injected into TraineeDao");
    }

    @Override
    public Optional<Trainee> get(long id) {
        logger.debug("Getting trainee with id: {}", id);
        return Optional.ofNullable(storage.getTrainees().get(id));
    }

    @Override
    public Map<Long, Trainee> getAll() {
        logger.debug("Getting all trainees");
        return Collections.unmodifiableMap(storage.getTrainees());
    }

    @Override
    public void save(Trainee t) {
        logger.info("Saving trainee: {}", t.getUserName());
        storage.getTrainees().put(t.getId(), t);
    }

    @Override
    public void update(Trainee t) {
        logger.info("Updating trainee: {}", t.getUserName());
        storage.getTrainees().put(t.getId(), t);
    }

    @Override
    public void delete(Trainee t) {
        logger.info("Deleting trainee: {}", t.getUserName());
        storage.getTrainees().remove(t.getId());
    }
}
