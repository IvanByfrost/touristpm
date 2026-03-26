package com.travel.integration;

import com.travel.model.auth.Role;
import com.travel.model.auth.User;
import com.travel.model.master.Destination;
import com.travel.repository.RoleRepository;
import com.travel.repository.UserRepository;
import com.travel.repository.master.DestinationRepository;
import com.travel.repository.AuditLogRepository;
import com.travel.repository.catalog.PackageRepository;
import com.travel.repository.BookingRepository;
import com.travel.repository.master.AccommodationRepository;
import com.travel.repository.master.TransportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminFlowIntegrationTest extends BaseSeleniumTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID destId;

    @BeforeEach
    void setupData() {
        // Limpieza profunda en orden correcto de integridad referencial
        auditLogRepository.deleteAll();
        bookingRepository.deleteAll();
        packageRepository.deleteAll();
        accommodationRepository.deleteAll();
        transportRepository.deleteAll();
        destinationRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Crear Rol y Admin
        Role adminRole = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());

        User admin = new User();
        admin.setEmail("admin@travel.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("Admin Real");
        admin.setDocument("12345678");
        admin.setRole(adminRole);
        userRepository.save(admin);

        // Crear Destino para pruebas de tarifas
        Destination d = new Destination();
        d.setName("Destino Test Selenium");
        d.setCountry("Testland");
        d.setBasePrice(new BigDecimal("1000.00"));
        d.setTaxPercentage(new BigDecimal("15.0"));
        d.setDescription("Prueba de tarifas automáticas");
        d = destinationRepository.save(d);
        destId = d.getDestinationId();
    }

    @Test
    void testFeeManagementZeroPriceRejection() {
        loginAsAdmin();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        // 1. Navegar a Admin
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin"))).click();
        
        // 2. Esperar a que el sidebar de admin sea visible (confirmando que estamos en la vista de admin)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("admin-sidebar")));
        
        // 3. Clic en "Tarifas" en el sidebar
        WebElement destLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-section='destinations']")));
        destLink.click();
        
        // 4. Esperar carga de la sección de tarifas en el bloque principal (.admin-main)
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".admin-main h2"), "Tarifas por Destino"));
        
        // 5. Abrir modal de edición
        WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@onclick, 'openEditDestinationModal')]")));
        editBtn.click();
        
        // 6. Interactuar con el modal
        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("modal-edit-destination")));
        wait.until(ExpectedConditions.visibilityOf(modal));
        
        WebElement priceInput = driver.findElement(By.id("edit-dest-price"));
        priceInput.clear();
        priceInput.sendKeys("0");
        
        // 7. Intentar guardar
        driver.findElement(By.id("btn-save-destination")).click();
        
        // 8. Validar rechazo (Toast de error corregido anteriormente)
        waitForToast("err");
        
        // El modal debe permanecer activo (Bulma: is-active)
        assertThat(modal.getAttribute("class")).contains("is-active");
    }

    @Test
    void testFeeManagementUpdateSuccessAndAudit() {
        loginAsAdmin();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        // 1. Navegar a Admin -> Tarifas
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("admin-sidebar")));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-section='destinations']"))).click();
        
        // 2. Abrir modal y actualizar precio
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@onclick, 'openEditDestinationModal')]"))).click();
        
        WebElement priceInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("edit-dest-price")));
        priceInput.clear();
        priceInput.sendKeys("1500.50");
        
        driver.findElement(By.id("btn-save-destination")).click();
        
        // 3. Validar éxito
        waitForToast("ok");
        
        // 4. Ir a Auditoría desde el sidebar
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-section='audit']"))).click();
        
        // 5. Verificar que aparezca un registro en la tabla de auditoría del contenido principal
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".admin-main table")));
        boolean logFound = wait.until(d -> {
            String pageSource = d.getPageSource();
            return pageSource.contains("UPDATE") && pageSource.contains("Destination");
        });
        
        assertThat(logFound).isTrue();
    }
}
