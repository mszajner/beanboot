package io.github.mszajner.beanboot.webapp.migrations;

import io.github.mszajner.beanboot.security.api.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.migrations.api.MigrationTask;
import io.github.mszajner.beanboot.starter.entities.GroupEntity;
import io.github.mszajner.beanboot.starter.entities.UserEntity;
import io.github.mszajner.beanboot.starter.repositories.GroupRepository;
import io.github.mszajner.beanboot.starter.repositories.UserRepository;

import java.util.Set;
import java.util.UUID;

@Component
@Order(1)
@RequiredArgsConstructor
public class LoadInitialDataMigration implements MigrationTask {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UUID id() {
        return UUID.fromString("0af90907-e6b5-4814-bf87-53942c09728e");
    }

    @Override
    public void run() {
        GroupEntity adminsGroup = addGroup("Administrators", Set.of(io.github.mszajner.beanboot.webapp.api.Role.ADMIN));
        addGroup("Users", Set.of(io.github.mszajner.beanboot.webapp.api.Role.USER));
        addUser("Admin",
                "Administracyjny",
                "admin@example.com",
                "admin",
                Set.of(adminsGroup));
    }

    @SuppressWarnings("SameParameterValue")
    private void addUser(String firstName, String lastName, String email, String password, Set<GroupEntity> groups) {
        UserEntity user = new UserEntity();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setGroups(groups);
        userRepository.save(user);
    }

    private GroupEntity addGroup(String groupName, Set<Role> roles) {
        GroupEntity group = new GroupEntity();
        group.setName(groupName);
        group.setRoles(roles);
        return groupRepository.save(group);
    }
}
