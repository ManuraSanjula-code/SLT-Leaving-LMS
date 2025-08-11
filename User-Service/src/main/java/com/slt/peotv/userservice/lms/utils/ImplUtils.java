package com.slt.peotv.userservice.lms.utils;

import com.slt.peotv.userservice.lms.entity.AuthorityEntity;
import com.slt.peotv.userservice.lms.entity.RoleEntity;
import com.slt.peotv.userservice.lms.entity.UserEntity;
import com.slt.peotv.userservice.lms.repository.AuthorityRepository;
import com.slt.peotv.userservice.lms.repository.RoleRepository;
import com.slt.peotv.userservice.lms.repository.UserRepository;
import com.slt.peotv.userservice.lms.shared.model.request.RoleReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class ImplUtils {

    @Autowired
    private AuthorityRepository authorityRepo;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private IdUtils idUtils;

    @Transactional
    public RoleEntity updateRole(RoleReq req, RoleEntity roleEntity) {
        if (roleEntity.getPublicId() == null) {
            roleEntity.setPublicId(idUtils.generateId(10));
        }
        if (!req.getDeletedAuthorities().isEmpty()) {
            req.getDeletedAuthorities().forEach(authority -> {
                Optional<AuthorityEntity> auth = authorityRepo.findById(Long.parseLong(authority));

                if (auth.isPresent()) {
                    AuthorityEntity authorityEntity = auth.get();
                    boolean removed = roleEntity.getAuthorities().removeIf(authorityE -> authorityE.equals(authorityEntity));
                    System.out.println("Authority removed: " + removed);
                }
            });
        }

        if (!req.getDeletedUsers().isEmpty()) {
            req.getDeletedUsers().forEach(userId -> {
                boolean removed = roleEntity.getUsers().removeIf(user -> user.getUserId().equals(userId));
                if (removed) {
                    UserEntity userEntity = userRepository.findByUserId(userId);
                    if (userEntity != null) {
                        userEntity.getRoles().removeIf(role -> role.equals(roleEntity));
                        userRepository.save(userEntity);
                        roleRepository.save(roleEntity);
                    }
                }
            });
        }

        if (!req.getAddedUsers().isEmpty()) {
            req.getAddedUsers().forEach(userId -> {
                UserEntity userEntity = userRepository.findByUserId(userId);
                if (userEntity != null && !roleEntity.getUsers().contains(userEntity)) {
                    roleEntity.getUsers().add(userEntity);
                    userEntity.getRoles().add(roleEntity);
                    userRepository.save(userEntity);
                }
            });
        }

        if (!roleEntity.getAuthorities().isEmpty()) {
            req.getAddedAuthorities().forEach(authority -> {
                Optional<AuthorityEntity> authorityEntity = authorityRepo.findById(Long.parseLong(authority));

                if (authorityEntity.isPresent()) {
                    AuthorityEntity auth = authorityEntity.get();

                    if (!roleEntity.getAuthorities().contains(auth)) {
                        roleEntity.getAuthorities().add(auth);
                    }
                }
            });
        }

        if (req.getName() != null && !req.getName().isEmpty()) {
            roleEntity.setName(req.getName());
        }
        if (req.getPriority() != null) {
            roleEntity.setPriority(req.getPriority());
        }
        return roleRepository.save(roleEntity);
    }

    public RoleEntity saveRole_(RoleReq req) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setPublicId(idUtils.generateId(10));
        roleEntity.setOt(req.getOt());

        List<UserEntity> userEntities = Collections.synchronizedList(new ArrayList<>());
        List<AuthorityEntity> authorityEntities = Collections.synchronizedList(new ArrayList<>());

        if (req.getAddedUsers() != null && !req.getAddedUsers().isEmpty()) {
            req.getAddedUsers().forEach(userId -> {
                synchronized (userRepository) {
                    UserEntity user = userRepository.findByUserId(userId);
                    if (user != null) {
                        userEntities.add(user);
                        if (user.getRoles() == null) {
                            user.setRoles(new ArrayList<>());
                        }
                        user.getRoles().add(roleEntity);
                    }
                }
            });
        }

        if (req.getAddedAuthorities() != null && !req.getAddedAuthorities().isEmpty()) {
            req.getAddedAuthorities().forEach(authority -> {
                synchronized (authorityRepo) {
                    Optional<AuthorityEntity> authorityEntity = authorityRepo.findById(Long.parseLong(authority));
                    authorityEntity.ifPresent(authorityEntities::add);
                }
            });
        }

        if (req.getName() != null && !req.getName().isEmpty()) {
            roleEntity.setName(req.getName());
        }
        if (req.getPriority() != null) {
            roleEntity.setPriority(req.getPriority());
        }
        roleEntity.setUsers(Collections.unmodifiableList(userEntities));
        roleEntity.setAuthorities(Collections.unmodifiableList(authorityEntities));

        return roleEntity;
    }
}
