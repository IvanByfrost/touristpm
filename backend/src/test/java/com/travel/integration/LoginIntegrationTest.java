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
        admin.setFullName("Admin Test");
        admin.setDocument("12345678");
        admin.setRole(adminRole);
        userRepository.save(admin);
    }

    @Test
    void testAdminLoginSuccess() {
        driver.get(baseUrl + "/#/login");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Esperar a que el formulario de login sea visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));
        
        // Ingresar credenciales
        driver.findElement(By.cssSelector("input[type='email']")).sendKeys("admin@test.com");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("admin123");
        driver.findElement(By.cssSelector("#loginForm button")).click();
        
        // Verificar redirección al dashboard
        wait.until(ExpectedConditions.urlContains("#/dashboard"));
        
        // Verificar que el nav-dashboard es visible (bulma is-hidden removed by JS)
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-dashboard")));
    }

    @Test
    void testLoginFailure() {
        driver.get(baseUrl + "/#/login");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));
        
        driver.findElement(By.cssSelector("input[type='email']")).sendKeys("admin@test.com");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("#loginForm button")).click();
        
        // Verificar que aparece el toast de error
        waitForToast("err");
        
        // El dashboard NO debe ser visible
        assertThat(driver.findElement(By.id("nav-dashboard")).isDisplayed()).isFalse();
    }
}
