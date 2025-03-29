package com.slt.peotv.userservice.lms.utils;

import com.slt.peotv.userservice.lms.entity.AuthorityEntity;
import com.slt.peotv.userservice.lms.entity.RoleEntity;
import com.slt.peotv.userservice.lms.entity.UserEntity;
import com.slt.peotv.userservice.lms.repository.AuthorityRepository;
import com.slt.peotv.userservice.lms.repository.RoleRepository;
import com.slt.peotv.userservice.lms.repository.UserRepository;
import com.slt.peotv.userservice.lms.shared.dto.RoleDTOArchive;
import com.slt.peotv.userservice.lms.shared.model.request.RoleReq;
import io.netty.util.internal.ConcurrentSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepo;
    private final IdUtils idUtils;

    private final Map<String, ReentrantLock> roleLocks = new ConcurrentHashMap<>();
    private final ReentrantLock creationLock = new ReentrantLock();

    public RoleService(RoleRepository roleRepository, UserRepository userRepository, AuthorityRepository authorityRepo, IdUtils idUtils) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.authorityRepo = authorityRepo;
        this.idUtils = idUtils;
    }

    public RoleDTOArchive saveRole_(RoleReq req) {
        if (req == null) {
            throw new IllegalArgumentException("Role request cannot be null");
        }
        try {
            creationLock.lock();
            Optional<RoleEntity> roleEntity = findExistingRole(req);

            RoleEntity entityToSave = roleEntity
                    .map(existingEntity -> updateRole(req, existingEntity))
                    .orElseGet(() -> saveNewRole(req));

            RoleEntity savedEntity = roleRepository.save(entityToSave);
            return UserMapper.mapToRoleDTO_(savedEntity);
        } finally {
            creationLock.unlock();
        }
    }

    private Optional<RoleEntity> findExistingRole(RoleReq req) {
        if (req.getRoleId() != null && !req.getRoleId().isEmpty()) {
            return roleRepository.findByPublicId(req.getRoleId());
        } else if (req.getName() != null && !req.getName().isEmpty()) {
            return Optional.ofNullable(roleRepository.findByName(req.getName()));
        }
        return Optional.empty();
    }

    @Transactional
    public RoleEntity updateRole(RoleReq req, RoleEntity roleEntity) {
        ReentrantLock roleLock = roleLocks.computeIfAbsent(
                roleEntity.getPublicId(),
                k -> new ReentrantLock()
        );

        try {
            roleLock.lock();

            if (roleEntity.getPublicId() == null) {
                roleEntity.setPublicId(idUtils.generateId(10));
            }

            // Process changes atomically
            processAuthorityChanges(req, roleEntity);
            processUserChanges(req, roleEntity);
            updateRoleProperties(req, roleEntity);

            return roleRepository.save(roleEntity);
        } finally {
            roleLock.unlock();
            // Clean up lock if role is no longer in use
            if (!roleRepository.existsById(roleEntity.getId())) {
                roleLocks.remove(roleEntity.getPublicId());
            }
        }
    }

    private void processAuthorityChanges(RoleReq req, RoleEntity roleEntity) {
        // Use concurrent collections for thread-safe operations
        Set<AuthorityEntity> currentAuthorities = new ConcurrentSet<>();
        currentAuthorities.addAll(roleEntity.getAuthorities());

        // Remove deleted authorities
        if (!CollectionUtils.isEmpty(req.getDeletedAuthorities())) {
            req.getDeletedAuthorities().parallelStream().forEach(authorityId -> {
                synchronized (authorityRepo) {
                    authorityRepo.findById(Long.parseLong(authorityId)).ifPresent(auth -> {
                        if (currentAuthorities.remove(auth)) {
                            System.out.println("Authority removed: " + auth.getId());
                        }
                    });
                }
            });
        }

        // Add new authorities
        if (!CollectionUtils.isEmpty(req.getAddedAuthorities())) {
            req.getAddedAuthorities().parallelStream().forEach(authorityId -> {
                synchronized (authorityRepo) {
                    authorityRepo.findById(Long.parseLong(authorityId)).ifPresent(auth -> {
                        if (currentAuthorities.add(auth)) {
                            System.out.println("Authority added: " + auth.getId());
                        }
                    });
                }
            });
        }

        roleEntity.setAuthorities(new ArrayList<>(currentAuthorities));
    }

    private void processUserChanges(RoleReq req, RoleEntity roleEntity) {
        // Use concurrent collections for thread-safe operations
        Set<UserEntity> currentUsers = new ConcurrentSet<>();
        currentUsers.addAll(roleEntity.getUsers());
        // Remove deleted users
        if (!CollectionUtils.isEmpty(req.getDeletedUsers())) {
            req.getDeletedUsers().parallelStream().forEach(userId -> {
                synchronized (userRepository) {
                    UserEntity userEntity = userRepository.findByUserId(userId);
                    if (userEntity != null && currentUsers.remove(userEntity)) {
                        userEntity.getRoles().remove(roleEntity);
                        userRepository.save(userEntity);
                    }
                }
            });
        }

        // Add new users
        if (!CollectionUtils.isEmpty(req.getAddedUsers())) {
            req.getAddedUsers().parallelStream().forEach(userId -> {
                synchronized (userRepository) {
                    UserEntity userEntity = userRepository.findByUserId(userId);
                    if (userEntity != null && currentUsers.add(userEntity)) {
                        userEntity.getRoles().add(roleEntity);
                        userRepository.save(userEntity);
                    }
                }
            });
        }

        roleEntity.setUsers(new ArrayList<>(currentUsers));
    }

    private void updateRoleProperties(RoleReq req, RoleEntity roleEntity) {
        if (req.getName() != null && !req.getName().isEmpty()) {
            roleEntity.setName(req.getName());
        }
        if (req.getPriority() != null) {
            roleEntity.setPriority(req.getPriority());
        }
    }

    public RoleEntity saveNewRole(RoleReq req) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setPublicId(idUtils.generateId(10));

        // Use concurrent collections for thread safety
        Set<UserEntity> userEntities = ConcurrentHashMap.newKeySet();
        Set<AuthorityEntity> authorityEntities = ConcurrentHashMap.newKeySet();

        // Process added users with thread-safe operations
        if (!CollectionUtils.isEmpty(req.getAddedUsers())) {
            req.getAddedUsers().parallelStream().forEach(userId -> {
                synchronized (userRepository) {
                    UserEntity user = userRepository.findByUserId(userId);
                    if (user != null) {
                        userEntities.add(user);
                    }
                }
            });
        }

        // Process added authorities with thread-safe operations
        if (!CollectionUtils.isEmpty(req.getAddedAuthorities())) {
            req.getAddedAuthorities().parallelStream().forEach(authorityId -> {
                synchronized (authorityRepo) {
                    authorityRepo.findById(Long.parseLong(authorityId)).ifPresent(authorityEntities::add);
                }
            });
        }

        // Set basic properties
        if (req.getName() != null && !req.getName().isEmpty()) {
            roleEntity.setName(req.getName());
        }
        if (req.getPriority() != null) {
            roleEntity.setPriority(req.getPriority());
        }

        // Set users and authorities (using defensive copies)
        roleEntity.setUsers(new ArrayList<>(userEntities));
        roleEntity.setAuthorities(new ArrayList<>(authorityEntities));

        return roleEntity;
    }
}