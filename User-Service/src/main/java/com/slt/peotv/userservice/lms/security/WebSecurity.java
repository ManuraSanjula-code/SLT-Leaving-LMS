package com.slt.peotv.userservice.lms.security;

import com.slt.peotv.userservice.lms.security.Priority.PriorityPermissionEvaluator;
import com.slt.peotv.userservice.lms.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableWebSecurity
@Configuration
public class WebSecurity {

    private final UserService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public WebSecurity(UserService userDetailsService,
                       BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authManager = authenticationManager(http.getSharedObject(AuthenticationConfiguration.class));

        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, SecurityConstants.VERIFICATION_EMAIL_URL).permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.GET_ROLE).permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.USERS).permitAll()
                .antMatchers(HttpMethod.POST, SecurityConstants.UPLOAD_CSV_URL).permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.GET_ROLE_).permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.GET_PROFILE).permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.GET_SECTION).permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.GET_AUTH).permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.GET_All_NAMES_ROLE).permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.GET_All_NAMES_SECTIONS).permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.GET_All_NAMES_PROFILES).permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.CHECK_).permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.TEMP_USERS).permitAll()
                .antMatchers(HttpMethod.POST, "/users/login/temp").permitAll()
                .antMatchers(HttpMethod.POST, SecurityConstants.UPLOAD_JSON_URL).permitAll()
                .antMatchers(HttpMethod.POST, SecurityConstants.PASSWORD_RESET_REQUEST_URL).permitAll()
                .antMatchers(HttpMethod.POST, SecurityConstants.PASSWORD_RESET_URL).permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.IMAGE).permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/api-docs", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(getAuthenticationFilter(authManager))
                .addFilter(new AuthorizationFilter(authManager, userDetailsService))
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .headers().frameOptions().sameOrigin();

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return authProvider;
    }

    protected AuthenticationFilter getAuthenticationFilter(AuthenticationManager authManager) throws Exception {
        final AuthenticationFilter filter = new AuthenticationFilter(authManager);
        filter.setFilterProcessesUrl("/users/login");
        return filter;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler =
                new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(new PriorityPermissionEvaluator());
        return expressionHandler;
    }
}