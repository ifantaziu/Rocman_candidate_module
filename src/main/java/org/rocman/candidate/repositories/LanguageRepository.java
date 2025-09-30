package org.rocman.candidate.repositories;

import org.rocman.candidate.entities.Language;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language, Long> {}
