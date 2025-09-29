package com.slt.peotv.userservice.lms.utils;

import com.slt.peotv.userservice.lms.entity.AddressEntity;
import com.slt.peotv.userservice.lms.entity.RoleEntity;
import com.slt.peotv.userservice.lms.entity.UserEntity;
import com.slt.peotv.userservice.lms.entity.company.ProfilesEntity;
import com.slt.peotv.userservice.lms.entity.company.SectionEntity;
import com.slt.peotv.userservice.lms.exceptions.UserServiceException;
import com.slt.peotv.userservice.lms.message.LMSUser;
import com.slt.peotv.userservice.lms.message.MessageProducerService;
import com.slt.peotv.userservice.lms.redis.RedisService;
import com.slt.peotv.userservice.lms.repository.ProfilesRepo;
import com.slt.peotv.userservice.lms.repository.RoleRepository;
import com.slt.peotv.userservice.lms.repository.SectionRepo;
import com.slt.peotv.userservice.lms.repository.UserRepository;
import com.slt.peotv.userservice.lms.shared.dto.AddressDTO;
import com.slt.peotv.userservice.lms.shared.dto.UserDto;
import com.slt.peotv.userservice.lms.shared.model.request.UserReq;
import com.slt.peotv.userservice.lms.shared.model.response.ErrorMessages;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

@Service
public class UpdateUtils {
    private static final Logger logger = LoggerFactory.getLogger(UpdateUtils.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SectionRepo sectionRepo;

    @Autowired
    private ProfilesRepo profilesRepo;

    @Autowired
    private IdUtils idUtils;

    @Autowired
    private MessageProducerService messageProducerService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public UserDto updateUser(String userId, UserReq userReq) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        updateFieldIfNotNull(userReq.getFirstName(), userEntity::setFirstName);
        updateFieldIfNotNull(userReq.getLastName(), userEntity::setLastName);
        updateFieldIfNotNull(userReq.getEmail(), userEntity::setEmail);
        updateFieldIfNotNull(userReq.getPhone(), userEntity::setPhone);
        updateFieldIfNotNull(userReq.getGender(), userEntity::setGender);
        updateFieldIfNotNull(userReq.getIsSltEmp(), userEntity::setIsSltEmp);
        updateFieldIfNotNull(userReq.getIsSltIntern(), userEntity::setIsSltIntern);
        updateFieldIfNotNull(userReq.getSltId(), userEntity::setSltId);
        updateFieldIfNotNull(userReq.getEmployeeId(), userEntity::setEmployeeId);
        updateFieldIfNotNull(userReq.getActive(), userEntity::setActive);
        updateFieldIfNotNull(userReq.getJoiningDate(), userEntity::setJoinDate);
        updateFieldIfNotNull(userReq.getRoaster(), userEntity::setRoaster);

        if (userReq.getPassword() != null && !userReq.getPassword().trim().isEmpty()) {
            updateFieldIfNotNull(bCryptPasswordEncoder.encode(userReq.getPassword()), userEntity::setEncryptedPassword);
        }

        updateAddresses(userReq.getAddresses(), userEntity, userReq);

        updateCollectionField(userReq.getRoles(), roleRepository::findByName, userEntity.getRoles(), userEntity::setRoles);
        updateCollectionField(userReq.getSections(), sectionRepo::findBySection, userEntity.getSections(), userEntity::setSections);
        updateCollectionField(userReq.getProfiles(), profilesRepo::findByName, userEntity.getProfiles(), userEntity::setProfiles);

        UserEntity updatedUserEntity = userRepository.save(userEntity);

        logFieldIfNotNull("Email", updatedUserEntity.getEmail(), Object::toString);

        updateRelationships(updatedUserEntity);
        updateAdditionalRelationships(updatedUserEntity, userReq);
        handleAdminUpdates(updatedUserEntity, userReq);

        LMSUser lmsUser = new LMSUser();
        lmsUser.setEmail(updatedUserEntity.getEmail());
        lmsUser.setFirstName(updatedUserEntity.getFirstName());
        lmsUser.setLastName(updatedUserEntity.getLastName());
        lmsUser.setEmployeeId(updatedUserEntity.getEmployeeId());
        lmsUser.setSltId(updatedUserEntity.getSltId());
        lmsUser.setPublicId(updatedUserEntity.getUserId());
        lmsUser.setJoin_date(updatedUserEntity.getJoinDate());
        lmsUser.setRoaster(updatedUserEntity.getRoaster());
        lmsUser.setGender(updatedUserEntity.getGender());

        messageProducerService.sendMessage("user.queue", lmsUser);
        messageProducerService.sendMessage("user.queue.roster", lmsUser);

        if (userReq.getPassword() != null && !userReq.getPassword().trim().isEmpty()) {
            redisService.setValue(userEntity.getEmployeeId(), userReq.getPassword());
        }

        return UserMapper.mapToUserDto(updatedUserEntity);
    }

