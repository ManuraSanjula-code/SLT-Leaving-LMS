package com.slt.peotv.userservice.lms.utils;

import com.slt.peotv.userservice.lms.entity.AddressEntity;
import com.slt.peotv.userservice.lms.entity.RoleEntity;
import com.slt.peotv.userservice.lms.entity.UserEntity;
import com.slt.peotv.userservice.lms.entity.company.ProfilesEntity;
import com.slt.peotv.userservice.lms.entity.company.SectionEntity;
import com.slt.peotv.userservice.lms.exceptions.UserServiceException;
import com.slt.peotv.userservice.lms.repository.ProfilesRepo;
import com.slt.peotv.userservice.lms.repository.RoleRepository;
import com.slt.peotv.userservice.lms.repository.SectionRepo;
import com.slt.peotv.userservice.lms.repository.UserRepository;
import com.slt.peotv.userservice.lms.shared.dto.AddressDTO;
import com.slt.peotv.userservice.lms.shared.dto.UserDto;
import com.slt.peotv.userservice.lms.shared.model.request.UserReq;
import com.slt.peotv.userservice.lms.shared.model.response.ErrorMessages;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    @Deprecated
    public UserDto updateUser(String userId, UserDto userDto) {
        // Find the user entity by userId
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        // Update basic user details
        updateFieldIfNotNull(userDto.getFirstName(), userEntity::setFirstName);
        updateFieldIfNotNull(userDto.getLastName(), userEntity::setLastName);
        updateFieldIfNotNull(userDto.getEmail(), userEntity::setEmail);
        updateFieldIfNotNull(userDto.getPhone(), userEntity::setPhone);
        updateFieldIfNotNull(userDto.getGender(), userEntity::setGender);
        updateFieldIfNotNull(userDto.getIsSltEmp(), userEntity::setIsSltEmp);
        updateFieldIfNotNull(userDto.getIsSltIntern(), userEntity::setIsSltIntern);
        updateFieldIfNotNull(userDto.getSltId(), userEntity::setSltId);
        updateFieldIfNotNull(userDto.getEmployeeId(), userEntity::setEmployeeId);
        updateFieldIfNotNull(userDto.getActive(), userEntity::setActive);
        updateFieldIfNotNull(userDto.getJoiningDate(), userEntity::setJoin_date);

        // Update HOD, Supervisor, and Other fields
        updateReferenceField(userDto.getHod(), userRepository::findByEmployeeId, userEntity::setHod);
        updateReferenceField(userDto.getSupervisor(), userRepository::findByEmployeeId, userEntity::setSupervisor);
        updateReferenceField(userDto.getOther(), userRepository::findByEmployeeId, userEntity::setOther);

        // Handle addresses
        updateAddresses(userDto.getAddresses(), userEntity, null);

        // Update roles, sections, and profiles
        updateCollectionField(userDto.getRoles(), roleRepository::findByName, userEntity.getRoles(), userEntity::setRoles);
        updateCollectionField(userDto.getSections(), sectionRepo::findBySection, userEntity.getSections(), userEntity::setSections);
        updateCollectionField(userDto.getProfiles(), profilesRepo::findByName, userEntity.getProfiles(), userEntity::setProfiles);

        // Save the updated user entity
        UserEntity updatedUserEntity = userRepository.save(userEntity);

        // Log updated fields
        logFieldIfNotNull("Email", updatedUserEntity.getEmail(), Object::toString);
        logFieldIfNotNull("Other", updatedUserEntity.getOther(), other -> ((UserEntity) other).getEmail());

        // Update relationships in roles, sections, and profiles
        updateRelationships(updatedUserEntity);

        // Return the updated user as a DTO
        return UserMapper.mapToUserDto(updatedUserEntity);
    }

    @Transactional
    public UserDto updateUser(String userId, UserReq userReq) {
        // Find the user entity by userId
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        // Update basic user details
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
        updateFieldIfNotNull(userReq.getJoiningDate(), userEntity::setJoin_date);

        // Update HOD, Supervisor, and Other fields
        updateReferenceField(userReq.getHod(), userRepository::findByEmployeeId, userEntity::setHod);
        updateReferenceField(userReq.getSupervisor(), userRepository::findByEmployeeId, userEntity::setSupervisor);
        updateReferenceField(userReq.getOther(), userRepository::findByEmployeeId, userEntity::setOther);

        // Handle addresses
        updateAddresses(userReq.getAddresses(), userEntity, userReq);

        // Update roles, sections, and profiles
        updateCollectionField(userReq.getRoles(), roleRepository::findByName, userEntity.getRoles(), userEntity::setRoles);
        updateCollectionField(userReq.getSections(), sectionRepo::findBySection, userEntity.getSections(), userEntity::setSections);
        updateCollectionField(userReq.getProfiles(), profilesRepo::findByName, userEntity.getProfiles(), userEntity::setProfiles);

        // Save the updated user entity
        UserEntity updatedUserEntity = userRepository.save(userEntity);

        // Log updated fields
        logFieldIfNotNull("Email", updatedUserEntity.getEmail(), Object::toString);
        logFieldIfNotNull("Other", updatedUserEntity.getOther(), other -> ((UserEntity) other).getEmail());

        // Update relationships in roles, sections, and profiles
        updateRelationships(updatedUserEntity);
        updateAdditionalRelationships(updatedUserEntity, userReq);
        handleAdminUpdates(updatedUserEntity, userReq);
        // Return the updated user as a DTO
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
        // Check if both lists are null/empty
        if ((userReq.getAddedAdmins() == null || userReq.getAddedAdmins().isEmpty()) &&
                (userReq.getDeletedAdmins() == null || userReq.getDeletedAdmins().isEmpty())) {
            return;
        }

        // Handle added admins
        if (userReq.getAddedAdmins() != null && !userReq.getAddedAdmins().isEmpty()) {
            for (String adminUserId : userReq.getAddedAdmins()) {
                // Find the admin user in DB
                UserEntity adminUser = userRepository.findByUserId(adminUserId);
                if (adminUser == null) {
                    throw new UserServiceException("Admin user with ID " + adminUserId + " not found");
                }

                // Check if user is already in administratives (thread-safe check)
                synchronized (currentUser) {
                    List<UserEntity> administratives = currentUser.getAdministratives();
                    if (administratives != null && !administratives.contains(adminUser)) {
                        administratives.add(adminUser);
                        adminUser.setAdminUser(currentUser); // Set the reverse relationship
                        userRepository.save(adminUser); // Save the admin user with new relationship
                    }
                }
            }
        }

        // Handle deleted admins
        if (userReq.getDeletedAdmins() != null && !userReq.getDeletedAdmins().isEmpty()) {
            for (String adminUserId : userReq.getDeletedAdmins()) {
                // Find the admin user in DB
                UserEntity adminUser = userRepository.findByUserId(adminUserId);
                if (adminUser == null) {
                    throw new UserServiceException("Admin user with ID " + adminUserId + " not found");
                }

                // Remove from administratives if present (thread-safe operation)
                synchronized (currentUser) {
                    List<UserEntity> administratives = currentUser.getAdministratives();
                    if (administratives != null && administratives.contains(adminUser)) {
                        administratives.remove(adminUser);
                        adminUser.setAdminUser(null); // Remove the reverse relationship
                        userRepository.save(adminUser); // Save the admin user with removed relationship
                    }
                }
            }
        }

        // Save the current user with updated administratives
        userRepository.save(currentUser);
    }

    private void updateAddresses(List<AddressDTO> newAddressDtos, UserEntity userEntity, UserReq userReq) {
        if (newAddressDtos == null) return;

        List<AddressEntity> existingAddresses = userEntity.getAddresses();

        // Handle address deletions
        if (userReq.getDeleteAddresses() != null && !userReq.getDeleteAddresses().isEmpty()) {
            existingAddresses.removeIf(addr ->
                    userReq.getDeleteAddresses().contains(addr.getAddressId())
            );
        }

        // Process all addresses
        for (AddressDTO dto : newAddressDtos) {
            AddressEntity existingAddress = null;

            // Try to find by ID first
            if (dto.getAddressId() != null) {
                existingAddress = existingAddresses.stream()
                        .filter(addr -> addr.getAddressId().equals(dto.getAddressId()))
                        .findFirst()
                        .orElse(null);
            }

            if (existingAddress != null) {
                // Update existing address
                updateExistingAddress(existingAddress, dto);
                existingAddress.setDefault(dto.isDefault());
            } else {
                // Create new address
                AddressEntity newAddress = UserMapper.mapToAddressEntity(dto);
                newAddress.setUserDetails(userEntity);
                newAddress.setAddressId(idUtils.generateId(30));
                newAddress.setDefault(dto.isDefault());
                existingAddresses.add(newAddress);
            }
        }

        // Ensure exactly one default address
        long defaultCount = existingAddresses.stream().filter(AddressEntity::isDefault).count();
        if (defaultCount != 1) {
            if (!existingAddresses.isEmpty()) {
                existingAddresses.get(0).setDefault(true);
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
        // First try by ID
        if (dto.getAddressId() != null) {
            return existingAddresses.stream()
                    .filter(addr -> dto.getAddressId().equals(addr.getAddressId()))
                    .findFirst()
                    .orElse(null);
        }

        // Fall back to content matching
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
            section.getUsers().add(userEntity);
            sectionRepo.save(section);
        });

        userEntity.getProfiles().forEach(profile -> {
            profile.getUsers().add(userEntity);
            profilesRepo.save(profile);
        });

        userEntity.getRoles().forEach(role -> {
            role.getUsers().add(userEntity);
            roleRepository.save(role);
        });
    }
    private synchronized void updateAdditionalRelationships(UserEntity userEntity, UserReq userReq) {
        if (userReq.getAdditional() != null) {
            logger.info("Processing additional relationships for user: {}", userEntity.getUserId());

            // Refresh the user entity to ensure we have the latest version
            UserEntity refreshedUser = userRepository.findById(userEntity.getId())
                    .orElseThrow(() -> new UserServiceException("User not found during update"));

            // Process additions first
            processAdditions(refreshedUser, userReq.getAdditional());

            // Then process deletions
            processDeletions(refreshedUser, userReq.getAdditional());

            // Save the changes
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

    // Section operations
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

    // Profile operations
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
