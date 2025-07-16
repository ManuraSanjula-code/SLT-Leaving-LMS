package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRepo extends JpaRepository<LeaveEntity, Long> {
    Page<LeaveEntity> findAll(Pageable pageable);
    Page<LeaveEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    List<LeaveEntity> findByEmployee(EmployeeEntity employee);
    Optional<LeaveEntity> findByEmployeeAndSubmitDate(EmployeeEntity employeeID, Date submitDate);
    Optional<LeaveEntity> findByEmployeeAndHappenDate(EmployeeEntity employee, Date happenDate);
    Optional<LeaveEntity> findByEmployeeAndFromDate(EmployeeEntity employee, Date fromDate);
    Optional<LeaveEntity> findByPublicId(String publicId);
    List<LeaveEntity> findByFromDate(Date fromDate);
    /* List<LeaveEntity> findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual
        (EmployeeEntity employee, Date currentDate1, Date currentDate2); */
    List<LeaveEntity> findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            EmployeeEntity employee,
            Date arrivalDate,
            Date currentDate
    );    
    List<LeaveEntity> findByEmployeeAndIsManualRequest(EmployeeEntity employee, Boolean isManualRequest);
    List<LeaveEntity> findApprovedLeavesByEmployeeAndFromDateAndToDate(EmployeeEntity employee, Date fromDate, Date toDate);

    List<LeaveEntity> findByEmployeeAndSubmitDateBetween(
            EmployeeEntity employee,
            Date startDate,
            Date endDate
    );

}
