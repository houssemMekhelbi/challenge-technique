package com.bonitasoft.technicalchallenge.resource;

import com.bonitasoft.technicalchallenge.model.ERole;
import com.bonitasoft.technicalchallenge.model.Role;
import com.bonitasoft.technicalchallenge.model.User;
import com.bonitasoft.technicalchallenge.payload.request.auth.SignupRequest;
import com.bonitasoft.technicalchallenge.payload.response.MessageResponse;
import com.bonitasoft.technicalchallenge.payload.response.UserInfoResponse;
import com.bonitasoft.technicalchallenge.repository.RoleRepository;
import com.bonitasoft.technicalchallenge.repository.UserRepository;
import com.bonitasoft.technicalchallenge.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/admin")
public class AdminResource {
    private static final Logger logger = LoggerFactory.getLogger(AdminResource.class);

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserInfoResponse> userInfoResponses = users.stream()
                .map(user -> {
                    List<String> roleNames = user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .collect(Collectors.toList());

                    return new UserInfoResponse(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            roleNames
                    );
                })
                .toList();
        return ResponseEntity.ok().body(userInfoResponses);
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") Long userId) {
        userRepository.deleteById(userId);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable("userId") Long userId, @Valid @RequestBody SignupRequest updateUserRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        user.setUsername(updateUserRequest.getUsername());
        user.setEmail(updateUserRequest.getEmail());
        user.setPassword(encoder.encode(updateUserRequest.getPassword()));

        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User updated successfully!"));
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> setUserRole(@PathVariable("userId") Long userId, @RequestParam("role") String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        Role newRole = roleRepository.findByName(ERole.valueOf(role))
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));

        user.getRoles().clear();
        user.getRoles().add(newRole);
        userRepository.save(user);

        return ResponseEntity.ok().body(new MessageResponse("Role updated successfully!"));
    }

    @DeleteMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeUserRole(@PathVariable("userId") Long userId, @RequestParam("role") String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        Role roleToRemove = roleRepository.findByName(ERole.valueOf(role))
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));

        if (user.getRoles().contains(roleToRemove)) {
            user.getRoles().remove(roleToRemove);
            userRepository.save(user);
            return ResponseEntity.ok().body(new MessageResponse("Role removed successfully!"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User does not have the specified role."));
        }
    }

}
