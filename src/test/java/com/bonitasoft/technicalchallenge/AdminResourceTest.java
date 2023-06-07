package com.bonitasoft.technicalchallenge;

import com.bonitasoft.technicalchallenge.model.ERole;
import com.bonitasoft.technicalchallenge.model.Role;
import com.bonitasoft.technicalchallenge.model.User;
import com.bonitasoft.technicalchallenge.payload.request.auth.SignupRequest;
import com.bonitasoft.technicalchallenge.payload.response.MessageResponse;
import com.bonitasoft.technicalchallenge.repository.RoleRepository;
import com.bonitasoft.technicalchallenge.repository.UserRepository;
import com.bonitasoft.technicalchallenge.resource.AdminResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AdminResourceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AdminResource adminResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUsers() {
        User user = new User("john", "john@example.com", "password");
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        ResponseEntity<?> responseEntity = adminResource.getAllUsers();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(Collections.singletonList(user), responseEntity.getBody());

        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testDeleteUser() {
        Long userId = 1L;

        ResponseEntity<?> responseEntity = adminResource.deleteUser(userId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(new MessageResponse("User deleted successfully!"), responseEntity.getBody());

        verify(userRepository, times(1)).deleteById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testUpdateUser() {
        Long userId = 1L;
        SignupRequest updateUserRequest = new SignupRequest();
        updateUserRequest.setUsername("newUsername");
        updateUserRequest.setEmail("newEmail@example.com");
        updateUserRequest.setPassword("newPassword");

        User existingUser = new User("oldUsername", "oldEmail@example.com", "oldPassword");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(encoder.encode(updateUserRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        ResponseEntity<?> responseEntity = adminResource.updateUser(userId, updateUserRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(new MessageResponse("User updated successfully!"), responseEntity.getBody());

        assertEquals("newUsername", existingUser.getUsername());
        assertEquals("newEmail@example.com", existingUser.getEmail());
        assertEquals("encodedPassword", existingUser.getPassword());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(existingUser);
        verifyNoMoreInteractions(userRepository);
        verify(encoder, times(1)).encode(updateUserRequest.getPassword());
        verifyNoMoreInteractions(encoder);
    }

    @Test
    void testSetUserRole() {
        Long userId = 1L;
        String role = "ADMIN";

        User user = new User("john", "john@example.com", "password");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Role newRole = new Role(ERole.ROLE_ADMIN);
        when(roleRepository.findByName(ERole.valueOf(role))).thenReturn(Optional.of(newRole));

        ResponseEntity<?> responseEntity = adminResource.setUserRole(userId, role);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(new MessageResponse("Role updated successfully!"), responseEntity.getBody());

        assertEquals(Collections.singleton(newRole), user.getRoles());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
        verifyNoMoreInteractions(userRepository);
        verify(roleRepository, times(1)).findByName(ERole.valueOf(role));
        verifyNoMoreInteractions(roleRepository);
    }

    @Test
    void testRemoveUserRole() {
        Long userId = 1L;
        String role = "ADMIN";

        User user = new User("john", "john@example.com", "password");
        Role roleToRemove = new Role(ERole.ROLE_ADMIN);
        user.getRoles().add(roleToRemove);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(ERole.valueOf(role))).thenReturn(Optional.of(roleToRemove));

        ResponseEntity<?> responseEntity = adminResource.removeUserRole(userId, role);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(new MessageResponse("Role removed successfully!"), responseEntity.getBody());

        assertEquals(Collections.emptySet(), user.getRoles());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
        verifyNoMoreInteractions(userRepository);
        verify(roleRepository, times(1)).findByName(ERole.valueOf(role));
        verifyNoMoreInteractions(roleRepository);
    }
}
