package com.slt.peotv.userservice.lms.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.slt.peotv.userservice.lms.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	UserEntity findByEmail(String email);
	UserEntity findByUserId(String userId);
	UserEntity findUserByEmailVerificationToken(String token);

    @Override
	Page<UserEntity> findAll(Pageable pageable);

	@Query(value="select * from users u where u.email_verification_status = true",
			countQuery="select count(*) from users u where u.email_verification_status = true",
			nativeQuery = true)
	Page<UserEntity> findAllUsersWithConfirmedEmailAddress( Pageable pageableRequest );

	@Query(value="select * from users u where u.first_name = ?1",nativeQuery=true)
	List<UserEntity> findUserByFirstName(String firstName);

	@Query(value="select * from users u where u.last_name = :lastName",nativeQuery=true)
	List<UserEntity> findUserByLastName(@Param("lastName") String lastName);

	@Query(value="select * from users u where first_name LIKE %:keyword% or last_name LIKE %:keyword%",nativeQuery=true)
	List<UserEntity> findUsersByKeyword(@Param("keyword") String keyword);

	@Query(value="select u.first_name, u.last_name from users u where u.first_name LIKE %:keyword% or u.last_name LIKE %:keyword%",nativeQuery=true)
	List<Object[]> findUserFirstNameAndLastNameByKeyword(@Param("keyword") String keyword);

	@Transactional
	@Modifying
	@Query(value="update users u set u.email_verification_status=:emailVerificationStatus where u.user_id=:userId", nativeQuery=true)
	void updateUserEmailVerificationStatus(@Param("emailVerificationStatus") boolean emailVerificationStatus,
			@Param("userId") String userId);

	@Query("select user from UserEntity user where user.userId =:userId")
	UserEntity findUserEntityByUserId(@Param("userId") String userId);

	@Query("select user.firstName, user.lastName from UserEntity user where user.userId =:userId")
	List<Object[]> getUserEntityFullNameById(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u set u.emailVerificationStatus =:emailVerificationStatus where u.userId = :userId")
    void updateUserEntityEmailVerificationStatus(
    		@Param("emailVerificationStatus") boolean emailVerificationStatus,
            @Param("userId") String userId);
    
    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.name = :roleName")
    List<UserEntity> findByRoleName(@Param("roleName") String roleName);

    // Custom query to find users by multiple roles
    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.name IN :roleNames")
    List<UserEntity> findByRoleNames(@Param("roleNames") List<String> roleNames);
    
    @Query(value = "SELECT u.* FROM users u " +
            "JOIN users_roles ur ON u.id = ur.users_id " +
            "JOIN roles r ON ur.roles_id = r.id " +
            "WHERE r.name IN ('ROLE_CHAIRMAN', 'ROLE_CEO') " +
            "GROUP BY u.id " +
            "HAVING COUNT(DISTINCT r.name) = 2 " +
            "AND COUNT(DISTINCT r.name) = (SELECT COUNT(*) FROM users_roles ur2 WHERE ur2.users_id = u.id)",
    nativeQuery = true)
    List<UserEntity> findUsersWithOnlyChairmanAndCeoRolesNative();

}
