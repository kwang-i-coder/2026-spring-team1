package com.team1.__spring_team1.domain.auth.service;

import com.team1.__spring_team1.domain.auth.dto.LoginRequest;
import com.team1.__spring_team1.domain.auth.dto.LoginResponse;
import com.team1.__spring_team1.domain.auth.dto.LoginResult;
import com.team1.__spring_team1.domain.auth.dto.SignupRequest;
import com.team1.__spring_team1.domain.auth.dto.SignupResponse;
import com.team1.__spring_team1.domain.auth.entity.Session;
import com.team1.__spring_team1.domain.auth.repository.SessionRepository;
import com.team1.__spring_team1.domain.user.entity.User;
import com.team1.__spring_team1.domain.user.repository.UserRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import com.team1.__spring_team1.global.security.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getLoginId(),
                encodedPassword,
                request.getName()
        );

        User savedUser = userRepository.save(user);

        return SignupResponse.from(savedUser);
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        String sessionToken = generateSessionToken();
        String sessionTokenHash = TokenHashUtil.hash(sessionToken);

        Session session = new Session(
                user,
                sessionTokenHash,
                LocalDateTime.now().plusDays(7)
        );

        sessionRepository.save(session);

        return new LoginResult(
                LoginResponse.from(user, sessionToken),
                sessionToken
        );
    }

    private String generateSessionToken() {
        byte[] randomBytes = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);

        return HexFormat.of().formatHex(randomBytes);
    }

    @Transactional
    public void logout(String sessionToken) {
        String sessionTokenHash = TokenHashUtil.hash(sessionToken);

        Session session = sessionRepository.findBySessionTokenHash(sessionTokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (!session.isValid()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        session.revoke();
    }
}