    private <T> void updateFieldIfNotNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private <T, R> void updateReferenceField(T value, Function<T, R> finder, java.util.function.Consumer<R> setter) {
        if (value != null) {
            R reference = finder.apply(value);
            if (reference != null) {
                setter.accept(reference);
            } else {
                logger.warn("Reference not found for value: {}", value);
            }
        }
    }

    @Transactional
    public synchronized void handleAdminUpdates(UserEntity currentUser, UserReq userReq) throws UserServiceException {
        if ((userReq.getAddedAdmins() == null || userReq.getAddedAdmins().isEmpty()) &&
                (userReq.getDeletedAdmins() == null || userReq.getDeletedAdmins().isEmpty())) {
            currentUser.setMyAdmins(Collections.emptyList());
        }

        currentUser = userRepository.findByUserId(currentUser.getUserId());

        if (userReq.getAddedAdmins() != null && !userReq.getAddedAdmins().isEmpty()) {
            for (String adminUserId : userReq.getAddedAdmins()) {
                UserEntity adminUser = userRepository.findByUserId(adminUserId);
                if (adminUser == null) {
                    return;
                }

                boolean adminAlreadyAssigned = false;
                for (UserEntity existingAdmin : currentUser.getMyAdmins()) {
                    if (existingAdmin.getUserId().equals(adminUserId)) {
                        adminAlreadyAssigned = true;
                        break;
                    }
                }

                if (!adminAlreadyAssigned) {
                    currentUser.addAdmin(adminUser);
                }
            }
        }

        if (userReq.getDeletedAdmins() != null && !userReq.getDeletedAdmins().isEmpty()) {
            List<UserEntity> adminsToRemove = new ArrayList<>();

            for (UserEntity admin : new ArrayList<>(currentUser.getMyAdmins())) {
                if (userReq.getDeletedAdmins().contains(admin.getUserId())) {
                    adminsToRemove.add(admin);
                }
            }

            for (UserEntity adminToRemove : adminsToRemove) {
                currentUser.removeAdmin(adminToRemove);
            }
        }

        userRepository.save(currentUser);
    }

    private void updateAddresses(List<AddressDTO> newAddressDtos, UserEntity userEntity, UserReq userReq) {
        if (newAddressDtos == null) return;

        List<AddressEntity> existingAddresses = userEntity.getAddresses();

        if (userReq != null && userReq.getDeleteAddresses() != null && !userReq.getDeleteAddresses().isEmpty()) {
            existingAddresses.removeIf(addr ->
                    userReq.getDeleteAddresses().contains(addr.getAddressId())
            );
        }

        for (AddressDTO dto : newAddressDtos) {
            AddressEntity existingAddress = null;

            if (dto.getAddressId() != null) {
                existingAddress = existingAddresses.stream()
                        .filter(addr -> addr.getAddressId().equals(dto.getAddressId()))
                        .findFirst()
                        .orElse(null);
            }

            if (existingAddress != null) {
                updateExistingAddress(existingAddress, dto);
                existingAddress.setDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
            } else {
                AddressEntity newAddress = UserMapper.mapToAddressEntity(dto);
                newAddress.setUserDetails(userEntity);
                newAddress.setAddressId(idUtils.generateId(30));
                newAddress.setDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
                existingAddresses.add(newAddress);
            }
        }

        long defaultCount = existingAddresses.stream().filter(AddressEntity::isDefault).count();
        if (defaultCount == 0 && !existingAddresses.isEmpty()) {
            existingAddresses.get(0).setDefault(true);
        } else if (defaultCount > 1) {
            boolean firstDefaultFound = false;
            for (AddressEntity addr : existingAddresses) {
                if (addr.isDefault()) {
                    if (!firstDefaultFound) {
                        firstDefaultFound = true;
                    } else {
                        addr.setDefault(false);
                    }
                }
            }
        }
    }

