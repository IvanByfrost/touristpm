-- 1. USERS AND ROLES MODULE
CREATE TABLE roles (
    role_id BINARY(16) PRIMARY KEY,
    name VARCHAR(50) NOT NULL -- 'Tourist', 'Admin'
);

CREATE TABLE users (
    user_id BINARY(16) PRIMARY KEY,
    role_id BINARY(16) REFERENCES roles(role_id),
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. MASTER TABLES (DESTINATIONS, ACCOMMODATIONS, TRANSPORTS)
CREATE TABLE destinations (
    destination_id BINARY(16) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    country VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accommodations (
    accommodation_id BINARY(16) PRIMARY KEY,
    destination_id INT REFERENCES destinations(destination_id),
    name VARCHAR(100) NOT NULL,
    stars INT CHECK (stars BETWEEN 1 AND 5),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transports (
    transport_id BINARY(16) PRIMARY KEY,
    transport_type VARCHAR(50), -- 'Flight', 'Bus', 'Private Transfer'
    provider_company VARCHAR(100),
    max_capacity INT CHECK (max_capacity > 0)
);

-- 3. RATES AND PACKAGES MODULE
CREATE TABLE rates (
    rate_id BINARY(16) PRIMARY KEY,
    description VARCHAR(100),
    amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    service_type VARCHAR(50), -- 'Flight', 'Lodging', etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by INT REFERENCES users(user_id)
);

CREATE TABLE packages (
    package_id BINARY(16) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    destination_id INT REFERENCES destinations(destination_id),
    accommodation_id INT REFERENCES accommodations(accommodation_id),
    transport_id INT REFERENCES transports(transport_id),
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0),
    available_slots INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    start_date DATE,
    end_date DATE,
    CONSTRAINT check_package_dates CHECK (end_date >= start_date)
);

-- 4. BOOKINGS AND ITINERARIES MODULE
CREATE TABLE bookings (
    booking_id BINARY(16) PRIMARY KEY,
    user_id INT REFERENCES users(user_id),
    package_id INT REFERENCES packages(package_id) NULL,
    rate_id INT REFERENCES rates(rate_id) NULL,
    booking_type VARCHAR(50), -- 'Flight', 'Lodging', 'Package'
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'Pending', -- 'Active', 'Inactive', 'Cancelled'
    total_amount DECIMAL(10, 2) NOT NULL,
    details TEXT
);

CREATE TABLE itineraries (
    itinerary_id BINARY(16) PRIMARY KEY,
    booking_id INT REFERENCES bookings(booking_id),
    destination_id INT REFERENCES destinations(destination_id),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    activities TEXT,
    CONSTRAINT check_itinerary_dates CHECK (end_date >= start_date)
);

-- 5. PAYMENTS MODULE
CREATE TABLE payment_methods (
    payment_method_id BINARY(16) PRIMARY KEY,
    user_id INT REFERENCES users(user_id),
    method_type VARCHAR(50), -- 'Card', 'Transfer', 'PayPal'
    encrypted_data TEXT,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE payments (
    payment_id BINARY(16) PRIMARY KEY,
    booking_id INT REFERENCES bookings(booking_id),
    payment_method_id INT REFERENCES payment_methods(payment_method_id),
    amount_paid DECIMAL(10, 2) NOT NULL CHECK (amount_paid > 0),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_status VARCHAR(20), -- 'Completed', 'Pending', 'Failed'
    receipt_url VARCHAR(255)
);