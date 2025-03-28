package com.slt.peotv.userservice.lms.security;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.slt.peotv.userservice.lms.shared.dto.AuthorityDTO;
import com.slt.peotv.userservice.lms.shared.dto.RoleDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.slt.peotv.userservice.lms.entity.AuthorityEntity;
import com.slt.peotv.userservice.lms.entity.RoleEntity;
import com.slt.peotv.userservice.lms.entity.UserEntity;

public class UserPrincipal implements UserDetails {

	private final Collection<RoleDTO> roles;
	private final Integer highestRolePriority;
	private final Map<String, Integer> authorityWeights;
	private static final long serialVersionUID = -7530187709860249942L;

	private UserEntity userEntity;
	private String userId;

	public UserPrincipal(UserEntity userEntity) {
		this.userEntity = userEntity;
		this.userId = userEntity.getUserId();

		this.roles = userEntity.getRoles().stream()
				.map(role -> {
					Set<AuthorityDTO> authorityDTOs = Collections.emptySet();
					if (role.getAuthorities() != null) {
						authorityDTOs = role.getAuthorities().stream()
								.map(auth -> new AuthorityDTO(auth.getName(), auth.getWeight()))
								.collect(Collectors.toSet());
					}
					return new RoleDTO(
							role.getName(),
							role.getPriority(),
							authorityDTOs
					);
				})
				.collect(Collectors.toList());

		this.highestRolePriority = this.roles.stream()
				.map(RoleDTO::getPriority)
				.min(Integer::compare)
				.orElse(Integer.MAX_VALUE);

		this.authorityWeights = this.roles.stream()
				.flatMap(role -> role.getAuthorities() != null ? role.getAuthorities().stream() : Stream.empty())
				.collect(Collectors.toMap(
						AuthorityDTO::getName,
						AuthorityDTO::getWeight,
						Math::min
				));
	}

	public boolean hasPriority(int requiredPriority) {
		return this.highestRolePriority <= requiredPriority;
	}

	public boolean hasAuthorityWithWeight(String authority, int requiredWeight) {
		Integer userWeight = this.authorityWeights.get(authority);
		return userWeight != null && userWeight >= requiredWeight;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new HashSet<>();

		if (userEntity.getRoles() != null) {
			// Add role-based authorities
			userEntity.getRoles().forEach(role -> {
				authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
				if (role.getAuthorities() != null) {
					role.getAuthorities().forEach(auth ->
							authorities.add(new SimpleGrantedAuthority(auth.getName())));
				}
			});

			// Add priority authority
			authorities.add(new SimpleGrantedAuthority("PRIORITY_" + highestRolePriority));
		}

		return authorities;
	}

	@Override
	public String getPassword() {
		return this.userEntity.getEncryptedPassword();
	}

	@Override
	public String getUsername() {
		return this.userEntity.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		return !this.userEntity.getEmailVerificationStatus();
	}

	public String getUserId() {
		return userId;
	}
	public Long getId(){
		return userEntity.getId();
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Map<String, Object> getClaims() {
		Map<String, Object> claims = new HashMap<>();
		claims.put("roles", roles);
		claims.put("highestRolePriority", highestRolePriority);
		claims.put("authorityWeights", authorityWeights);
		return claims;
	}

	public Collection<RoleDTO> getRoles() {
		return roles;
	}

	public Integer getHighestRolePriority() {
		return highestRolePriority;
	}

	public Map<String, Integer> getAuthorityWeights() {
		return authorityWeights;
	}

	public UserEntity getUserEntity() {
		return userEntity;
	}

	public void setUserEntity(UserEntity userEntity) {
		this.userEntity = userEntity;
	}

}
