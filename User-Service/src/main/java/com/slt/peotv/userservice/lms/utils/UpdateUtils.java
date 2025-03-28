package com.slt.peotv.userservice.lms.utils;

import com.slt.peotv.userservice.lms.entity.AddressEntity;
import com.slt.peotv.userservice.lms.entity.UserEntity;
import com.slt.peotv.userservice.lms.exceptions.UserServiceException;
import com.slt.peotv.userservice.lms.repository.ProfilesRepo;
import com.slt.peotv.userservice.lms.repository.RoleRepository;
import com.slt.peotv.userservice.lms.repository.SectionRepo;
import com.slt.peotv.userservice.lms.repository.UserRepository;
import com.slt.peotv.userservice.lms.shared.dto.AddressDTO;
import com.slt.peotv.userservice.lms.shared.dto.UserDto;
import com.slt.peotv.userservice.lms.shared.model.request.UserReq;
import com.slt.peotv.userservice.lms.shared.model.response.ErrorMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;
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
                newAddress.setAddressId(idUtils.generateUserId(30));
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

    private void processAddress(AddressDTO dto, List<AddressEntity> existingAddresses, UserEntity userEntity) {
        // Try to find existing address by ID
        AddressEntity existingAddress = dto.getAddressId() != null ?
                existingAddresses.stream()
                        .filter(addr -> addr.getAddressId().equals(dto.getAddressId()))
                        .findFirst()
                        .orElse(null) :
                null;

        // If no ID match, try to find by content
        if (existingAddress == null) {
            existingAddress = findAddressByContent(dto, existingAddresses);
        }

        if (existingAddress != null) {
            // Update existing address
            updateExistingAddress(existingAddress, dto);
        } else {
            // Create new address
            addNewAddress(dto, existingAddresses, userEntity, false);
        }
    }

    private void processDefaultAddress(AddressDTO dto, List<AddressEntity> existingAddresses, UserEntity userEntity) {
        // First unset all existing defaults
        existingAddresses.forEach(addr -> addr.setDefault(false));

        // Try to find if this default address matches an existing one
        AddressEntity existingDefault = findExistingDefault(dto, existingAddresses);

        if (existingDefault != null) {
            // Update the existing address to be default
            updateExistingAddress(existingDefault, dto);
            existingDefault.setDefault(true);
        } else {
            // Add new default address
            addNewAddress(dto, existingAddresses, userEntity, true);
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

    private void addNewAddress(AddressDTO dto, List<AddressEntity> existingAddresses,
                               UserEntity userEntity, boolean isDefault) {
        AddressEntity newAddress = UserMapper.mapToAddressEntity(dto);
        newAddress.setUserDetails(userEntity);
        newAddress.setAddressId(idUtils.generateUserId(30));
        newAddress.setDefault(isDefault);
        existingAddresses.add(newAddress);
    }

    private void updateAddresses_(List<AddressDTO> addressDtos, UserEntity userEntity) {
        if (addressDtos != null && !addressDtos.isEmpty()) {
            List<AddressEntity> addresses = userEntity.getAddresses();
            addressDtos.forEach(dto -> {
                AddressEntity addressEntity = UserMapper.mapToAddressEntity(dto);
                addressEntity.setUserDetails(userEntity);
                addressEntity.setAddressId(idUtils.generateUserId(30));
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
