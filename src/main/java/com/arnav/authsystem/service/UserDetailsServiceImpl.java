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

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.regions.Region;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    @org.springframework.beans.factory.annotation.Value("${sns.topic.arn:}")
    private String snsTopicArn;
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
    UserInfo savedUser= userRepository.save(new UserInfo(userId, dto.getUsername(), dto.getPassword(),"FREE", roles));
    log.info("Saved user roles: {}", savedUser.getRoles().size());
    // Publish signup event to SNS
    try {
        if (snsTopicArn != null && !snsTopicArn.isBlank()) {
            SnsClient snsClient = SnsClient.builder()
                    .region(Region.AP_SOUTHEAST_2)
                    .build();
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(snsTopicArn)
                    .message("New user signed up: " + dto.getUsername())
                    .subject("LinkShrink - New Signup")
                    .build();
            snsClient.publish(publishRequest);
            snsClient.close();
            log.info("SNS notification published for new user: {}", dto.getUsername());
        }
    } catch (Exception e) {
        log.error("Failed to publish SNS notification", e);
        // don't fail signup if SNS publish fails
    }

    
    return true; 
}

     
}
