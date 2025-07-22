package org.rocman.candidate.services;


import org.rocman.candidate.repositories.CandidateRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
    public class CandidateDetailsService implements UserDetailsService {

        private final CandidateRepository candidateRepository;

        public CandidateDetailsService(CandidateRepository candidateRepository) {
            this.candidateRepository = candidateRepository;
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return candidateRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

    }

