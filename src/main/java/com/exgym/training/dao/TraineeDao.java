package com.exgym.training.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.exgym.training.entity.Trainee;

@Repository
public class TraineeDao implements Dao<Trainee> {

    private Map<Long, Trainee> trainees = new HashMap<>();

    @Override
    public Optional<Trainee> get(long id) {

        return trainees.values().stream()
                .filter(trainee -> trainee.getId() == id)
                .findFirst();
    }

    @Override
    public Map<Long, Trainee> getAll() {

        return Collections.unmodifiableMap(trainees);
    }

    @Override
    public void save(Trainee t) {
        trainees.put(t.getId(), t);

    }

    @Override
    public void update(Trainee t) {
        trainees.put(t.getId(), t);

    }

    @Override
    public void delete(Trainee t) {
        trainees.remove(t.getId());

    }

}
