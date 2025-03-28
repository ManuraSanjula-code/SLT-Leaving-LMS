package com.slt.peotv.userservice.lms.security;

import com.slt.peotv.userservice.lms.repository.UserRepository;
import com.slt.peotv.userservice.lms.security.Priority.PriorityPermissionEvaluator;
import com.slt.peotv.userservice.lms.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableWebSecurity
@Configuration
public class WebSecurity {

    private final UserService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public WebSecurity(UserService userDetailsService,
                       BCryptPasswordEncoder bCryptPasswordEncoder
    ) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // Configure AuthenticationManagerBuilder
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);

        // Get AuthenticationManager
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        http
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers(HttpMethod.GET, SecurityConstants.VERIFICATION_EMAIL_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityConstants.GET_ROLE).permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityConstants.USERS).permitAll()
                        .requestMatchers(HttpMethod.POST, SecurityConstants.UPLOAD_CSV_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityConstants.GET_ROLE_).permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityConstants.GET_PROFILE).permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityConstants.GET_SECTION).permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityConstants.GET_AUTH).permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityConstants.GET_All_NAMES_ROLE).permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityConstants.GET_All_NAMES_SECTIONS).permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityConstants.GET_All_NAMES_PROFILES).permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityConstants.CHECK_).permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityConstants.TEMP_USERS).permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/login/temp").permitAll()

                        .requestMatchers(HttpMethod.POST, SecurityConstants.UPLOAD_JSON_URL)
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, SecurityConstants.PASSWORD_RESET_REQUEST_URL)
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, SecurityConstants.PASSWORD_RESET_URL)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityConstants.IMAGE).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**"))
                        .permitAll()
                        .requestMatchers("/api-docs", "/swagger-ui/**")
                        .permitAll()
                        .anyRequest().authenticated())

                .addFilter(getAuthenticationFilter(authenticationManager))
                .addFilter(new AuthorizationFilter(authenticationManager, userDetailsService))
                .authenticationManager(authenticationManager)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.headers((headers) -> headers.frameOptions((frameOptions) -> frameOptions.sameOrigin()));

        return http.build();
    }


    protected AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) throws Exception {
        final AuthenticationFilter filter = new AuthenticationFilter(authenticationManager);
        filter.setFilterProcessesUrl("/users/login");
        return filter;
    }
    @Bean
    public DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler =
                new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(new PriorityPermissionEvaluator());
        return expressionHandler;
    }
}
