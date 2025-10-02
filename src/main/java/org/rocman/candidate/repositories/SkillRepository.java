package org.rocman.candidate.repositories;

import org.rocman.candidate.entities.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {}
