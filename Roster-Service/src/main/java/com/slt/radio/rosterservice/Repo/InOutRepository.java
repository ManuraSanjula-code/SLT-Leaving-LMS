package com.slt.radio.rosterservice.Repo;


import com.slt.radio.rosterservice.Model.One.LMS.InOut;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface InOutRepository extends MongoRepository<InOut, String> {
    List<InOut> findByEmployeeIDAndDate(String employeeID, Date date);
    Optional<InOut> findTopByEmployeeIDAndDateOrderByPunchInMoaAsc(String employeeID, Date date);
}

