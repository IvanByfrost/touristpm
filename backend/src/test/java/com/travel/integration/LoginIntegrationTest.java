package com.travel.integration;

import com.travel.model.auth.Role;
import com.travel.repository.RoleRepository;
import com.travel.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginIntegrationTest extends BaseSeleniumTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setupData() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role adminRole = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        
        com.travel.model.auth.User admin = new com.travel.model.auth.User();
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(adminRole);
        userRepository.save(admin);
    }

    @Test
    void testAdminLoginSuccess() {
        driver.get(baseUrl + "/admin.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // CP-SEL-001: Login exitoso
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-username"))).sendKeys("admin");
        driver.findElement(By.id("login-password")).sendKeys("admin123");
        driver.findElement(By.cssSelector("#login-form button[type='submit']")).click();
        
        // Verificar que el panel de administración se activa
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#admin-panel.active")));
        
        assertThat(driver.findElement(By.className("user-name")).getText()).isEqualTo("Administrador Principal");
    }

    @Test
    void testLoginFailure() {
        driver.get(baseUrl + "/admin.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // CP-SEL-002: Login fallido
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-username"))).sendKeys("admin");
        driver.findElement(By.id("login-password")).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("#login-form button[type='submit']")).click();
        
        // Verificar que aparece una notificación de error
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("notification-error")));
        
        assertThat(driver.findElement(By.id("admin-panel")).getAttribute("class")).doesNotContain("active");
    }
}
