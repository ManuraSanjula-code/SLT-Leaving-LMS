package com.slt.peotv.userservice.lms.service.impl;

import com.slt.peotv.userservice.lms.entity.*;
import com.slt.peotv.userservice.lms.entity.company.ProfilesEntity;
import com.slt.peotv.userservice.lms.entity.company.SectionEntity;
import com.slt.peotv.userservice.lms.exceptions.UserServiceException;
import com.slt.peotv.userservice.lms.repository.*;
import com.slt.peotv.userservice.lms.security.UserPrincipal;
import com.slt.peotv.userservice.lms.service.UserService;
import com.slt.peotv.userservice.lms.shared.Messaging.UserEventPublisher;
import com.slt.peotv.userservice.lms.shared.Utils;
import com.slt.peotv.userservice.lms.shared.dto.*;
import com.slt.peotv.userservice.lms.shared.model.request.*;
import com.slt.peotv.userservice.lms.shared.model.response.ErrorMessages;
import com.slt.peotv.userservice.lms.shared.model.response.UserRest;
import com.slt.peotv.userservice.lms.utils.UserMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.modelmapper.ModelMapper;
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
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserServiceImpl implements UserService {

    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/uploads/";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Utils utils;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProfilesRepo profilesRepo;

    @Autowired
    private SectionRepo sectionRepo;

    @Autowired
    private AuthorityRepository authorityRepo;

    @Autowired
    private UserEventPublisher eventPublisher;

    private ImplUtils implUtils = new ImplUtils();

    @Autowired
    private TempUserRepo tempUserRepo;

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public UserDto createUser(UserDto user) throws Exception {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new UserServiceException("Record already exists");
        }

        UserEntity userEntity = UserMapper.mapToUserEntity(user, roleRepository, profilesRepo, sectionRepo, userRepository);
        if (userEntity == null) {
            throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
        }
        String publicUserId = utils.generateUserId(30);
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

        if (user.getOther() != null) {
            String other = user.getOther();
            UserEntity userOther = userRepository.findByEmployeeId(other);
            if (userOther != null) {
                userEntity.setOther(userOther);
            }
        }

        if (user.getAddresses().isEmpty()) {
            List<AddressEntity> addressEntities = new ArrayList<>();
            for (AddressDTO addressDto : user.getAddresses()) {
                AddressEntity addressEntity = UserMapper.mapToAddressEntity(addressDto);
                addressEntity.setUserDetails(userEntity);
                String publicAddressId = utils.generateUserId(30);
                addressEntity.setAddressId(publicAddressId);
                addressEntities.add(addressEntity);
            }
            userEntity.setAddresses(addressEntities); // Set the addresses list
        }


        if (user.getSections().isEmpty()) {
            userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }

        // Handle roles, sections, and profiles
        Collection<SectionEntity> sectionEntities = new HashSet<>();
        Collection<ProfilesEntity> profilesEntities = new HashSet<>();
        Collection<RoleEntity> roleEntities = new HashSet<>();

        if (user.getRoles().isEmpty()) {
            for (String role : user.getRoles()) {
                RoleEntity roleEntity = roleRepository.findByName(role);
                if (roleEntity != null) {
                    roleEntities.add(roleEntity);
                }
            }
        }

        if (user.getSections().isEmpty()) {
            user.getSections().forEach(sect -> {
                SectionEntity sec = sectionRepo.findBySection(sect);
                if (sec != null) {
                    sectionEntities.add(sec);
                }
            });
        }

        if (user.getProfiles().isEmpty()) {
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

        if (storedUserDetails.getSections().isEmpty()) {
            storedUserDetails.getSections().forEach(sec -> {
                sec.getUsers().add(storedUserDetails);
                sectionRepo.save(sec);
            });
        }

        if (storedUserDetails.getProfiles().isEmpty()) {
            storedUserDetails.getProfiles().forEach(pro -> {
                pro.getUsers().add(storedUserDetails);
                profilesRepo.save(pro);
            });
        }

        if (storedUserDetails.getRoles().isEmpty()) {
            storedUserDetails.getRoles().forEach(role -> {
                role.getUsers().add(storedUserDetails);
                roleRepository.save(role);
            });
        }

        TempUser tempUser = new TempUser();
        tempUser.setUserId(storedUserDetails.getUserId());
        tempUser.setPassword(user.getPassword());
        tempUser.setNew(true);
        tempUser.setEmail(user.getEmail());
        tempUser.setFirstName(storedUserDetails.getFirstName());
        tempUser.setLastName(storedUserDetails.getLastName());
        tempUser.setPeoTvId(storedUserDetails.getEmployeeId());
        tempUser.setPasswordEn(false);
        tempUserRepo.save(tempUser);

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

        String fileName = utils.generateUserId(10) + file.getOriginalFilename();
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
            return new UserPrincipal(userEntity);
        }else{

            UserEntity userEntity = userRepository.findByEmail(email);

            if (userEntity == null) {
                throw new UsernameNotFoundException(email);
            }
            TempUser tempUser = tempUserRepo.findTempUserByUserId(userEntity.getUserId());
            if (tempUser != null && tempUser.isNew()) {
                tempUser.setNew(false);
                tempUser.setPassword("404");
                tempUserRepo.save(tempUser);
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
        userRest.setRoles(Arrays.asList("ROLE_ADMIN", "ROLE_USER", "ROLE_EMPLOYEE"));
        return userRest;
    }

    @Override
    public UserDto updateUser(String userId, UserDto userDto) throws Exception {
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
        // Handle addresses
        List<AddressDTO> address_dto = userDto.getAddresses();
        List<AddressEntity> addresses = userEntity.getAddresses();

        if (addresses.isEmpty()) {

            if (address_dto.isEmpty()) {

                addresses.forEach(address -> {
                    if (address != null) {
                        address_dto.forEach(dto -> {
                            if (dto != null) {
                                if (!AddressComparator.areEqual(dto, address)) {
                                    AddressEntity addressEntity = UserMapper.mapToAddressEntity(dto);
                                    addressEntity.setUserDetails(userEntity); // Associate with UserEntity
                                    String publicAddressId = utils.generateUserId(30);
                                    addressEntity.setAddressId(publicAddressId);
                                    addresses.add(address);
                                }
                            }
                        });
                    }
                });

            }

        }
        userEntity.setAddresses(addresses);

        Collection<RoleEntity> role_user = userEntity.getRoles();
        Collection<ProfilesEntity> profile_user = userEntity.getProfiles();
        Collection<SectionEntity> section_user = userEntity.getSections();

        if (!role_user.isEmpty() || !userDto.getRoles().isEmpty()) {
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
        if (!userDto.getSections().isEmpty() || !section_user.isEmpty()) {
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
        if (!userDto.getProfiles().isEmpty() || !profile_user.isEmpty()) {
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
        List<AddressEntity> updatedAddresses = new ArrayList<>();

        for (AddressDTO addressDto : addressDtos) {
            AddressEntity addressEntity;

            if (addressDto.getAddressId() == null || addressDto.getAddressId().isEmpty()) {
                // Create a new address
                addressEntity = new AddressEntity();
                addressEntity.setAddressId(UUID.randomUUID().toString()); // Generate a unique ID
                addressEntity.setUserDetails(userEntity);
            } else {
                // Try to find an existing address
                addressEntity = userEntity.getAddresses().stream()
                        .filter(addr -> addr.getAddressId().equals(addressDto.getAddressId())).findFirst().orElse(null);

                if (addressEntity == null) {
                    // If the address is not found, treat it as a new address
                    addressEntity = new AddressEntity();
                    addressEntity.setAddressId(utils.generateAddressId(10)); // Generate a new unique ID
                    addressEntity.setUserDetails(userEntity);
                }
            }

            // Update address details
            addressEntity.setCity(addressDto.getCity());
            addressEntity.setCountry(addressDto.getCountry());
            addressEntity.setStreetName(addressDto.getStreetName());
            addressEntity.setPostalCode(addressDto.getPostalCode());

            // Handle default address logic
            if (Boolean.TRUE.equals(addressDto.getIsDefault())) {
                // Set all other addresses to non-default
                userEntity.getAddresses().forEach(addr -> addr.setIsDefault(false));
                addressEntity.setIsDefault(true);
            } else {
                addressEntity.setIsDefault(false);
            }

            updatedAddresses.add(addressEntity);
        }

        return updatedAddresses;
    }

    private List<AddressEntity> handleAddresses_(UserEntity userEntity, List<AddressDTO> addressDtos) {
        List<AddressEntity> updatedAddresses = new ArrayList<>();

        for (AddressDTO addressDto : addressDtos) {
            AddressEntity addressEntity;

            if (addressDto.getAddressId() == null || addressDto.getAddressId().isEmpty()) {
                // Create a new address
                addressEntity = new AddressEntity();
                addressEntity.setUserDetails(userEntity);
            } else {
                // Find and update an existing address
                addressEntity = userEntity.getAddresses().stream()
                        .filter(addr -> addr.getAddressId().equals(addressDto.getAddressId())).findFirst()
                        .orElseThrow(() -> new UserServiceException("Address not found"));
            }

            // Update address details
            addressEntity.setCity(addressDto.getCity());
            addressEntity.setCountry(addressDto.getCountry());
            addressEntity.setStreetName(addressDto.getStreetName());
            addressEntity.setPostalCode(addressDto.getPostalCode());

            // Handle default address logic
            if (Boolean.TRUE.equals(addressDto.getIsDefault())) {
                // Set all other addresses to non-default
                userEntity.getAddresses().forEach(addr -> addr.setIsDefault(false));
                addressEntity.setIsDefault(true);
            } else {
                addressEntity.setIsDefault(false);
            }

            updatedAddresses.add(addressEntity);
        }

        return updatedAddresses;
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
            boolean hastokenExpired = Utils.hasTokenExpired(token);
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

        String token = new Utils().generatePasswordResetToken(userEntity.getUserId());

        PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
        passwordResetTokenEntity.setToken(token);
        passwordResetTokenEntity.setUserDetails(userEntity);
        passwordResetTokenRepository.save(passwordResetTokenEntity);

        return !returnValue;
    }

    @Override
    public boolean resetPassword(String token, String password) {
        boolean returnValue = false;

        if (Utils.hasTokenExpired(token)) {
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
    public ProfilesEntity createProfiles(String name, ProfileReq req) {
        return null;
    }

    @Override
    public SectionEntity createSection(String name) {
        return null;
    }

    @Override
    public SectionEntity getSection(String name) {
        return null;
    }

    @Override
    public ProfilesEntity getProfiles(String name) {
        return null;
    }

    @Override
    public List<UserDtoArchive> findByRoleName(String roleName) {
        List<UserDtoArchive> returnValue = new ArrayList<>();

        userRepository.findByRoleName(roleName.toUpperCase()).forEach(user -> {
            if(user == null)
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

    public List<RoleDTO> getRole_() {
        List<RoleEntity> roleEntities = (List<RoleEntity>) roleRepository.findAll();

        List<RoleDTO> roles = roleEntities.stream()
                .map(roleEntity -> {
                    RoleDTO roleDTO = UserMapper.mapToRoleDTO(roleEntity);
                    return roleDTO;
                })
                .collect(Collectors.toList());

        return roles;
    }

    @Override
    public List<RoleDTO> getRole() {
        return ((List<RoleEntity>) roleRepository.findAll()).stream()
                .map(UserMapper::mapToRoleDTO)
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
    public ProfilesDTO saveProfile(ProfilesDTO profilesDTO) {
        return null;
    }

    @Override
    public SectionDTO saveSection(SectionReq req) {
        SectionEntity sectionEntity = sectionRepo.findBySection(req.getSection());

        if (sectionEntity != null) {
            synchronized (sectionEntity) {
                removeDeletedUsers(req, sectionEntity);
            }
        } else {
            sectionEntity = createNewSectionEntity(req);
        }

        if (req.getSection() != null && !req.getSection().isEmpty()) {
            sectionEntity.setSection(req.getSection());
        }

        synchronized (sectionRepo) {
            return UserMapper.mapToSectionDTO(sectionRepo.save(sectionEntity));
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
        sectionEntity.setPublicId(utils.generateUserId(10));
        List<UserEntity> userEntities = new CopyOnWriteArrayList<>();

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

    public ProfilesDTO saveProfile_(ProfileReq req) {
        ProfilesEntity profilesEntity = mapProfileReqToProfilesEntity(req);
        profilesEntity.setPublicId(utils.generateUserId(10));

        List<UserEntity> userEntities = new CopyOnWriteArrayList<>();

        // Handle added users
        if (req.getAddedUsers() != null && !req.getAddedUsers().isEmpty()) {
            addUsersToProfile(req.getAddedUsers(), userEntities);
        }

        // Handle deleted users
        if (req.getDeletedUsers() != null && !req.getDeletedUsers().isEmpty()) {
            removeUsersFromProfile(req.getDeletedUsers(), profilesEntity);
        }

        if (req.getName() != null && !req.getName().isEmpty()) {
            profilesEntity.setName(req.getName());
        }

        // Save the profile entity
        synchronized (profilesRepo) {
            return UserMapper.mapToProfilesDTO(profilesRepo.save(profilesEntity));
        }
    }

    @Override
    public ProfilesDTO saveProfile(ProfileReq req) {
        ProfilesEntity profilesEntity;

        if (req.getPublicId() != null && !req.getPublicId().isEmpty()) {
            // Fetch the existing profile by publicId
            profilesEntity = profilesRepo.findByPublicId(req.getPublicId());

            if(profilesEntity == null) {
               throw new NoSuchElementException("Profile not found with publicId: " + req.getPublicId());
            }

            // Update the existing profile entity with new data
            if (req.getName() != null && !req.getName().isEmpty()) {
                profilesEntity.setName(req.getName());
            }
        } else {
            // Create a new profile entity (create scenario)
            profilesEntity = mapProfileReqToProfilesEntity(req);
            profilesEntity.setPublicId(utils.generateUserId(10));
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
    public AuthorityDTO saveAuthority(AuthorityDTO authorityDTO) {
        return null;
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
        }else {
            throw new RuntimeException("Role not found with id: " + profileId);
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
        if(authorityRepo.findByName(name) == null) {
            return false;
        }else
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
        }else {
            throw new RuntimeException("Role not found with id: " + sectionId);
        }
    }

    public class AddressComparator {
        public static boolean areEqual(AddressDTO dto, AddressEntity entity) {
            if (dto == null && entity == null) {
                return true;
            }
            if (dto == null || entity == null) {
                return false;
            }

            return new EqualsBuilder().append(dto.getId(), entity.getId())
                    .append(dto.getAddressId(), entity.getAddressId()).append(dto.getCity(), entity.getCity())
                    .append(dto.getCountry(), entity.getCountry()).append(dto.getStreetName(), entity.getStreetName())
                    .append(dto.getPostalCode(), entity.getPostalCode())
                    .append(dto.getIsDefault(), entity.getIsDefault())
                    .append(dto.getUserDetails() != null ? dto.getUserDetails().getId() : null,
                            entity.getUserDetails() != null ? entity.getUserDetails().getId() : null)
                    .isEquals();
        }
    }

    public class ImplUtils {
        @Transactional
        public RoleEntity updateRole(RoleReq req, RoleEntity roleEntity) {
            // Remove deleted authorities
            req.getDeletedAuthorities().forEach(authorityName -> {
                AuthorityEntity auth = authorityRepo.findByName(authorityName);
                if (auth != null) {
                    boolean removed = roleEntity.getAuthorities().removeIf(authority -> authority.getName().equals(authorityName));
                    System.out.println("Authority removed: " + removed);
                }
            });

            // Remove deleted users
            req.getDeletedUsers().forEach(userId -> {
                boolean removed = roleEntity.getUsers().removeIf(user -> user.getUserId().equals(userId));
                if (removed) {
                    UserEntity userEntity = userRepository.findByUserId(userId);
                    if (userEntity != null) {
                        userEntity.getRoles().removeIf(role -> role.getName().equals(roleEntity.getName()));
                        userRepository.save(userEntity); // Update the user entity
                        roleRepository.save(roleEntity);
                    }
                }
                System.out.println("User removed: " + removed);
            });

            // Add new users
            req.getAddedUsers().forEach(userId -> {
                UserEntity userEntity = userRepository.findByUserId(userId);
                if (userEntity != null && !roleEntity.getUsers().contains(userEntity)) {
                    roleEntity.getUsers().add(userEntity);
                    userEntity.getRoles().add(roleEntity);
                    userRepository.save(userEntity); // Update the user entity
                }
            });

            // Add new authorities
            req.getAddedAuthorities().forEach(authorityName -> {
                AuthorityEntity authorityEntity = authorityRepo.findByName(authorityName);
                if (authorityEntity != null && !roleEntity.getAuthorities().contains(authorityEntity)) {
                    roleEntity.getAuthorities().add(authorityEntity);
                }
            });

            // Update role name if provided
            if (req.getName() != null && !req.getName().isEmpty()) {
                roleEntity.setName(req.getName());
            }

            return roleRepository.save(roleEntity); // Save the updated role entity
        }

        public RoleEntity saveRole_(RoleReq req) {
            // Create a new RoleEntity (local variable, no shared state)
            RoleEntity roleEntity = new RoleEntity();

            // Use thread-safe collections for users and authorities
            List<UserEntity> userEntities = Collections.synchronizedList(new ArrayList<>());
            List<AuthorityEntity> authorityEntities = Collections.synchronizedList(new ArrayList<>());

            // Safely process added users
            if (req.getAddedUsers() != null && !req.getAddedUsers().isEmpty()) {
                req.getAddedUsers().forEach(userId -> {
                    // Synchronize access to userRepository if it's not thread-safe
                    synchronized (userRepository) {
                        UserEntity user = userRepository.findByUserId(userId);
                        if (user != null) {
                            userEntities.add(user);
                        }
                    }
                });
            }

            // Safely process added authorities
            if (req.getAddedAuthorities() != null && !req.getAddedAuthorities().isEmpty()) {
                req.getAddedAuthorities().forEach(authority -> {
                    // Synchronize access to authorityRepo if it's not thread-safe
                    synchronized (authorityRepo) {
                        AuthorityEntity authorityEntity = authorityRepo.findByName(authority);
                        if (authorityEntity != null) {
                            authorityEntities.add(authorityEntity);
                        }
                    }
                });
            }

            // Set the role name (ensure null safety)
            if (req.getName() != null && !req.getName().isEmpty()) {
                roleEntity.setName(req.getName());
            }

            // Set users and authorities (use immutable collections to prevent further modifications)
            roleEntity.setUsers(Collections.unmodifiableList(userEntities));
            roleEntity.setAuthorities(Collections.unmodifiableList(authorityEntities));

            return roleEntity;
        }


    }
}
