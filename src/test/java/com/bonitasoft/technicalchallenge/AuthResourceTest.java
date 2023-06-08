package com.bonitasoft.technicalchallenge;

import com.bonitasoft.technicalchallenge.model.ERole;
import com.bonitasoft.technicalchallenge.model.Role;
import com.bonitasoft.technicalchallenge.model.User;
import com.bonitasoft.technicalchallenge.payload.request.auth.LoginRequest;
import com.bonitasoft.technicalchallenge.payload.request.auth.SignupRequest;
import com.bonitasoft.technicalchallenge.payload.response.MessageResponse;
import com.bonitasoft.technicalchallenge.payload.response.UserInfoResponse;
import com.bonitasoft.technicalchallenge.repository.RoleRepository;
import com.bonitasoft.technicalchallenge.repository.UserRepository;
import com.bonitasoft.technicalchallenge.resource.AuthResource;
import com.bonitasoft.technicalchallenge.security.jwt.JwtUtils;
import com.bonitasoft.technicalchallenge.security.services.EmailService;
import com.bonitasoft.technicalchallenge.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AuthResourceTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthResource authResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticateUser() {
        LoginRequest loginRequest = new LoginRequest("john", "password");
        Collection<? extends GrantedAuthority> roles = Arrays.asList(
                new SimpleGrantedAuthority(ERole.ROLE_USER.name())
        );
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "john", "john@example.com", "password", roles);
        Authentication authentication = new UsernamePasswordAuthenticationToken("john", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResponseCookie jwtCookie = ResponseCookie.fromClientResponse("jwt", "jwtValue")
                .httpOnly(true)
                .maxAge(-1)
                .path("/")
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtCookie(userDetails)).thenReturn(jwtCookie);

        ResponseEntity<?> responseEntity = authResource.authenticateUser(loginRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(userDetails.getId(), ((UserInfoResponse) responseEntity.getBody()).getId());
        assertEquals(userDetails.getUsername(), ((UserInfoResponse) responseEntity.getBody()).getUsername());
        assertEquals(userDetails.getEmail(), ((UserInfoResponse) responseEntity.getBody()).getEmail());
        assertEquals(Arrays.asList("ROLE_USER"), ((UserInfoResponse) responseEntity.getBody()).getRoles());
        assertEquals(jwtCookie.toString(), responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE));

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtils, times(1)).generateJwtCookie(userDetails);
        verifyNoMoreInteractions(authenticationManager, jwtUtils);
    }

    @Test
    void testRegisterUser_UsernameTaken() {
        SignupRequest signUpRequest = new SignupRequest("john", "john@example.com", "password");
        when(userRepository.existsByUsername("john")).thenReturn(true);

        ResponseEntity<?> responseEntity = authResource.registerUser(signUpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        MessageResponse expectedResponse = new MessageResponse("Error: Username is already taken!");
        MessageResponse actualResponse = (MessageResponse) responseEntity.getBody();

        assertEquals(expectedResponse.getMessage(), actualResponse.getMessage());

        verify(userRepository, times(1)).existsByUsername("john");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testRegisterUser_EmailInUse() {
        SignupRequest signUpRequest = new SignupRequest("john", "john@example.com", "password");
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        ResponseEntity<?> responseEntity = authResource.registerUser(signUpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        MessageResponse expectedResponse = new MessageResponse("Error: Email is already in use!");
        MessageResponse actualResponse = (MessageResponse) responseEntity.getBody();

        assertEquals(expectedResponse.getMessage(), actualResponse.getMessage());

        verify(userRepository, times(1)).existsByUsername("john");
        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testRegisterUser_Success() {
        SignupRequest signUpRequest = new SignupRequest("john", "john@example.com", "password");
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(new Role(ERole.ROLE_USER)));
        when(encoder.encode("password")).thenReturn("encodedPassword");

        ResponseEntity<?> responseEntity = authResource.registerUser(signUpRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        MessageResponse expectedResponse = new MessageResponse("User registered successfully!");
        MessageResponse actualResponse = (MessageResponse) responseEntity.getBody();

        assertEquals(expectedResponse.getMessage(), actualResponse.getMessage());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).existsByUsername("john");
        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(userRepository, times(1)).save(userCaptor.capture());
        verify(roleRepository, times(1)).findByName(ERole.ROLE_USER);
        verify(emailService, times(1)).sendWelcomeEmail("john@example.com");
        verifyNoMoreInteractions(userRepository, roleRepository, emailService);

        User savedUser = userCaptor.getValue();
        assertEquals("john", savedUser.getUsername());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());

        Set<Role> actualRoles = new HashSet<>(savedUser.getRoles());
        Set<Role> expectedRoles = Collections.singleton(new Role(ERole.ROLE_USER));

        assertEquals(expectedRoles.size(), actualRoles.size());

        for (Role expectedRole : expectedRoles) {
            boolean found = false;
            for (Role actualRole : actualRoles) {
                if (expectedRole.getName().equals(actualRole.getName())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }
}
