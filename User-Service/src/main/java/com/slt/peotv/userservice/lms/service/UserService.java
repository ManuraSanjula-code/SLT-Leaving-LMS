package com.slt.peotv.userservice.lms.service;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import com.slt.peotv.userservice.lms.shared.dto.*;
import com.slt.peotv.userservice.lms.shared.model.request.*;
import com.slt.peotv.userservice.lms.shared.model.response.UserRest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import com.slt.peotv.userservice.lms.entity.UserEntity;
import com.slt.peotv.userservice.lms.entity.company.ProfilesEntity;
import com.slt.peotv.userservice.lms.entity.company.SectionEntity;

public interface UserService extends UserDetailsService{
	UserDto createUser(UserReq user) throws Exception;
	UserDto updateUserProfile(MultipartFile file, String userid) throws Exception;
	UserDto getUser(String email);
	UserDto getUserByUserId(String userId);
	UserRest getUserByUserId_(String userId);
	UserDto updateUser(String userId, UserReq user) throws Exception;
	void deleteUser(String userId);
	List<UserDto> getUsers(int page, int limit);
	boolean verifyEmailToken(String token);
	boolean requestPasswordReset(String email);
	boolean resetPassword(String token, String password);
	Resource loadImageAsResource(String imageName) throws MalformedURLException;
	UserEntity getUserByE(String email);
	Resource getImage(String userId) throws MalformedURLException;
	void resetPassWord(UserPasswordReset userPasswordReset);
	boolean userAddress(String userId);

	List<UserDtoArchive> findByRoleName(String roleName);
	List<RoleDTO> getRole();
	List<RoleDTOArchive> getRoleArchive();
	List<SectionDTO> getSection();
	List<ProfilesDTO> getProfile();
	List<Map<String, String>> getAuthority();

	RoleDTO saveRole(RoleReq req);
	RoleDTOArchive saveRole_(RoleReq req);
	AuthorityDTO saveAuthority(AuthReq authority);
	SectionDTO saveSection(SectionReq req);
	ProfilesDTO saveProfile(ProfileReq req);

	List<String> getAllRoleNames();
	List<String> getAllSectionNames();
	List<String> getAllProfileNames();
	Page<UserEntity> getAllUsers(int page, int size);
	boolean checkAuth(String name);
	void deleteAuth(Long authId);
	void deleteSection(Long sectionId);
	void deleteRole(Long roleId);
	void deleteProfile(Long roleId);
	UserDetails getUserDetailsByUserId(String userId);
	public void rebalancePriorities();
}
