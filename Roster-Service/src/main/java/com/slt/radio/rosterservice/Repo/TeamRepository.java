package com.slt.radio.rosterservice.Repo;

import com.slt.radio.rosterservice.Model.One.Teamm.Team;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends MongoRepository<Team, String> {
    Optional<Team> findByName(String name);
    Optional<Team> findByShortName(String shortName);
}

