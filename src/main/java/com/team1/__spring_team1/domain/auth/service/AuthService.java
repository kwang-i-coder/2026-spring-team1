package com.team1.__spring_team1.domain.auth.service;

import com.team1.__spring_team1.domain.auth.dto.SignupRequest;
import com.team1.__spring_team1.domain.auth.dto.SignupResponse;
import com.team1.__spring_team1.domain.user.entity.User;
import com.team1.__spring_team1.domain.user.repository.UserRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
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
}