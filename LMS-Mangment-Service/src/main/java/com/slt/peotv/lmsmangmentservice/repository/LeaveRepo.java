package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRepo extends JpaRepository<LeaveEntity, Long> {
    List<LeaveEntity> findByEmployeeID(String employeeID);
    Optional<LeaveEntity> findByPublicId(String publicId);
    List<LeaveEntity> findByEmployeeIDAndFromDateLessThanEqualAndToDateGreaterThanEqual(String employeeID, Date currentDate1, Date currentDate2);
    List<LeaveEntity> findByEmployeeIDAndIsManualRequest(String employeeID, Boolean isManualRequest);
    /*List<LeaveEntity> findByUserAndFromDateLessThanEqualAndToDateGreaterThanEqual(UserEntity user, Date currentDate);
    @Query("SELECT l FROM LeaveEntity l WHERE l.user.id = :userId AND l.fromDate <= :date AND l.toDate >= :date AND (l.isSupervisedApproved = true AND l.isHODApproved = true)")
    List<LeaveEntity> findActiveLeaveByUserAndDate(@Param("userId") Long userId, @Param("date") Date date);

    @Query("SELECT e FROM LeaveEntity e WHERE e.dueDate < :currentDate")
    List<LeaveEntity> findOverdueEntities(@Param("currentDate") Date currentDate);*/

    List<LeaveEntity> findApprovedLeavesByEmployeeIDAndFromDateAndToDate(String employeeID, Date fromDate, Date toDate);

}
