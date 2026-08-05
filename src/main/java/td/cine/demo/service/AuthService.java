package td.cine.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import td.cine.demo.dto.AuthResponse;
import td.cine.demo.dto.LoginRequest;
import td.cine.demo.dto.RegisterRequest;
import td.cine.demo.dto.UserDto;
import td.cine.demo.exception.ConflictException;
import td.cine.demo.model.User;
import td.cine.demo.model.enums.UserRole;
import td.cine.demo.repository.UserRepository;
import td.cine.demo.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new ConflictException("Email already used: " + request.email());
    }
    User user = new User();
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setBirthdate(request.birthdate());
    user.setEmail(request.email());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setPhone(request.phone());
    user.setRole(UserRole.CLIENT);
    userRepository.save(user);

    String token = jwtService.generateToken(user);
    return new AuthResponse(token, toDto(user));
  }

  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    User user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new ConflictException("Invalid credentials"));
    String token = jwtService.generateToken(user);
    return new AuthResponse(token, toDto(user));
  }

  private UserDto toDto(User user) {
    return new UserDto(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getBirthdate(),
        user.getEmail(),
        user.getPhone(),
        user.getRole());
  }
}
