package com.shootersplatform.backend.identity.infrastructure;

import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.UserAccount;
import com.shootersplatform.backend.identity.domain.UserAccountRepository;
import com.shootersplatform.backend.identity.domain.UserId;
import com.shootersplatform.backend.identity.domain.UserRole;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
class JpaUserAccountRepository implements UserAccountRepository {

    private final SpringDataUserAccountRepository userAccounts;
    private final SpringDataRoleRepository roles;

    JpaUserAccountRepository(SpringDataUserAccountRepository userAccounts, SpringDataRoleRepository roles) {
        this.userAccounts = userAccounts;
        this.roles = roles;
    }

    @Override
    public boolean existsByEmail(EmailAddress email) {
        return userAccounts.existsByEmail(email.value());
    }

    @Override
    public Optional<UserAccount> findByEmail(EmailAddress email) {
        return userAccounts.findByEmail(email.value()).map(this::toDomain);
    }

    @Override
    public Optional<UserAccount> findById(UserId id) {
        return userAccounts.findById(id.value()).map(this::toDomain);
    }

    @Override
    public UserAccount save(UserAccount userAccount) {
        UserAccountEntity entity = new UserAccountEntity();
        entity.setId(userAccount.id().value());
        entity.setEmail(userAccount.email().value());
        entity.setPasswordHash(userAccount.passwordHash());
        entity.setEnabled(userAccount.enabled());
        entity.setCreatedAt(userAccount.createdAt());
        entity.setUpdatedAt(userAccount.updatedAt());
        entity.setRoles(userAccount.roles().stream()
                .map(role -> roles.findById(role.name()).orElseThrow())
                .collect(Collectors.toSet()));

        return toDomain(userAccounts.save(entity));
    }

    private UserAccount toDomain(UserAccountEntity entity) {
        Set<UserRole> roleNames = entity.getRoles().stream()
                .map(RoleEntity::getName)
                .map(UserRole::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        return new UserAccount(
                new UserId(entity.getId()),
                new EmailAddress(entity.getEmail()),
                entity.getPasswordHash(),
                entity.isEnabled(),
                roleNames,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
