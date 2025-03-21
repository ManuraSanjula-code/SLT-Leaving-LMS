package com.slt.peotv.userservice.lms.repository;

import com.slt.peotv.userservice.lms.entity.TempUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

@Repository
public interface TempUserRepo extends CrudRepository<TempUser, Long> {
    TempUser findTempUserByUserId(String userId);
    TempUser findByEmail(String email);

    @Query(nativeQuery = true, value = """
    SELECT 
        id, 
        user_id, 
        first_name,
        last_name, 
        email, 
        peo_tv_id, 
        expire_time, 
        is_new, 
        password_en,
        password  -- Required for entity mapping
    FROM temp_user 
    WHERE email = :email 
    AND (
        (password_en = TRUE 
         AND password = CONCAT(
             SUBSTRING_INDEX(password, ':', 1), 
             ':', 
             SHA2(CONCAT(:password, SUBSTRING_INDEX(password, ':', 1)), 512))
        ) 
        OR 
        (password_en = FALSE 
         AND password = :password)
    )""")
    Optional<TempUser> findValidUser(@Param("email") String email,
                                     @Param("password") String password);
}
