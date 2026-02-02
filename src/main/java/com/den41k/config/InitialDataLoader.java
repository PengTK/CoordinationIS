package com.den41k.config;

import com.den41k.model.Role;
import com.den41k.model.RolePermission;
import com.den41k.model.User;
import com.den41k.service.RoleService;
import com.den41k.service.UserService;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Singleton
public class InitialDataLoader {

    private static final Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

    private final UserService userService;
    private final RoleService roleService;

    @Inject
    public InitialDataLoader(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @EventListener
    @Transactional
    public void onStartup(StartupEvent event) {
        log.info("=== Запуск инициализации данных приложения ===");
        
        try {
            createAdminRole();
            
            createAdminUser();

            createGuestRole();

            log.info("=== Инициализация данных завершена ===");
        } catch (Exception e) {
            log.error("Ошибка при инициализации данных: {}", e.getMessage(), e);
        }
    }

    private void createAdminRole() {
        boolean adminRoleExists = roleService.getAllRoles().stream()
            .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
        
        if (!adminRoleExists) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setProjectPermission(RolePermission.ALL_PERMS);
            adminRole.setTaskPermission(RolePermission.ALL_PERMS);
            adminRole.setRolePermission(RolePermission.ALL_PERMS);
            adminRole.setUserPermission(RolePermission.ALL_PERMS);
            
            roleService.save(adminRole);
            log.info("✅ Создана роль администратора: ADMIN");
        } else {
            log.info("ℹ️ Роль администратора уже существует");
        }
    }

    private void createGuestRole() {
        boolean adminRoleExists = roleService.getAllRoles().stream()
                .anyMatch(role -> "GUEST".equalsIgnoreCase(role.getName()));

        if (!adminRoleExists) {
            Role adminRole = new Role();
            adminRole.setName("GUEST");
            adminRole.setProjectPermission(RolePermission.NO_PERMS);
            adminRole.setTaskPermission(RolePermission.NO_PERMS);
            adminRole.setRolePermission(RolePermission.NO_PERMS);
            adminRole.setUserPermission(RolePermission.NO_PERMS);

            roleService.save(adminRole);
            log.info("✅ Создана роль госля: GUEST");
        } else {
            log.info("ℹ️ Роль гостя уже существует");
        }
    }

    private void createAdminUser() {
        boolean adminUserExists = userService.findByEmail("admin@gmail.com").isPresent();
        
        if (!adminUserExists) {
            Role adminRole = roleService.getAllRoles().stream()
                .filter(role -> "ADMIN".equalsIgnoreCase(role.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Роль администратора не найдена"));
            
            String plainPassword = "123";
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
            
            User adminUser = new User();
            adminUser.setName("Администратор");
            adminUser.setSureName("Системы");
            adminUser.setPatronymic("COORDIS");
            adminUser.setEmail("admin@gmail.com");
            adminUser.setPassword(hashedPassword);
            adminUser.setRole(adminRole);
            adminUser.setCreatedAt(LocalDateTime.now());
            
            userService.save(adminUser);
            log.info("✅ Создан пользователь-администратор: admin@gmail.com");
            log.warn("⚠️  Пароль по умолчанию: 123 (рекомендуется сменить после первого входа)");
        } else {
            log.info("ℹ️ Пользователь-администратор уже существует");
        }
    }
}