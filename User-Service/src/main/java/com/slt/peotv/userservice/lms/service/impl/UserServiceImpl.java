package com.slt.peotv.userservice.lms.service.impl;

import com.slt.peotv.userservice.lms.entity.*;
import com.slt.peotv.userservice.lms.entity.company.ProfilesEntity;
import com.slt.peotv.userservice.lms.entity.company.SectionEntity;
import com.slt.peotv.userservice.lms.exceptions.UserServiceException;
import com.slt.peotv.userservice.lms.message.LMSUser;
import com.slt.peotv.userservice.lms.message.MessageProducerService;
import com.slt.peotv.userservice.lms.redis.RedisService;
import com.slt.peotv.userservice.lms.repository.*;
import com.slt.peotv.userservice.lms.security.UserPrincipal;
import com.slt.peotv.userservice.lms.service.UserService;
import com.slt.peotv.userservice.lms.utils.*;
import com.slt.peotv.userservice.lms.shared.dto.*;
import com.slt.peotv.userservice.lms.shared.model.request.*;
import com.slt.peotv.userservice.lms.shared.model.response.ErrorMessages;
import com.slt.peotv.userservice.lms.shared.model.response.UserRest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserServiceImpl implements UserService {

    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/uploads/";

    @Autowired
    private ImplUtils implUtils;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IdUtils idUtils;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ProfilesRepo profilesRepo;
    @Autowired
    private SectionRepo sectionRepo;
    @Autowired
    private AuthorityRepository authorityRepo;
    @Autowired
    private MessageProducerService messageProducerService;
    @Autowired
    private TempUserRepo tempUserRepo;
    @Autowired
    private RedisService redisService;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private UpdateUtils updateUtils;

    private <T> void updateFieldIfNotNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
    @Override
    public UserDto createUser(UserReq user) throws UserServiceException {
        try {
            validateEmailUniqueness(user.getEmail());

            UserEntity userEntity = createAndValidateUserEntity(user);

            enrichUserEntity(user, userEntity);

            setUserRelationships(user, userEntity);

            UserEntity storedUser = saveAndProcessUser(userEntity, user);

            notifySystemsAndCacheCredentials(storedUser, user.getPassword());

            return UserMapper.mapToUserDto(storedUser);

        } catch (DataIntegrityViolationException e) {
            throw new UserServiceException("The provided data conflicts with existing records");
        } catch (IllegalArgumentException e) {
            throw new UserServiceException(e.getMessage());
        } catch (Exception e) {
            throw new UserServiceException("Failed to create user due to an unexpected error");
        }
    }

    private void validateEmailUniqueness(String email) throws UserServiceException {
        if (userRepository.findByEmail(email) != null) {
            throw new UserServiceException("This email address is already registered");
        }
    }

    private UserEntity createAndValidateUserEntity(UserReq user) throws UserServiceException {
        UserEntity entity = UserMapper.mapToUserEntity(user, roleRepository, profilesRepo, sectionRepo, userRepository);
        if (entity == null) {
            throw new UserServiceException("Required user information is missing");
        }
        return entity;
    }

    private void enrichUserEntity(UserReq user, UserEntity entity) {
        entity.setUserId(idUtils.generateId(30));
        entity.setJoin_date(user.getJoiningDate() != null ? user.getJoiningDate() : new Date());
        entity.setRoaster(user.getRoaster());
        entity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        if (!user.getAddresses().isEmpty()) {
            entity.setAddresses(createAddressEntities(user.getAddresses(), entity));
        }
    }

    private List<AddressEntity> createAddressEntities(List<AddressDTO> addresses, UserEntity userEntity) {
        return addresses.stream()
                .map(addressDto -> {
                    AddressEntity entity = UserMapper.mapToAddressEntity_(addressDto);
                    entity.setUserDetails(userEntity);
                    entity.setAddressId(idUtils.generateId(30));
                    entity.setIsDefault(addressDto.getIsDefault());
                    return entity;
                })
                .collect(Collectors.toList());
    }

    private void setUserRelationships(UserReq user, UserEntity entity) {
        entity.setRoles(getValidRoles(user.getRoles()));
        entity.setSections(getValidSections(user.getSections()));
        entity.setProfiles(getValidProfiles(user.getProfiles()));
    }

    private Collection<RoleEntity> getValidRoles(Collection<String> roleNames) {
        return roleNames.stream()
                .map(roleRepository::findByName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Collection<SectionEntity> getValidSections(Collection<String> sectionNames) {
        return sectionNames.stream()
                .map(sectionRepo::findBySection)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Collection<ProfilesEntity> getValidProfiles(Collection<String> profileNames) {
        return profileNames.stream()
                .map(profilesRepo::findByName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private UserEntity saveAndProcessUser(UserEntity entity, UserReq user) throws UserServiceException {
        UserEntity storedUser = userRepository.save(entity);
        if (storedUser == null) {
            throw new UserServiceException("Failed to save user data");
        }

        updateUtils.handleAdminUpdates(storedUser, user);
        updateRelationshipEntities(storedUser);
        return storedUser;
    }

    private void updateRelationshipEntities(UserEntity user) {
        user.getSections().forEach(sec -> {
            sec.getUsers().add(user);
            sectionRepo.save(sec);
        });

        user.getProfiles().forEach(pro -> {
            pro.getUsers().add(user);
            profilesRepo.save(pro);
        });

        user.getRoles().forEach(role -> {
            role.getUsers().add(user);
            roleRepository.save(role);
        });
    }

    private void notifySystemsAndCacheCredentials(UserEntity user, String plainPassword) {
        LMSUser lmsUser = convertToLMSUser(user);
        messageProducerService.sendMessage("user.queue", lmsUser);
        messageProducerService.sendMessage("user.queue.roster", lmsUser);
        redisService.setValue(user.getEmployeeId(), plainPassword);
    }

    @Override
    @Deprecated
    public UserDto updateUserProfile(MultipartFile file, String userid) throws Exception {
        UserEntity user = userRepository.findByUserId(userid);
        if (user == null) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String fileName = idUtils.generateId(10) + file.getOriginalFilename();
        user.setProfilePic(fileName);

        Path filePath = uploadPath.resolve(Objects.requireNonNull(fileName));
        file.transferTo(filePath.toFile());

        UserEntity save = userRepository.save(user);
        return UserMapper.mapToUserDto(save);
    }

    @Override
    public UserDto getUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            throw new UsernameNotFoundException(email);
        }

        return UserMapper.mapToUserDto(userEntity);
    }



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String[] parts = email.split(" ");
        String email_ = parts[0];
        String loginType = parts.length > 1 ? parts[1] : "DEFAULT";

        if ("TEMP".equals(loginType)) {
            TempUser tempUser = tempUserRepo.findByEmail(email_);
            UserEntity userEntity = new UserEntity();
            userEntity.setUserId(tempUser.getUserId());
            userEntity.setFirstName(tempUser.getFirstName());
            userEntity.setLastName(tempUser.getLastName());
            userEntity.setEmail(tempUser.getEmail());
            userEntity.setEncryptedPassword(tempUser.getPassword());
            userEntity.setEmailVerificationStatus(false);

            RoleEntity role = UserMapper.mapRoleToRoleEntity(userEntity);
            userEntity.setRoles(Collections.singleton(role));

            return new UserPrincipal(userEntity);
        } else {

            UserEntity userEntity = userRepository.findByEmployeeId(email);

            if (userEntity == null) {
                throw new UsernameNotFoundException(email);
            }
            return new UserPrincipal(userEntity);
        }
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UsernameNotFoundException("User with ID: " + userId + " not found");
        }
        return UserMapper.transformToUserDto(userEntity);
    }

    @Override
    public List<UserDto> getAdminsForUserByUserId(String userId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            return Collections.emptyList();
        }

        List<UserEntity> admins = user.getMyAdmins();
        if (admins == null) {
            return Collections.emptyList();
        }

        return admins.stream()
                .map(admin -> {
                    UserDto userDto = UserMapper.transformToUserDto(admin);
                    Integer highestRolePriority = admin.getRoles() != null ?
                            admin.getRoles().stream()
                                    .map(RoleEntity::getPriority)
                                    .min(Integer::compare)
                                    .orElse(Integer.MAX_VALUE) :
                            Integer.MAX_VALUE;
                    userDto.setHighestRolePriority(highestRolePriority);
                    return userDto;
                }).collect(Collectors.toList());
    }

    @Override
    public UserRest getUserByUserId_(String userId) {
        TempUser tempUser = tempUserRepo.findTempUserByUserId(userId);
        UserRest userRest = new UserRest();
        userRest.setUserId(tempUser.getUserId());
        userRest.setFirstName(tempUser.getFirstName());
        userRest.setLastName(tempUser.getLastName());
        userRest.setEmail(tempUser.getEmail());
        userRest.setRoles(Arrays.asList("ROLE_TEMP"));
        return userRest;
    }

    @Override
    public UserDto updateUser(String userId, UserReq userDto) throws Exception {
        if (userDto == null) {
            throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
        }
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        if (userDto.getFirstName() != null && !userDto.getFirstName().trim().isEmpty()) {
            userEntity.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null && !userDto.getLastName().trim().isEmpty()) {
            userEntity.setLastName(userDto.getLastName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().trim().isEmpty()) {
            userEntity.setEmail(userDto.getEmail());
        }
        if (userDto.getPhone() != null) {
            userEntity.setPhone(userDto.getPhone());
        }
        if (userDto.getGender() != null) {
            userEntity.setGender(userDto.getGender());
        }
        if (userDto.getIsSltEmp() != null) {
            userEntity.setIsSltEmp(userDto.getIsSltEmp());
        }
        if (userDto.getIsSltIntern() != null) {
            userEntity.setIsSltIntern(userDto.getIsSltIntern());
        }

        if (userDto.getSltId() != null) {
            userEntity.setSltId(userDto.getSltId());
        }

        if (userDto.getEmployeeId() != null) {
            userEntity.setEmployeeId(userDto.getEmployeeId());
        }

        if (userDto.getActive() != null) {
            userEntity.setActive(userDto.getActive());
        }
       
        List<AddressEntity> addressEntities = handleAddresses(userEntity, userDto.getAddresses());
        userEntity.setAddresses(addressEntities);

        Collection<RoleEntity> role_user = userEntity.getRoles();
        Collection<ProfilesEntity> profile_user = userEntity.getProfiles();
        Collection<SectionEntity> section_user = userEntity.getSections();

        if (role_user != null && userDto.getRoles() != null) {
            if (!role_user.isEmpty() && !userDto.getRoles().isEmpty()) {
                for (String role : userDto.getRoles()) {
                    RoleEntity roleEntity = roleRepository.findByName(role);
                    if (roleEntity != null) {

                        role_user.forEach(role_ -> {
                            if (role_ != null && (!role_.getName().equals(roleEntity.getName()))) {
                                role_user.add(roleEntity);
                            }
                        });

                    }
                }
            }
        }

        if (section_user != null && userDto.getSections() != null) {
            if (!userDto.getSections().isEmpty() && !section_user.isEmpty()) {
                userDto.getSections().forEach(sect -> {
                    SectionEntity sec = sectionRepo.findBySection(sect);
                    if (sec != null) {
                        section_user.forEach(section -> {
                            if (section != null && (!section.getSection().equals(sec.getSection()))) {
                                section_user.add(sec);
                            }
                        });
                    }
                });
            }
        }

        if (userDto.getProfiles() != null && profile_user != null) {
            if (!userDto.getProfiles().isEmpty() && !profile_user.isEmpty()) {
                userDto.getProfiles().forEach(profile -> {
                    ProfilesEntity pr = profilesRepo.findByName(profile);
                    if (pr != null) {
                        profile_user.forEach(prof -> {
                            if (prof != null && (!prof.getName().equals(prof.getName()))) {
                                profile_user.add(prof);
                            }
                        });
                    }
                });
            }
        }

        userEntity.setRoles(role_user);
        userEntity.setSections(section_user);
        userEntity.setProfiles(profile_user);

        UserEntity updatedUserEntity = userRepository.save(userEntity);
        if (updatedUserEntity == null) {
            throw new Exception(ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage());
        }

        if (!updatedUserEntity.getSections().isEmpty()) {
            updatedUserEntity.getSections().forEach(sec -> {
                sec.getUsers().add(updatedUserEntity);
                sectionRepo.save(sec);
            });
        }

        if (!updatedUserEntity.getProfiles().isEmpty()) {
            updatedUserEntity.getProfiles().forEach(pro -> {
                pro.getUsers().add(updatedUserEntity);
                profilesRepo.save(pro);
            });
        }

        if (!updatedUserEntity.getRoles().isEmpty()) {
            updatedUserEntity.getRoles().forEach(role -> {
                role.getUsers().add(updatedUserEntity);
                roleRepository.save(role);
            });
        }

        return UserMapper.transformToUserDto(updatedUserEntity);
    }

    private List<AddressEntity> handleAddresses(UserEntity userEntity, List<AddressDTO> addressDtos) {
        List<AddressEntity> existingAddresses = userEntity.getAddresses();
        Map<String, AddressEntity> existingAddressMap = existingAddresses.stream()
                .collect(Collectors.toMap(AddressEntity::getAddressId, Function.identity()));

        for (AddressDTO addressDto : addressDtos) {
            AddressEntity addressEntity;

            if (addressDto.getAddressId() != null && existingAddressMap.containsKey(addressDto.getAddressId())) {
                addressEntity = existingAddressMap.get(addressDto.getAddressId());
            } else {
                addressEntity = new AddressEntity();
                addressEntity.setUserDetails(userEntity);
                addressEntity.setAddressId(idUtils.generateId(10));
            }

            addressEntity.setCity(addressDto.getCity());
            addressEntity.setCountry(addressDto.getCountry());
            addressEntity.setStreetName(addressDto.getStreetName());
            addressEntity.setPostalCode(addressDto.getPostalCode());

            if (Boolean.TRUE.equals(addressDto.getIsDefault())) {
                for (AddressEntity addr : existingAddresses) {
                    if (addr.getIsDefault() && !addr.getAddressId().equals(addressEntity.getAddressId())) {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    }
                }
                addressEntity.setIsDefault(true);
                addressEntity = addressRepository.save(addressEntity);
            } else addressEntity.setIsDefault(existingAddresses.isEmpty());

            if (!existingAddresses.contains(addressEntity)) {
                existingAddresses.add(addressEntity);
            }

        }

        if (!existingAddresses.isEmpty() && existingAddresses.stream().noneMatch(AddressEntity::getIsDefault)) {
            AddressEntity defaultAddress = existingAddresses.get(0);
            defaultAddress.setIsDefault(true);
            addressRepository.save(defaultAddress);
        }
        return existingAddresses;
    }

    @Transactional
    @Override
    public void deleteUser(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
        userEntity.setActive(0);
        userRepository.save(userEntity);

    }

    @Override
    public List<UserDto> getUsers(int page, int limit) {
        List<UserDto> returnValue = new ArrayList<>();

        if (page > 0) {
            page = page - 1;
        }

        Pageable pageableRequest = PageRequest.of(page, limit);

        Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);
        List<UserEntity> users = usersPage.getContent();

        for (UserEntity userEntity : users) {

            Collection<String> roles = new ArrayList();
            Collection<String> authorities = new ArrayList();
            Collection<String> sections = new ArrayList();
            Collection<String> profiles = new ArrayList();

            if (!userEntity.getRoles().isEmpty()) {
                userEntity.getRoles().forEach(role -> {
                    roles.add(role.getName());
                    role.getAuthorities().forEach(aut -> {
                        authorities.add(aut.getName());
                    });
                });
            }

            if (!userEntity.getSections().isEmpty()) {
                userEntity.getSections().forEach(sec -> {
                    sections.add(sec.getSection());
                });
            }

            if (!userEntity.getProfiles().isEmpty()) {
                userEntity.getProfiles().forEach(sec -> {
                    profiles.add(sec.getName());
                });
            }

            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(userEntity, userDto);

            userDto.setSections(sections);
            userDto.setAuthorities(authorities);
            userDto.setRoles(roles);
            userDto.setProfiles(profiles);

            returnValue.add(userDto);
        }

        return returnValue;
    }

    @Override
    public List<UserDto> getUsersForLms() {
        List<UserDto> returnValue = new ArrayList<>();
        List<UserEntity> users = userRepository.findAll();
        for (UserEntity userEntity : users) {

            Collection<String> roles = new ArrayList();
            Collection<String> authorities = new ArrayList();
            Collection<String> sections = new ArrayList();
            Collection<String> profiles = new ArrayList();

            if (!userEntity.getRoles().isEmpty()) {
                userEntity.getRoles().forEach(role -> {
                    roles.add(role.getName());
                    role.getAuthorities().forEach(aut -> {
                        authorities.add(aut.getName());
                    });
                });
            }

            if (!userEntity.getSections().isEmpty()) {
                userEntity.getSections().forEach(sec -> {
                    sections.add(sec.getSection());
                });
            }

            if (!userEntity.getProfiles().isEmpty()) {
                userEntity.getProfiles().forEach(sec -> {
                    profiles.add(sec.getName());
                });
            }

            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(userEntity, userDto);

            userDto.setSections(sections);
            userDto.setAuthorities(authorities);
            userDto.setRoles(roles);
            userDto.setProfiles(profiles);

            returnValue.add(userDto);
        }

        return returnValue;
    }

    @Override
    public Page<UserBasicDto> findAllBasicUserDtos(int page, int limit) {
        Pageable pageableRequest = PageRequest.of(page, limit);
        return userRepository.findAllBasicUserDtos(pageableRequest);
    }

    @Override
    public boolean verifyEmailToken(String token) {
        boolean returnValue = false;

        UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);

        if (userEntity != null) {
            boolean hastokenExpired = IdUtils.hasTokenExpired(token);
            if (!hastokenExpired) {
                userEntity.setEmailVerificationToken(null);
                userEntity.setEmailVerificationStatus(Boolean.TRUE);
                userRepository.save(userEntity);
                returnValue = true;
            }
        }

        return returnValue;
    }

    @Override
    public boolean requestPasswordReset(String email) {

        boolean returnValue = false;

        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            return returnValue;
        }

        String token = new IdUtils().generatePasswordResetToken(userEntity.getUserId());

        PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
        passwordResetTokenEntity.setToken(token);
        passwordResetTokenEntity.setUserDetails(userEntity);
        passwordResetTokenRepository.save(passwordResetTokenEntity);

        return !returnValue;
    }

    @Override
    public boolean resetPassword(String token, String password) {
        boolean returnValue = false;

        if (IdUtils.hasTokenExpired(token)) {
            return returnValue;
        }

        PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);

        if (passwordResetTokenEntity == null) {
            return returnValue;
        }

        String encodedPassword = bCryptPasswordEncoder.encode(password);

        UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
        userEntity.setEncryptedPassword(encodedPassword);
        UserEntity savedUserEntity = userRepository.save(userEntity);

        if (savedUserEntity != null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword)) {
            returnValue = true;
        }

        passwordResetTokenRepository.delete(passwordResetTokenEntity);

        return returnValue;
    }

    @Override
    public Resource loadImageAsResource(String imageName) throws MalformedURLException {
        Path filePath = Paths.get(UPLOAD_DIR).resolve(imageName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists()) {
            return resource;
        } else {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public UserEntity getUserByE(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            return null;
        } else {
            return userEntity;
        }
    }

    @Override
    public Resource getImage(String userId) throws MalformedURLException {
        String profilePic = userRepository.findByUserId(userId).getProfilePic();
        return this.loadImageAsResource(profilePic);
    }

    @Override
    public void resetPassWord(UserPasswordReset userPasswordReset) {
        UserEntity user = userRepository.findByUserId(userPasswordReset.getUserId());
        if (user == null) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
        if (user.getEncryptedPassword() == null) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
        if (userPasswordReset.getCurrentPassword() == null) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
        if (bCryptPasswordEncoder.matches(userPasswordReset.getCurrentPassword(), user.getEncryptedPassword())) {
            if (userPasswordReset.getNewPassword().equals(userPasswordReset.getConfirmPassword())) {
                user.setEncryptedPassword(bCryptPasswordEncoder.encode(userPasswordReset.getNewPassword()));
                userRepository.save(user);
            }
        }
    }

    @Override
    public boolean userAddress(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity.getAddresses() == null) {
            return false;
        } else return !userEntity.getAddresses().isEmpty();
    }

    @Override
    public List<UserDtoArchive> findByRoleName(String roleName) {
        List<UserDtoArchive> returnValue = new ArrayList<>();

        userRepository.findByRoleName(roleName.toUpperCase()).forEach(user -> {
            if (user == null)
                throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

            UserDtoArchive userDto = new UserDtoArchive();

            if (user.getUserId() != null)
                userDto.setUserId(user.getUserId());
            if (user.getFirstName() != null)
                userDto.setFirstName(user.getFirstName());
            if (user.getLastName() != null)
                userDto.setLastName(user.getLastName());
            if (user.getEmail() != null)
                userDto.setEmail(user.getEmail());
            if (user.getEmployeeId() != null)
                userDto.setEmployeeId(user.getEmployeeId());
            if (user.getSltId() != null)
                userDto.setSltId(user.getSltId());

            returnValue.add(userDto);
        });

        return returnValue;
    }

    @Override
    public List<RoleDTO> getRole() {
        return ((List<RoleEntity>) roleRepository.findAll()).stream()
                .map(UserMapper::mapToRoleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoleDTOArchive> getRoleArchive() {
        return ((List<RoleEntity>) roleRepository.findAll()).stream()
                .map(UserMapper::mapToRoleDTO_)
                .collect(Collectors.toList());
    }

    @Override
    public List<SectionDTO> getSection() {
        List<SectionEntity> sectionEntities = (List<SectionEntity>) sectionRepo.findAll();
        List<SectionDTO> section_array = sectionEntities.stream()
                .map(section -> {
                    SectionDTO roleDTO = UserMapper.mapToSectionDTO(section);
                    return roleDTO;
                })
                .collect(Collectors.toList());
        return section_array;
    }

    @Override
    public List<ProfilesDTO> getProfile() {
        List<ProfilesEntity> profileEntities = (List<ProfilesEntity>) profilesRepo.findAll();

        List<ProfilesDTO> section_array = profileEntities.stream()
                .map(profile -> {
                    ProfilesDTO roleDTO = UserMapper.mapToProfilesDTO(profile);
                    return roleDTO;
                })
                .collect(Collectors.toList());
        return section_array;
    }

    @Override
    public List<Map<String, String>> getAuthority() {
        List<Map<String, String>> profiles = new ArrayList<>();
        authorityRepo.findAll().forEach(auth -> {
            Map<String, String> profileMap = new ConcurrentHashMap<>();
            profileMap.put("ID", String.valueOf(auth.getId()));
            profileMap.put("name", auth.getName());
            profileMap.put("weight" , String.valueOf(auth.getWeight()));
            profiles.add(profileMap);
        });
        return profiles;
    }

    @Override
    @Deprecated
    public RoleDTO saveRole(RoleReq req) {
        RoleEntity roleEntity = roleRepository.findByName(req.getName());
        if (roleEntity == null) {
            roleEntity = implUtils.saveRole_(req);
        } else {
            roleEntity = implUtils.updateRole(req, roleEntity);
        }
        roleEntity = roleRepository.save(roleEntity);
        return UserMapper.mapToRoleDTO(roleEntity);
    }

    @Override
    public RoleDTOArchive saveRoleV1(RoleReq req) {
        if (req == null) {
            throw new IllegalArgumentException("Role request cannot be null");
        }

        Optional<RoleEntity> roleEntity = Optional.empty();
        if (req.getRoleId() != null && !req.getRoleId().isEmpty()) {
            roleEntity = roleRepository.findByPublicId(req.getRoleId());
        }else if (req.getName() != null && !req.getName().isEmpty()) {
            roleEntity = Optional.ofNullable(roleRepository.findByName(req.getName()));
        }

        RoleEntity entityToSave = roleEntity
                .map(existingEntity -> implUtils.updateRole(req, existingEntity))
                .orElseGet(() -> implUtils.saveRole_(req));

        RoleEntity savedEntity = roleRepository.save(entityToSave);
        return UserMapper.mapToRoleDTO_(savedEntity);
    }

    @Override
    public RoleDTOArchive saveRoleV2(RoleReq req) {
        return roleService.saveRole_(req);
    }

    @Override
    public AuthorityDTO saveAuthority(AuthReq req) {
        if (req == null) {
            throw new IllegalArgumentException("AuthReq cannot be null");
        }

        if (req.getNewName() == null || req.getNewName().trim().isEmpty()) {
            throw new IllegalArgumentException("newName cannot be null or empty");
        }

        AuthorityEntity authorityEntity;

        if (req.getOldName() != null && !req.getOldName().trim().isEmpty()) {
            authorityEntity = authorityRepo.findByName(req.getOldName());
            if (authorityEntity != null) {
                authorityEntity.setName(req.getNewName());
            } else {
                authorityEntity = new AuthorityEntity();
                authorityEntity.setName(req.getNewName());
            }
        } else {
            authorityEntity = new AuthorityEntity();
            authorityEntity.setName(req.getNewName());
        }

        if(req.getWeight() != null) {
            authorityEntity.setWeight(req.getWeight());
        }

        try {
            AuthorityEntity savedEntity = authorityRepo.save(authorityEntity);
            return UserMapper.mapToAuthorityDTO(savedEntity);
        } catch (DataIntegrityViolationException ex) {
            AuthorityEntity existingEntity = authorityRepo.findByName(req.getNewName());
            if (existingEntity != null) {
                return UserMapper.mapToAuthorityDTO(existingEntity);
            } else {
                throw new RuntimeException("Failed to save authority and no existing entity found", ex);
            }
        }
    }

    @Override
    @Transactional
    public SectionDTO saveSection(SectionReq req, boolean swap) {
        if (req == null) {
            throw new IllegalArgumentException("Section request cannot be null");
        }
        if (req.getSection() == null || req.getSection().trim().isEmpty()) {
            throw new IllegalArgumentException("Section cannot be null or empty");
        }

        if((sectionRepo.findBySection(req.getSection()) != null) && !swap)
            throw new IllegalArgumentException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

        SectionEntity sectionEntity = findOrCreateSection(req);

        if (req.getDeletedUsers() != null) {
            removeUsersFromSection(req.getDeletedUsers(), sectionEntity);
        }

        if (req.getAddedUsers() != null) {
            addUsersToSection(req.getAddedUsers(), sectionEntity);
        }

        SectionEntity savedEntity = sectionRepo.save(sectionEntity);
        return UserMapper.mapToSectionDTO(savedEntity);
    }

    private SectionEntity findOrCreateSection(SectionReq req) {
        if (StringUtils.hasText(req.getPublicId())) {
            return sectionRepo.findByPublicId(req.getPublicId())
                    .orElseGet(() -> createNewSection(req));
        } else {
            return Optional.ofNullable(sectionRepo.findBySection(req.getSection()))
                    .orElseGet(() -> createNewSection(req));
        }
    }

    private SectionEntity createNewSection(SectionReq req) {
        SectionEntity section = new SectionEntity();
        section.setPublicId(idUtils.generateId(10));
        section.setSection(req.getSection());
        section.setUsers(new ArrayList<>());
        return section;
    }

    private void addUsersToSection(List<String> userIds, SectionEntity section) {
        userIds.forEach(userId -> {
            UserEntity user = userRepository.findByUserId(userId);
            if (user != null) {
                section.addUser(user);
            }
        });
    }

    private void removeUsersFromSection(List<String> userIds, SectionEntity section) {
        userIds.forEach(userId -> {
            UserEntity user = userRepository.findByUserId(userId);
            if (user != null) {
                section.removeUser(user);
            }
        });
    }



    public ProfilesEntity mapProfileReqToProfilesEntity(ProfileReq profileReq) {
        ProfilesEntity profilesEntity = new ProfilesEntity();

        profilesEntity.setName(profileReq.getName());
        profilesEntity.setWorkStart(profileReq.getWorkStart());
        profilesEntity.setWorkEnds(profileReq.getWorkEnds());
        profilesEntity.setIgnoreSl(profileReq.getIgnoreSl());
        profilesEntity.setGracePeriodStart(profileReq.getGracePeriodStart());
        profilesEntity.setHdStart(profileReq.getHdStart());
        profilesEntity.setSlStartMorning(profileReq.getSlStartMorning());
        profilesEntity.setSlStartEvening(profileReq.getSlStartEvening());
        profilesEntity.setPossibleFpLocations(profileReq.getPossibleFpLocations());
        profilesEntity.setDefaultHrs(profileReq.getDefaultHrs());
        profilesEntity.setHdHrs(profileReq.getHdHrs());
        profilesEntity.setMinHrsForSl(profileReq.getMinHrsForSl());
        profilesEntity.setShortLeaveCount(profileReq.getShortLeaveCount());
        profilesEntity.setHdEndsMorning(profileReq.getHdEndsMorning());
        profilesEntity.setFlexiDays(profileReq.getFlexiDays());
        profilesEntity.setFlexiHrsStart(profileReq.getFlexiHrsStart());

        return profilesEntity;
    }

    @Override
    @Transactional
    public ProfilesDTO saveProfile(ProfileReq req, boolean swap) {
        if (req == null) {
            throw new IllegalArgumentException("Profile request cannot be null");
        }
        if((profilesRepo.findByName(req.getName()) != null) && !swap)
            throw new IllegalArgumentException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

        final String lockKey = StringUtils.hasText(req.getPublicId())
                ? req.getPublicId()
                : "NEW_PROFILE_" + System.currentTimeMillis();

        synchronized (lockKey.intern()) {
            ProfilesEntity profilesEntity = findOrCreateProfileEntity(req);

            if (req.getDeletedUsers() != null) {
                removeUsersFromProfile(req.getDeletedUsers(), profilesEntity);
            }

            if (req.getAddedUsers() != null) {
                addUsersToProfile(req.getAddedUsers(), profilesEntity);
            }

            ProfilesEntity savedEntity = profilesRepo.save(profilesEntity);
            return UserMapper.mapToProfilesDTO(savedEntity);
        }
    }

    private ProfilesEntity findOrCreateProfileEntity(ProfileReq req) {
        if (StringUtils.hasText(req.getPublicId())) {
            ProfilesEntity entity = profilesRepo.findByPublicId(req.getPublicId());
            if (entity == null) {
                throw new NoSuchElementException("Profile not found with publicId: " + req.getPublicId());
            }

            if (StringUtils.hasText(req.getName())) {
                entity.setName(req.getName());
            }
            updateProfileFields(entity, req);

            return entity;
        } else {
            ProfilesEntity newEntity = mapProfileReqToProfilesEntity(req);
            newEntity.setPublicId(idUtils.generateId(10));
            return newEntity;
        }
    }

    private void updateProfileFields(ProfilesEntity entity, ProfileReq req) {
        if (req.getWorkStart() != null) entity.setWorkStart(req.getWorkStart());
        if (req.getWorkEnds() != null) entity.setWorkEnds(req.getWorkEnds());
    }

    private void addUsersToProfile(List<String> userIds, ProfilesEntity profile) {
        if (userIds == null || profile == null) return;

        userIds.forEach(userId -> {
            UserEntity user = userRepository.findByUserId(userId);
            if (user != null) {
                if (profile.getUsers() == null) {
                    profile.setUsers(new ArrayList<>());
                }
                profile.getUsers().add(user);

                if (user.getProfiles() == null) {
                    user.setProfiles(new ArrayList<>());
                }
                user.getProfiles().add(profile);
            }
        });
    }

    private void removeUsersFromProfile(List<String> userIds, ProfilesEntity profile) {
        if (userIds == null || profile == null || profile.getUsers() == null) return;

        userIds.forEach(userId -> {
            UserEntity user = userRepository.findByUserId(userId);
            if (user != null) {
                profile.getUsers().remove(user);

                if (user.getProfiles() != null) {
                    user.getProfiles().remove(profile);
                }
            }
        });
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Optional<RoleEntity> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isPresent()) {
            RoleEntity role = roleOpt.get();

            for (UserEntity user : role.getUsers()) {
                user.getRoles().remove(role);
                userRepository.save(user);
            }

            for (AuthorityEntity authority : role.getAuthorities()) {
                authority.getRoles().remove(role);
                authorityRepo.save(authority);
            }

            roleRepository.delete(role);
        } else {
            throw new RuntimeException("Role not found with id: " + roleId);
        }
    }

    @Override
    @Transactional
    public void deleteRoleV2(Long roleId) {
        roleRepository.findById(roleId).ifPresent(role -> roleRepository.delete(role));
    }

    @Override
    public void deleteProfile(Long profileId) {
        profilesRepo.findById(profileId).ifPresent(profile -> profilesRepo.delete(profile));
    }

    @Override
    public UserDetails getUserDetailsByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        String[] parts = userId.split(" ");
        String user_id = parts[0].trim();
        String loginType = parts.length > 1 ? parts[1].trim() : "DEFAULT";


        try {
            if ("TEMP".equalsIgnoreCase(loginType)) {
                TempUser tempUser = tempUserRepo.findTempUserByUserId(user_id);

                if (tempUser == null) {
                    System.out.println("No temporary user found with ID: " + user_id);
                    return null;
                }


                UserEntity userEntity = new UserEntity();
                userEntity.setUserId(tempUser.getUserId() + " " + "TEMP");
                userEntity.setFirstName(tempUser.getFirstName());
                userEntity.setLastName(tempUser.getLastName());
                userEntity.setEmail(tempUser.getEmail());
                userEntity.setEncryptedPassword(tempUser.getPassword());
                userEntity.setEmailVerificationStatus(false);

                RoleEntity role = UserMapper.mapRoleToRoleEntity(userEntity);
                userEntity.setRoles(Arrays.asList(role));

                return new UserPrincipal(userEntity);
            } else if("LMS".equalsIgnoreCase(loginType)) {
            	
            	UserEntity userEntity = new UserEntity();
                userEntity.setUserId("lms-service" + " " + "LMS");
                userEntity.setFirstName("LMS");
                userEntity.setLastName("LMS");
                userEntity.setEmail("lms@slt.com");
                userEntity.setEncryptedPassword(null);
                userEntity.setEmailVerificationStatus(true);

                RoleEntity role = UserMapper.mapRoleToRoleEntityForLms(userEntity);
                userEntity.setRoles(Arrays.asList(role));

                return new UserPrincipal(userEntity);
                
            }
            else {
                UserEntity userEntity = userRepository.findByUserId(user_id);

                if (userEntity == null) {
                    System.out.println("No user found with ID: " + user_id);
                    return null;
                }

                return new UserPrincipal(userEntity);
            }
        } catch (Exception e) {
            System.err.println("Error fetching user details for ID: " + user_id);
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public void rebalancePriorities() {
        List<RoleEntity> roles = roleRepository.findAllByOrderByPriorityAsc();

        int newPriority = 10;
        for (RoleEntity role : roles) {
            if (role.getPriority() <= 9) continue;

            role.setPriority(newPriority);
            roleRepository.save(role);
            newPriority += 10;
        }
    }

    @Override
    public Page<UserEntity> findByRolePriorityBetween(int max, int min, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByRolePriorityBetween(max, min, pageable);
    }

    @Override
    public Page<UserEntity> findByRolePriorityBetweenV1(int max, int min, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoleEntity> rolesPage = roleRepository.findRolesByPriorityBetween(min, max, pageable);

        List<UserEntity> users = rolesPage.getContent().stream()
                .filter(role -> !(role.getPriority() >= 30 && role.getPriority() <= 49))
                .map(RoleEntity::getUsers)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        return new PageImpl<>(users, pageable, rolesPage.getTotalElements());
    }

    @Override
    public List<String> getAllRoleNames() {
        return StreamSupport.stream(roleRepository.findAll().spliterator(), false)
                .map(RoleEntity::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllSectionNames() {
        return StreamSupport.stream(sectionRepo.findAll().spliterator(), false)
                .map(SectionEntity::getSection)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllProfileNames() {
        return StreamSupport.stream(profilesRepo.findAll().spliterator(), false)
                .map(ProfilesEntity::getName)
                .collect(Collectors.toList());
    }


    @Override
    public Page<UserEntity> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).map(user->{
            List<UserAdminDto> adminDtos = user.getMyAdmins().stream()
                    .map(adminUser -> {
                        UserAdminDto adminDto = new UserAdminDto();
                        adminDto.setUserId(adminUser.getUserId());
                        adminDto.setFirstName(adminUser.getFirstName());
                        adminDto.setLastName(adminUser.getLastName());
                        adminDto.setEmail(adminUser.getEmail());
                        adminDto.setEmployeeId(adminUser.getEmployeeId());
                        adminDto.setSltId(adminUser.getSltId());
                        return adminDto;
                    })
                    .collect(Collectors.toList());

            user.setAdministrativesDto(adminDtos);
            return user;
        });
    }

    @Override
    public Page<UserDto> getAllUsersTDTO(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).map(user -> {
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setUserId(user.getUserId());
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setEmail(user.getEmail());
            userDto.setEmployeeId(user.getEmployeeId());
            userDto.setSltId(user.getSltId());
            userDto.setEncryptedPassword(user.getEncryptedPassword());
            userDto.setProfilePic(user.getProfilePic());
            userDto.setEmailVerificationToken(user.getEmailVerificationToken());
            userDto.setEmailVerificationStatus(user.getEmailVerificationStatus());
            userDto.setIsSltEmp(user.getIsSltEmp());
            userDto.setIsSltIntern(user.getIsSltIntern());
            userDto.setActive(user.getActive());
            userDto.setPhone(user.getPhone());
            userDto.setGender(user.getGender());
            userDto.setRoaster(user.getRoaster());
            userDto.setJoin_date(user.getJoin_date());

            if (user.getRoles() != null) {
                List<String> roleNames = user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toList());
                userDto.setRoles(roleNames);
            }

            if (user.getSections() != null) {
                List<String> sectionNames = user.getSections().stream()
                        .map(section -> section.getSection())
                        .collect(Collectors.toList());
                userDto.setSections(sectionNames);
            }

            if (user.getProfiles() != null) {
                List<String> profileNames = user.getProfiles().stream()
                        .map(profile -> profile.getName())
                        .collect(Collectors.toList());
                userDto.setProfiles(profileNames);
            }

            if (user.getAddresses() != null) {
                List<AddressDTO> addressDTOs = user.getAddresses().stream()
                        .map(address -> {
                            AddressDTO addressDTO = new AddressDTO();
                            return addressDTO;
                        })
                        .collect(Collectors.toList());
                userDto.setAddresses(addressDTOs);
            }

            if (user.getMyAdmins() != null) {
                List<UserAdminDto> adminDtos = user.getMyAdmins().stream()
                        .map(adminUser -> {
                            UserAdminDto adminDto = new UserAdminDto();
                            adminDto.setUserId(adminUser.getUserId());
                            adminDto.setFirstName(adminUser.getFirstName());
                            adminDto.setLastName(adminUser.getLastName());
                            adminDto.setEmail(adminUser.getEmail());
                            adminDto.setEmployeeId(adminUser.getEmployeeId());
                            adminDto.setSltId(adminUser.getSltId());
                            return adminDto;
                        })
                        .collect(Collectors.toList());
                userDto.setAdministratives(adminDtos);
            }

            return userDto;
        });
    }

    @Override
    public boolean checkAuth(String name) {
        if (authorityRepo.findByName(name) == null) {
            return false;
        } else
            return true;
    }

    @Override
    public void deleteAuth(Long authId) {
        authorityRepo.findById(authId).ifPresent(authority -> authorityRepo.delete(authority));
    }

    @Override
    public void deleteSection(Long sectionId) {
        sectionRepo.findById(sectionId).ifPresent(section -> sectionRepo.delete(section));
    }
   
    @Override
    public List<LMSUser> getAllUsersForService() {
        return userRepository.findAll().stream()
                .map(this::convertToLMSUser)
                .collect(Collectors.toList());
    }

   
    private LMSUser convertToLMSUser(UserEntity userEntity) {
        LMSUser lmsUser = new LMSUser();
        lmsUser.setEmployeeId(userEntity.getEmployeeId());
        lmsUser.setSltId(userEntity.getSltId());
        lmsUser.setFirstName(userEntity.getFirstName());
        lmsUser.setLastName(userEntity.getLastName());
        lmsUser.setEmail(userEntity.getEmail());
        lmsUser.setJoin_date(userEntity.getJoin_date());
        lmsUser.setPublicId(userEntity.getUserId());
        return lmsUser;
    }
}
