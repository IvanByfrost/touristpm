package com.travel.component;

import com.travel.model.catalog.Package;
import com.travel.model.master.Accommodation;
import com.travel.model.master.Destination;
import com.travel.model.master.Transport;
import com.travel.repository.catalog.PackageRepository;
import com.travel.repository.master.AccommodationRepository;
import com.travel.repository.master.DestinationRepository;
import com.travel.repository.master.TransportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final PackageRepository packageRepository;
    private final DestinationRepository destinationRepository;
    private final AccommodationRepository accommodationRepository;
    private final TransportRepository transportRepository;

    @Override
    public void run(String... args) throws Exception {
        if (packageRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        // Destinations
        Destination cancun = destinationRepository.save(Destination.builder().name("Cancún").country("México").description("Paraíso del Caribe").build());
        Destination andes = destinationRepository.save(Destination.builder().name("Los Andes").country("Chile/Argentina").description("Aventura en la montaña").build());
        Destination safari = destinationRepository.save(Destination.builder().name("Masai Mara").country("Kenia").description("Safari salvaje").build());

        // Accommodations
        Accommodation hotel = accommodationRepository.save(Accommodation.builder()
                .name("InterContinental")
                .stars(5)
                .destination(cancun)
                .address("Zona Hotelera, Cancún")
                .build());

        // Transports
        Transport plane = transportRepository.save(Transport.builder()
                .providerCompany("Avianca")
                .transportType("Avión")
                .maxCapacity(150)
                .build());

        // Packages
        packageRepository.save(Package.builder()
                .name("Paraíso en Cancún")
                .description("5 días en resorts todo incluido con playas de arena blanca")
                .destination(cancun)
                .accommodation(hotel)
                .transport(plane)
                .totalPrice(new BigDecimal("1200.00"))
                .availableSlots(10)
                .startDate(LocalDate.now().plusDays(30))
                .endDate(LocalDate.now().plusDays(35))
                .build());

        packageRepository.save(Package.builder()
                .name("Aventura en los Andes")
                .description("Trekking y cultura en la cordillera más larga del mundo")
                .destination(andes)
                .accommodation(hotel)
                .transport(plane)
                .totalPrice(new BigDecimal("800.00"))
                .availableSlots(5)
                .startDate(LocalDate.now().plusDays(40))
                .endDate(LocalDate.now().plusDays(47))
                .build());

        packageRepository.save(Package.builder()
                .name("Safari en Kenia")
                .description("Encuentros cercanos con la vida salvaje en su hábitat natural")
                .destination(safari)
                .accommodation(hotel)
                .transport(plane)
                .totalPrice(new BigDecimal("2200.00"))
                .availableSlots(8)
                .startDate(LocalDate.now().plusDays(60))
                .endDate(LocalDate.now().plusDays(68))
                .build());

        System.out.println("✅ Base de datos inicializada con paquetes de ejemplo.");
    }
}