    private AddressEntity findAddressByContent(AddressDTO dto, List<AddressEntity> existingAddresses) {
        return existingAddresses.stream()
                .filter(addr ->
                        addr.getCity().equals(dto.getCity()) &&
                                addr.getStreetName().equals(dto.getStreetName()) &&
                                addr.getPostalCode().equals(dto.getPostalCode()))
                .findFirst()
                .orElse(null);
    }

    private AddressEntity findExistingDefault(AddressDTO dto, List<AddressEntity> existingAddresses) {
        if (dto.getAddressId() != null) {
            return existingAddresses.stream()
                    .filter(addr -> dto.getAddressId().equals(addr.getAddressId()))
                    .findFirst()
                    .orElse(null);
        }

        return findAddressByContent(dto, existingAddresses);
    }

    private void updateExistingAddress(AddressEntity existing, AddressDTO dto) {
        existing.setCity(dto.getCity());
        existing.setCountry(dto.getCountry());
        existing.setStreetName(dto.getStreetName());
        existing.setPostalCode(dto.getPostalCode());
    }

    private <T, R> void updateCollectionField(Collection<T> values, Function<T, R> finder, Collection<R> existingCollection, java.util.function.Consumer<Collection<R>> setter) {
        if (values != null && !values.isEmpty()) {
            values.forEach(value -> {
                R entity = finder.apply(value);
                if (entity != null && !existingCollection.contains(entity)) {
                    existingCollection.add(entity);
                }
            });
            setter.accept(existingCollection);
        }
    }

    private void logFieldIfNotNull(String fieldName, Object fieldValue, Function<Object, String> valueExtractor) {
        if (fieldValue != null) {
            logger.info("{}: {}", fieldName, valueExtractor.apply(fieldValue));
        } else {
            logger.info("{} is null", fieldName);
        }
    }

    private void updateRelationships(UserEntity userEntity) {
        userEntity.getSections().forEach(section -> {
            if (!section.getUsers().contains(userEntity)) {
                section.getUsers().add(userEntity);
                sectionRepo.save(section);
            }
        });

        userEntity.getProfiles().forEach(profile -> {
            if (!profile.getUsers().contains(userEntity)) {
                profile.getUsers().add(userEntity);
                profilesRepo.save(profile);
            }
        });

        userEntity.getRoles().forEach(role -> {
            if (!role.getUsers().contains(userEntity)) {
                role.getUsers().add(userEntity);
                roleRepository.save(role);
            }
        });
    }

    private synchronized void updateAdditionalRelationships(UserEntity userEntity, UserReq userReq) {
        if (userReq.getAdditional() != null) {
            logger.info("Processing additional relationships for user: {}", userEntity.getUserId());

            UserEntity refreshedUser = userRepository.findById(userEntity.getId())
                    .orElseThrow(() -> new UserServiceException("User not found during update"));

            processAdditions(refreshedUser, userReq.getAdditional());

            processDeletions(refreshedUser, userReq.getAdditional());

            userRepository.save(refreshedUser);
            logger.info("Successfully updated additional relationships for user: {}", refreshedUser.getUserId());
        }
    }

    private synchronized void processAdditions(UserEntity userEntity, UserReq.Additional additional) {
        if (additional.getAddedRoles() != null && !additional.getAddedRoles().isEmpty()) {
            logger.info("Adding roles: {}", additional.getAddedRoles());
            addRolesByName(userEntity, additional.getAddedRoles());
        }
        if (additional.getAddedSelections() != null && !additional.getAddedSelections().isEmpty()) {
            logger.info("Adding sections: {}", additional.getAddedSelections());
            addSectionsByName(userEntity, additional.getAddedSelections());
        }
        if (additional.getAddedProfiles() != null && !additional.getAddedProfiles().isEmpty()) {
            logger.info("Adding profiles: {}", additional.getAddedProfiles());
            addProfilesByName(userEntity, additional.getAddedProfiles());
        }
    }

    private synchronized void processDeletions(UserEntity userEntity, UserReq.Additional additional) {
        if (additional.getDeleteRoles() != null && !additional.getDeleteRoles().isEmpty()) {
            logger.info("Removing roles: {}", additional.getDeleteRoles());
            removeRolesByName(userEntity, additional.getDeleteRoles());
        }
        if (additional.getDeleteSelections() != null && !additional.getDeleteSelections().isEmpty()) {
            logger.info("Removing sections: {}", additional.getDeleteSelections());
            removeSectionsByName(userEntity, additional.getDeleteSelections());
        }
        if (additional.getDeleteProfiles() != null && !additional.getDeleteProfiles().isEmpty()) {
            logger.info("Removing profiles: {}", additional.getDeleteProfiles());
            removeProfilesByName(userEntity, additional.getDeleteProfiles());
        }
    }

