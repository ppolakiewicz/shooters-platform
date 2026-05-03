package com.shootersplatform.backend.identity.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
class RoleEntity {

    @Id
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    String getName() {
        return name;
    }
}
