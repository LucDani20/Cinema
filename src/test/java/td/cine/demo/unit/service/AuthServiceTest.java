package td.cine.demo.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import td.cine.demo.dto.AuthResponse;
import td.cine.demo.dto.RegisterRequest;
import td.cine.demo.exception.ConflictException;
import td.cine.demo.model.User;
import td.cine.demo.model.enums.UserRole;
import td.cine.demo.repository.UserRepository;
import td.cine.demo.security.JwtService;
import td.cine.demo.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;
  @Mock private AuthenticationManager authenticationManager;

  @InjectMocks private AuthService authService;

  @Test
  void register_throwsConflictWhenEmailAlreadyUsed() {
    RegisterRequest request =
        new RegisterRequest(
            "Jose", "Rick", LocalDate.of(2000, 1, 1), "jose@test.com", "password123", "0340000000");

    when(userRepository.existsByEmail("jose@test.com")).thenReturn(true);

    assertThatThrownBy(() -> authService.register(request)).isInstanceOf(ConflictException.class);
  }

  @Test
  void register_createsClientAndReturnsToken() {
    RegisterRequest request =
        new RegisterRequest(
            "Jose", "Rick", LocalDate.of(2000, 1, 1), "jose@test.com", "password123", "0340000000");

    when(userRepository.existsByEmail("jose@test.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt-token");

    AuthResponse response = authService.register(request);

    assertThat(response.token()).isEqualTo("fake-jwt-token");
    assertThat(response.user().role()).isEqualTo(UserRole.CLIENT);
    assertThat(response.user().email()).isEqualTo("jose@test.com");
  }

  @Test
  void login_throwsConflictWhenUserNotFound() {
    td.cine.demo.dto.LoginRequest request =
        new td.cine.demo.dto.LoginRequest("missing@test.com", "password123");
    when(userRepository.findByEmail("missing@test.com")).thenReturn(java.util.Optional.empty());

    assertThatThrownBy(() -> authService.login(request)).isInstanceOf(ConflictException.class);
  }
}