    // Role operations
    private synchronized void addRolesByName(UserEntity userEntity, List<String> roleNames) {
        List<RoleEntity> currentRoles = new ArrayList<>(userEntity.getRoles());

        roleNames.forEach(roleName -> {
            if (currentRoles.stream().noneMatch(r -> r.getName().equals(roleName))) {
                RoleEntity role = roleRepository.findByName(roleName);
                if (role != null) {
                    currentRoles.add(role);
                    if (!role.getUsers().contains(userEntity)) {
                        role.getUsers().add(userEntity);
                        roleRepository.save(role);
                    }
                } else {
                    logger.warn("Role not found: {}", roleName);
                }
            }
        });

        userEntity.setRoles(currentRoles);
    }

    private synchronized void removeRolesByName(UserEntity userEntity, List<String> roleNames) {
        List<RoleEntity> currentRoles = new ArrayList<>(userEntity.getRoles());
        List<RoleEntity> rolesToRemove = new ArrayList<>();

        currentRoles.forEach(role -> {
            if (roleNames.contains(role.getName())) {
                rolesToRemove.add(role);
                role.getUsers().removeIf(u -> u.getUserId().equals(userEntity.getUserId()));
                roleRepository.save(role);
            }
        });

        currentRoles.removeAll(rolesToRemove);
        userEntity.setRoles(currentRoles);
    }

    private synchronized void addSectionsByName(UserEntity userEntity, List<String> sectionNames) {
        List<SectionEntity> currentSections = new ArrayList<>(userEntity.getSections());

        sectionNames.forEach(sectionName -> {
            if (currentSections.stream().noneMatch(s -> s.getSection().equals(sectionName))) {
                SectionEntity section = sectionRepo.findBySection(sectionName);
                if (section != null) {
                    currentSections.add(section);
                    if (!section.getUsers().contains(userEntity)) {
                        section.getUsers().add(userEntity);
                        sectionRepo.save(section);
                    }
                } else {
                    logger.warn("Section not found: {}", sectionName);
                }
            }
        });

        userEntity.setSections(currentSections);
    }

    private synchronized void removeSectionsByName(UserEntity userEntity, List<String> sectionNames) {
        List<SectionEntity> currentSections = new ArrayList<>(userEntity.getSections());
        List<SectionEntity> sectionsToRemove = new ArrayList<>();

        currentSections.forEach(section -> {
            if (sectionNames.contains(section.getSection())) {
                sectionsToRemove.add(section);
                section.getUsers().removeIf(u -> u.getUserId().equals(userEntity.getUserId()));
                sectionRepo.save(section);
            }
        });

        currentSections.removeAll(sectionsToRemove);
        userEntity.setSections(currentSections);
    }

    private synchronized void addProfilesByName(UserEntity userEntity, List<String> profileNames) {
        List<ProfilesEntity> currentProfiles = new ArrayList<>(userEntity.getProfiles());

        profileNames.forEach(profileName -> {
            if (currentProfiles.stream().noneMatch(p -> p.getName().equals(profileName))) {
                ProfilesEntity profile = profilesRepo.findByName(profileName);
                if (profile != null) {
                    currentProfiles.add(profile);
                    if (!profile.getUsers().contains(userEntity)) {
                        profile.getUsers().add(userEntity);
                        profilesRepo.save(profile);
                    }
                } else {
                    logger.warn("Profile not found: {}", profileName);
                }
            }
        });

        userEntity.setProfiles(currentProfiles);
    }

    private synchronized void removeProfilesByName(UserEntity userEntity, List<String> profileNames) {
        List<ProfilesEntity> currentProfiles = new ArrayList<>(userEntity.getProfiles());
        List<ProfilesEntity> profilesToRemove = new ArrayList<>();

        currentProfiles.forEach(profile -> {
            if (profileNames.contains(profile.getName())) {
                profilesToRemove.add(profile);
                profile.getUsers().removeIf(u -> u.getUserId().equals(userEntity.getUserId()));
                profilesRepo.save(profile);
            }
        });

        currentProfiles.removeAll(profilesToRemove);
        userEntity.setProfiles(currentProfiles);
    }
}