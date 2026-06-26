package com.arnav.authsystem.service;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.arnav.authsystem.dto.UserInfoDto;
import com.arnav.authsystem.entities.UserInfo;
import com.arnav.authsystem.entities.UserRole;
import com.arnav.authsystem.repository.RoleRepository;
import com.arnav.authsystem.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    private static final Logger log =
            LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    public UserDetailsServiceImpl(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder, 
                                  RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;

    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        log.debug("Entering loadUserByUsername");

        UserInfo user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        log.info("User authenticated successfully");

        return new CustomUserDetails(user);
    }
            
    public UserInfo checkIfUserAlreadyExist(UserInfoDto dto) {
        return userRepository.findByUsername(dto.getUsername()).orElse(null);
    }

    public Boolean signupUser(UserInfoDto dto) {
    if (Objects.nonNull(checkIfUserAlreadyExist(dto))) {
        return false;
    }
    
    // fetch the default role
    UserRole userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("Default role not found"));
    log.info("Found role: {}", userRole.getName()); // ADD THIS

    String userId = UUID.randomUUID().toString();
    Set<UserRole> roles = new HashSet<>();
    roles.add(userRole);
    log.info("Roles set size: {}", roles.size()); // ADD THIS
    dto.setPassword(passwordEncoder.encode(dto.getPassword()));
    UserInfo savedUser= userRepository.save(new UserInfo(userId, dto.getUsername(), dto.getPassword(), roles));
    log.info("Saved user roles: {}", savedUser.getRoles().size());
    return true; 
}

     
}
