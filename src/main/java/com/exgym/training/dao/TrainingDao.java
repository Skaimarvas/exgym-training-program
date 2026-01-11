package com.exgym.training.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.exgym.training.entity.Training;

@Repository
public class TrainingDao implements Dao<Training> {

    private Map<Long, Training> trainings = new HashMap<>();

    @Override
    public Optional<Training> get(long id) {

        return trainings.values().stream()
                .filter(training -> training.getId() == id)
                .findFirst();
    }

    @Override
    public Map<Long, Training> getAll() {

        return Collections.unmodifiableMap(trainings);
    }

    @Override
    public void save(Training t) {
        trainings.put(t.getId(), t);

    }

    @Override
    public void update(Training t) {
        trainings.put(t.getId(), t);

    }

    @Override
    public void delete(Training t) {
        trainings.remove(t.getId());

    }

}
