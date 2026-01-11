package com.exgym.training.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.exgym.training.entity.Trainer;

@Repository
public class TrainerDao implements Dao<Trainer> {

    private Map<Long, Trainer> trainers = new HashMap<>();

    @Override
    public Optional<Trainer> get(long id) {

        return trainers.values().stream()
                .filter(trainer -> trainer.getId() == id)
                .findFirst();
    }

    @Override
    public Map<Long, Trainer> getAll() {

        return Collections.unmodifiableMap(trainers);
    }

    @Override
    public void save(Trainer t) {
        trainers.put(t.getId(), t);

    }

    @Override
    public void update(Trainer t) {
        trainers.put(t.getId(), t);

    }

    @Override
    public void delete(Trainer t) {
        trainers.remove(t.getId());

    }

}