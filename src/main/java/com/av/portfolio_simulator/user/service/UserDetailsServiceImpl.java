package com.av.portfolio_simulator.user.service;

import com.av.portfolio_simulator.security.UserPrincipal;
import com.av.portfolio_simulator.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of Spring Security's UserDetailsService interface.
 * Acts as the bridge between the database and Spring Security's authentication mechanism.
 * Called automatically by the JwtAuthFilter on every authenticated request.
 */
@Service
@RequiredArgsConstructor
@NullMarked
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by email and wraps it in a UserPrincipal.
     * The parameter is named "email" for clarity, but Spring Security
     * refers to it internally as "username".
     *
     * @throws UsernameNotFoundException if no user exists with the given email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

}
