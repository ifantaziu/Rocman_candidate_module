package org.rocman.candidate.services;


import lombok.extern.slf4j.Slf4j;
import org.rocman.candidate.repositories.CandidateRepository;
import org.slf4j.MDC;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CandidateDetailsService implements UserDetailsService {

    private final CandidateRepository candidateRepository;

    public CandidateDetailsService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String reqId = MDC.get("requestId");
        MDC.put("userEmail", username);
        log.info("Attempting to authenticate user ");
        return candidateRepository.findByEmail(username)
                .map(user -> {
                    MDC.put("userId", String.valueOf(user.getId()));
                    log.info("User authenticated successfully");
                    MDC.clear();
                    return user;
                })
                .orElseThrow(() -> {
                    log.warn("Authentication failed: user not found ");
                    MDC.clear();
                    return new UsernameNotFoundException("User not found");
                });
    }
}

