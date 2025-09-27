package com.slt.peotv.userservice.lms.repository;

import com.slt.peotv.userservice.lms.entity.TempUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TempUserRepo extends CrudRepository<TempUser, Long> {
    TempUser findTempUserByUserId(String userId);
    TempUser findByEmail(String email);

    @Query(nativeQuery = true, value =
            "SELECT " +
                    "    * " +
                    "FROM temp_user " +
                    "WHERE email = :email " +
                    "AND ( " +
                    "    (admin = TRUE " +
                    "     AND password = CONCAT( " +
                    "         SUBSTRING_INDEX(password, ':', 1), " +
                    "         ':', " +
                    "         SHA2(CONCAT(:password, SUBSTRING_INDEX(password, ':', 1)), 512)) " +
                    "    ) " +
                    "    OR " +
                    "    (admin = FALSE " +
                    "     AND password = :password) " +
                    ")")
    Optional<TempUser> findValidUser(@Param("email") String email,
                                     @Param("password") String password);

    List<TempUser> findByExpireTimeBefore(Date currentTime);

    @Modifying
    @Transactional
    @Query("DELETE FROM TempUser t WHERE t.expireTime < ?1")
    int deleteExpiredUsers(Date currentTime);

    List<TempUser> findByExpireTimeAfter(Date currentTime);
}