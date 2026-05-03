package com.shootersplatform.backend.identity.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataRoleRepository extends JpaRepository<RoleEntity, String> {
}
