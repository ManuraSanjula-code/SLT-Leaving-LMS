package com.slt.peotv.lmsmangmentservice.security;

import com.slt.peotv.lmsmangmentservice.feign_client.model.UserRest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserPrincipal implements UserDetails {

	private static final long serialVersionUID = 1747474848322L;
	private UserRest user;
	private Integer highestRolePriority;
	public UserPrincipal() {

	}
	public UserPrincipal(UserRest user) {
		this.user = user;
		this.highestRolePriority = user.getHighestRolePriority();
	}
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = user.getAuthorities();
		authorities.add(new SimpleGrantedAuthority("PRIORITY_" + highestRolePriority));
		return authorities;
	}

	@Override
	public String getPassword() {
		return user.getEmail();
	}

	@Override
	public String getUsername() {
		return user.getUserId();
	}

	@Override
	public boolean isAccountNonExpired() {
		return user.getActive() != 0;
	}

	@Override
	public boolean isAccountNonLocked() {
		return user.getActive() != 0;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return user.getActive() != 0;
	}

	@Override
	public boolean isEnabled() {
		return user.getActive() != 0;
	}

	public UserRest getUser() {
		return user;
	}

	public void setUser(UserRest user) {
		this.user = user;
	}

	public Integer getHighestRolePriority() {
		return highestRolePriority;
	}

	public void setHighestRolePriority(Integer highestRolePriority) {
		this.highestRolePriority = highestRolePriority;
	}
}
