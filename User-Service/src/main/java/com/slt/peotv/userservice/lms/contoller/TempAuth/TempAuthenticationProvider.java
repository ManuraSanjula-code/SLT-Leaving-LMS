package com.slt.peotv.userservice.lms.contoller.TempAuth;

import com.slt.peotv.userservice.lms.entity.TempUser;
import com.slt.peotv.userservice.lms.repository.TempUserRepo;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TempAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final TempUserRepo tempUserRepo;
    public TempAuthenticationProvider(
            UserDetailsService userDetailsService,
            TempUserRepo tempUserRepo
    ) {
        this.userDetailsService = userDetailsService;
        this.tempUserRepo = tempUserRepo;
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String combinedUsername = authentication.getName();
        String password = authentication.getCredentials().toString();

        String[] parts = combinedUsername.split(" ");
        String email = parts[0];
        String loginType = parts.length > 1 ? parts[1] : "DEFAULT";

        if ("TEMP".equals(loginType)) {

            Optional<TempUser> temp = tempUserRepo.findValidUser(email, password);
            if (temp.isPresent()) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(temp.get().getEmail() + " " + "TEMP");
                return new UsernamePasswordAuthenticationToken(
                        userDetails,
                        password,
                        userDetails.getAuthorities()
                );
            }else{
                throw new BadCredentialsException("Invalid password");
            }

        } else {
            throw new BadCredentialsException("Unsupported login type");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}