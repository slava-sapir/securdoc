package com.springprojects.securedoc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springprojects.securedoc.entity.CredentialEntity;

@Repository
public interface CredentialRepository extends JpaRepository<CredentialEntity, Long> {
     Optional<CredentialEntity> getCredentialByUserEntityId(Long userId);
}
