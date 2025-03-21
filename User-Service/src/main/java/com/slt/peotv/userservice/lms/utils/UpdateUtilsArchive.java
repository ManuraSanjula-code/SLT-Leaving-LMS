package com.slt.peotv.userservice.lms.utils;

import com.slt.peotv.userservice.lms.entity.AddressEntity;
import com.slt.peotv.userservice.lms.entity.UserEntity;
import com.slt.peotv.userservice.lms.exceptions.UserServiceException;
import com.slt.peotv.userservice.lms.repository.ProfilesRepo;
import com.slt.peotv.userservice.lms.repository.RoleRepository;
import com.slt.peotv.userservice.lms.repository.SectionRepo;
import com.slt.peotv.userservice.lms.repository.UserRepository;
import com.slt.peotv.userservice.lms.shared.Utils;
import com.slt.peotv.userservice.lms.shared.dto.AddressDTO;
import com.slt.peotv.userservice.lms.shared.dto.UserDto;
import com.slt.peotv.userservice.lms.shared.model.response.ErrorMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Service
public class UpdateUtilsArchive {
    private static final Logger logger = LoggerFactory.getLogger(UpdateUtilsArchive.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SectionRepo sectionRepo;

    @Autowired
    private ProfilesRepo profilesRepo;

    @Autowired
    private Utils utils;

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

        // Update HOD, Supervisor, and Other fields
        updateReferenceField(userDto.getHod(), userRepository::findByEmployeeId, userEntity::setHod);
        updateReferenceField(userDto.getSupervisor(), userRepository::findByEmployeeId, userEntity::setSupervisor);
        updateReferenceField(userDto.getOther(), userRepository::findByEmployeeId, userEntity::setOther);

        // Handle addresses
        updateAddresses(userDto.getAddresses(), userEntity);

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

    private void updateAddresses(List<AddressDTO> addressDtos, UserEntity userEntity) {
        if (addressDtos != null && !addressDtos.isEmpty()) {
            List<AddressEntity> addresses = userEntity.getAddresses();
            addressDtos.forEach(dto -> {
                AddressEntity addressEntity = UserMapper.mapToAddressEntity(dto);
                addressEntity.setUserDetails(userEntity);
                addressEntity.setAddressId(utils.generateUserId(30));
                addresses.add(addressEntity);
            });
            userEntity.setAddresses(addresses);
        }
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
}
