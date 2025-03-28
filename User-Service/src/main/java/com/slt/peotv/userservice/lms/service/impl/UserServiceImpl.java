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
import com.slt.peotv.userservice.lms.utils.IdUtils;
import com.slt.peotv.userservice.lms.shared.dto.*;
import com.slt.peotv.userservice.lms.shared.model.request.*;
import com.slt.peotv.userservice.lms.shared.model.response.ErrorMessages;
import com.slt.peotv.userservice.lms.shared.model.response.UserRest;
import com.slt.peotv.userservice.lms.utils.ImplUtils;
import com.slt.peotv.userservice.lms.utils.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
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

    private <T> void updateFieldIfNotNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
    @Override
    public UserDto createUser(UserReq user) throws Exception {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new UserServiceException("Record already exists");
        }

        UserEntity userEntity = UserMapper.mapToUserEntity(user, roleRepository, profilesRepo, sectionRepo, userRepository);
        if (userEntity == null) {
            throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
        }
        String publicUserId = idUtils.generateUserId(30);
        userEntity.setUserId(publicUserId);
        userEntity.setJoin_date(new Date());
        userEntity.setRoaster(user.getRoaster());

        if (user.getHod() != null) {
            String hod = user.getHod();
            UserEntity userHod = userRepository.findByEmployeeId(hod);
            if (userHod != null) {
                userEntity.setHod(userHod);
            }
        }
        if (user.getSupervisor() != null) {
            String sup = user.getHod();
            UserEntity userSup = userRepository.findByEmployeeId(sup);
            if (userSup != null) {
                userEntity.setSupervisor(userSup);
            }
        }
        updateFieldIfNotNull(user.getJoiningDate(), userEntity::setJoin_date);

        if (user.getOther() != null) {
            String other = user.getOther();
            UserEntity userOther = userRepository.findByEmployeeId(other);
            if (userOther != null) {
                userEntity.setOther(userOther);
            }
        }

        if (!user.getAddresses().isEmpty()) {
            List<AddressEntity> addressEntities = new ArrayList<>();
            for (AddressDTO addressDto : user.getAddresses()) {
                AddressEntity addressEntity = UserMapper.mapToAddressEntity_(addressDto);
                addressEntity.setUserDetails(userEntity);
                String publicAddressId = idUtils.generateUserId(30);
                addressEntity.setAddressId(publicAddressId);
                addressEntity.setIsDefault(addressDto.getIsDefault());
                addressEntities.add(addressEntity);
            }
            userEntity.setAddresses(addressEntities); // Set the addresses list
        }


        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        // Handle roles, sections, and profiles
        Collection<SectionEntity> sectionEntities = new HashSet<>();
        Collection<ProfilesEntity> profilesEntities = new HashSet<>();
        Collection<RoleEntity> roleEntities = new HashSet<>();

        if (!user.getRoles().isEmpty()) {
            for (String role : user.getRoles()) {
                RoleEntity roleEntity = roleRepository.findByName(role);
                if (roleEntity != null) {
                    roleEntities.add(roleEntity);
                }
            }
        }

        if (!user.getSections().isEmpty()) {
            user.getSections().forEach(sect -> {
                SectionEntity sec = sectionRepo.findBySection(sect);
                if (sec != null) {
                    sectionEntities.add(sec);
                }
            });
        }

        if (!user.getProfiles().isEmpty()) {
            user.getProfiles().forEach(profile -> {
                ProfilesEntity pr = profilesRepo.findByName(profile);
                if (pr != null) {
                    profilesEntities.add(pr);
                }
            });
        }
        userEntity.setRoles(roleEntities);
        userEntity.setSections(sectionEntities);
        userEntity.setProfiles(profilesEntities);

        // Save the user entity first
        UserEntity storedUserDetails = userRepository.save(userEntity);
        if (storedUserDetails == null) {
            throw new Exception(ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage());
        }

        if (!storedUserDetails.getSections().isEmpty()) {
            storedUserDetails.getSections().forEach(sec -> {
                sec.getUsers().add(storedUserDetails);
                sectionRepo.save(sec);
            });
        }

        if (!storedUserDetails.getProfiles().isEmpty()) {
            storedUserDetails.getProfiles().forEach(pro -> {
                pro.getUsers().add(storedUserDetails);
                profilesRepo.save(pro);
            });
        }

        if (!storedUserDetails.getRoles().isEmpty()) {
            storedUserDetails.getRoles().forEach(role -> {
                role.getUsers().add(storedUserDetails);
                roleRepository.save(role);
            });
        }

        LMSUser lmsUser = new LMSUser();
        lmsUser.setEmail(storedUserDetails.getEmail());
        lmsUser.setFirstName(storedUserDetails.getFirstName());
        lmsUser.setLastName(storedUserDetails.getLastName());
        lmsUser.setEmployeeId(storedUserDetails.getEmployeeId());
        lmsUser.setSltId(storedUserDetails.getSltId());

        messageProducerService.sendMessage("user.queue", lmsUser);
        redisService.setValue(storedUserDetails.getEmail(), user.getPassword());
        return UserMapper.mapToUserDto(storedUserDetails);
    }

    @Override
    public UserDto updateUserProfile(MultipartFile file, String userid) throws Exception {
        UserEntity user = userRepository.findByUserId(userid);
        if (user == null) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String fileName = idUtils.generateUserId(10) + file.getOriginalFilename();
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
            userEntity.setRoles(Set.of(role));

            return new UserPrincipal(userEntity);
        } else {

            UserEntity userEntity = userRepository.findByEmail(email);

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
    public UserRest getUserByUserId_(String userId) {
        TempUser tempUser = tempUserRepo.findTempUserByUserId(userId);
        UserRest userRest = new UserRest();
        userRest.setUserId(tempUser.getUserId());
        userRest.setFirstName(tempUser.getFirstName());
        userRest.setLastName(tempUser.getLastName());
        userRest.setEmail(tempUser.getEmail());
        userRest.setRoles(List.of("ROLE_TEMP"));
        return userRest;
    }

    @Override
    public UserDto updateUser(String userId, UserReq userDto) throws Exception {
        if (userDto == null) {
            throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
        }
        // Find the user entity by userId
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        // Update basic user details
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
        if (userDto.getHod() != null) {
            String hod = userDto.getHod();
            UserEntity userHod = userRepository.findByEmployeeId(hod);
            if (userHod != null) {
                userEntity.setHod(userHod);
            }
        }
        if (userDto.getSupervisor() != null) {
            String sup = userDto.getHod();
            UserEntity userSup = userRepository.findByEmployeeId(sup);
            if (userSup != null) {
                userEntity.setSupervisor(userSup);
            }
        }
        if (userDto.getOther() != null) {
            String other = userDto.getOther();
            UserEntity userOther = userRepository.findByEmployeeId(other);
            if (userOther != null) {
                userEntity.setOther(userOther);
            }
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

        // Save the updated user entity
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
                addressEntity.setAddressId(idUtils.generateAddressId(10));
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

        userRepository.delete(userEntity);

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
    public boolean verifyEmailToken(String token) {
        boolean returnValue = false;

        // Find user by token
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

        // Prepare new password
        String encodedPassword = bCryptPasswordEncoder.encode(password);

        // Update User password in database
        UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
        userEntity.setEncryptedPassword(encodedPassword);
        UserEntity savedUserEntity = userRepository.save(userEntity);

        // Verify if password was saved successfully
        if (savedUserEntity != null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword)) {
            returnValue = true;
        }

        // Remove Password Reset token from database
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
    public RoleDTOArchive saveRole_(RoleReq req) {
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
    public SectionDTO saveSection(SectionReq req) {
        if (req == null) {
            throw new IllegalArgumentException("Section request cannot be null");
        }

        Optional<SectionEntity> sectionEntity = Optional.empty();

        if (StringUtils.hasText(req.getPublicId())) {
            sectionEntity = sectionRepo.findByPublicId(req.getPublicId());
        }

        if (sectionEntity.isEmpty() && StringUtils.hasText(req.getSection())) {
            sectionEntity = Optional.ofNullable(sectionRepo.findBySection(req.getSection()));
        }

        // Process the entity (update existing or create new)
        SectionEntity entityToSave = sectionEntity
                .map(existingEntity -> {
                    synchronized (existingEntity) {
                        removeDeletedUsers(req, existingEntity);
                        if (StringUtils.hasText(req.getSection())) {
                            existingEntity.setSection(req.getSection());
                        }
                        return existingEntity;
                    }
                })
                .orElseGet(() -> createNewSectionEntity(req));

        // Save and return
        synchronized (sectionRepo) {
            return UserMapper.mapToSectionDTO(sectionRepo.save(entityToSave));
        }
    }

    private void removeDeletedUsers(SectionReq req, SectionEntity sectionEntity) {
        req.getDeletedUsers().forEach(userId -> {
            boolean removed = sectionEntity.getUsers().removeIf(user -> user.getUserId().equals(userId));
            if (removed) {
                synchronized (userRepository) {
                    UserEntity userEntity = userRepository.findByUserId(userId);
                    if (userEntity != null) {
                        userEntity.getSections().removeIf(role -> role.getSection().equals(sectionEntity.getSection()));
                        userRepository.save(userEntity); // Update the user entity
                    }
                }
            }
            System.out.println("User removed: " + removed);
        });
    }

    private SectionEntity createNewSectionEntity(SectionReq req) {
        SectionEntity sectionEntity = new SectionEntity();
        sectionEntity.setPublicId(idUtils.generateUserId(10));
        List<UserEntity> userEntities = new CopyOnWriteArrayList<>();
        sectionEntity.setSection(req.getSection());
        if (req.getAddedUsers() != null && !req.getAddedUsers().isEmpty()) {
            req.getAddedUsers().forEach(userId -> {
                synchronized (userRepository) {
                    UserEntity user = userRepository.findByUserId(userId);
                    if (user != null) {
                        userEntities.add(user);
                    }
                }
            });
        }
        sectionEntity.setUsers(Collections.unmodifiableList(userEntities));
        return sectionEntity;
    }

    public ProfilesEntity mapProfileReqToProfilesEntity(ProfileReq profileReq) {
        ProfilesEntity profilesEntity = new ProfilesEntity();

        // Map fields from ProfileReq to ProfilesEntity (excluding addedUsers and deletedUsers)
        profilesEntity.setName(profileReq.getName());
        profilesEntity.setWorkStart(profileReq.getWorkStart());
        profilesEntity.setWorkEnds(profileReq.getWorkEnds());
        profilesEntity.setIgnoreSl(profileReq.getIgnoreSl());
        profilesEntity.setGracePeriodeStart(profileReq.getGracePeriodeStart());
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

        // publicId and users are not mapped here
        return profilesEntity;
    }

    @Override
    public ProfilesDTO saveProfile(ProfileReq req) {
        ProfilesEntity profilesEntity;

        if (req.getPublicId() != null && !req.getPublicId().isEmpty()) {
            // Fetch the existing profile by publicId
            profilesEntity = profilesRepo.findByPublicId(req.getPublicId());

            if (profilesEntity == null) {
                throw new NoSuchElementException("Profile not found with publicId: " + req.getPublicId());
            }

            // Update the existing profile entity with new data
            if (req.getName() != null && !req.getName().isEmpty()) {
                profilesEntity.setName(req.getName());
            }
        } else {
            // Create a new profile entity (create scenario)
            profilesEntity = mapProfileReqToProfilesEntity(req);
            profilesEntity.setPublicId(idUtils.generateUserId(10));
        }

        List<UserEntity> userEntities = new CopyOnWriteArrayList<>();

        // Handle added users
        if (req.getAddedUsers() != null && !req.getAddedUsers().isEmpty()) {
            addUsersToProfile(req.getAddedUsers(), userEntities);
        }

        // Handle deleted users
        if (req.getDeletedUsers() != null && !req.getDeletedUsers().isEmpty()) {
            removeUsersFromProfile(req.getDeletedUsers(), profilesEntity);
        }

        // Save or update the profile entity
        synchronized (profilesRepo) {
            return UserMapper.mapToProfilesDTO(profilesRepo.save(profilesEntity));
        }
    }

    private void addUsersToProfile(List<String> addedUsers, List<UserEntity> userEntities) {
        addedUsers.forEach(userId -> {
            synchronized (userRepository) {
                UserEntity user = userRepository.findByUserId(userId);
                if (user != null) {
                    userEntities.add(user);
                }
            }
        });
    }

    private void removeUsersFromProfile(List<String> deletedUsers, ProfilesEntity profilesEntity) {
        deletedUsers.forEach(userId -> {
            synchronized (userRepository) {
                UserEntity user = userRepository.findByUserId(userId);
                if (user != null) {
                    // Remove the user from the profile
                    profilesEntity.getUsers().removeIf(u -> u.getUserId().equals(userId));
                    // Remove the profile from the user's profile list
                    user.getProfiles().removeIf(profile -> profile.getPublicId().equals(profilesEntity.getPublicId()));
                    userRepository.save(user); // Update the user entity
                    profilesRepo.save(profilesEntity);
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

            // Finally, delete the role
            roleRepository.delete(role);
        } else {
            throw new RuntimeException("Role not found with id: " + roleId);
        }
    }

    @Override
    public void deleteProfile(Long profileId) {
        Optional<ProfilesEntity> roleOpt = profilesRepo.findById(profileId);
        if (roleOpt.isPresent()) {
            ProfilesEntity profile = roleOpt.get();
            for (UserEntity user : profile.getUsers()) {
                user.getRoles().remove(profile);
                userRepository.save(user);
            }
            profilesRepo.delete(profile);
        } else {
            throw new RuntimeException("Role not found with id: " + profileId);
        }
    }

    @Override
    public UserDetails getUserDetailsByUserId(String userId) {
        // Validate input
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        // Parse userId and loginType
        String[] parts = userId.split(" ");
        String user_id = parts[0].trim();  // Added trim() to remove any whitespace
        String loginType = parts.length > 1 ? parts[1].trim() : "DEFAULT";

        // Debug logging
        System.out.println("Parsed userId: '" + user_id + "'");
        System.out.println("Parsed loginType: '" + loginType + "'");

        try {
            if ("TEMP".equalsIgnoreCase(loginType)) {
                // Handle temporary user case
                TempUser tempUser = tempUserRepo.findTempUserByUserId(user_id);

                if (tempUser == null) {
                    System.out.println("No temporary user found with ID: " + user_id);
                    throw new UsernameNotFoundException("Temporary user not found with ID: " + user_id);
                }

                System.out.println("Found temporary user: " + tempUser.getUserId());

                // Create UserEntity from TempUser
                UserEntity userEntity = new UserEntity();
                userEntity.setUserId(tempUser.getUserId() + " " + "TEMP");
                userEntity.setFirstName(tempUser.getFirstName());
                userEntity.setLastName(tempUser.getLastName());
                userEntity.setEmail(tempUser.getEmail());
                userEntity.setEncryptedPassword(tempUser.getPassword());
                userEntity.setEmailVerificationStatus(false);

                // Set roles
                RoleEntity role = UserMapper.mapRoleToRoleEntity(userEntity);
                userEntity.setRoles(List.of(role));

                return new UserPrincipal(userEntity);
            } else {
                // Handle regular user case
                UserEntity userEntity = userRepository.findByUserId(user_id);

                if (userEntity == null) {
                    System.out.println("No user found with ID: " + user_id);
                    throw new UsernameNotFoundException("User not found with ID: " + user_id);
                }

                System.out.println("Found user: " + userEntity.getUserId());
                return new UserPrincipal(userEntity);
            }
        } catch (Exception e) {
            System.err.println("Error fetching user details for ID: " + user_id);
            e.printStackTrace();
            throw e;  // Re-throw the exception after logging
        }
    }

    @Transactional
    public void rebalancePriorities() {
        List<RoleEntity> roles = roleRepository.findAllByOrderByPriorityAsc();

        int newPriority = 10; // Start from first available in admin band
        for (RoleEntity role : roles) {
            // Skip system roles (1-9)
            if (role.getPriority() <= 9) continue;

            role.setPriority(newPriority);
            roleRepository.save(role);
            newPriority += 10; // Leave 9 gaps between roles
        }
    }


    @Override
    public List<String> getAllRoleNames() {
        return StreamSupport.stream(roleRepository.findAll().spliterator(), false)
                .map(RoleEntity::getName) // Use roleEntity.getName() directly
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllSectionNames() {
        return StreamSupport.stream(sectionRepo.findAll().spliterator(), false)
                .map(SectionEntity::getSection) // Use roleEntity.getName() directly
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllProfileNames() {
        return StreamSupport.stream(profilesRepo.findAll().spliterator(), false)
                .map(ProfilesEntity::getName) // Use roleEntity.getName() directly
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserEntity> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
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
        authorityRepo.deleteById(authId);
    }

    @Override
    public void deleteSection(Long sectionId) {
        sectionRepo.deleteById(sectionId);
        Optional<SectionEntity> sectionOpt = sectionRepo.findById(sectionId);
        if (sectionOpt.isPresent()) {
            SectionEntity profile = sectionOpt.get();
            for (UserEntity user : profile.getUsers()) {
                user.getRoles().remove(profile);
                userRepository.save(user);
            }
            sectionRepo.delete(profile);
        } else {
            throw new RuntimeException("Role not found with id: " + sectionId);
        }
    }
}
