package com.bonitasoft.technicalchallenge.resource;

import com.bonitasoft.technicalchallenge.payload.response.UserInfoResponse;
import com.bonitasoft.technicalchallenge.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/api/user")
public class UserResource {
    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);


    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('CHEF') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserInfoResponse userInfoResponse = new UserInfoResponse(userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);

        return ResponseEntity.ok().body(new UserInfoResponse(userInfoResponse.getId(),
                userInfoResponse.getUsername(),
                userInfoResponse.getEmail(),
                roles));
    }

}
