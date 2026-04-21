CREATE TABLE IF NOT EXISTS Owner (
    owner_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50),
    phone_number VARCHAR(15) NOT NULL UNIQUE,
    email_address VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS Agent (
    agent_id INT AUTO_INCREMENT PRIMARY KEY,
    phone_number BIGINT NOT NULL UNIQUE,
    email_address VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50),
    password VARCHAR(255) NOT NULL,
    status ENUM('active','inactive') NOT NULL
);

CREATE TABLE IF NOT EXISTS CLIENT (
    client_id INT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(15) NOT NULL UNIQUE,
    email_address VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS Property (
    property_id INT PRIMARY KEY AUTO_INCREMENT,
    owner_id INT NOT NULL,
    state VARCHAR(30) NOT NULL,
    city VARCHAR(30) NOT NULL,
    locality VARCHAR(50) NOT NULL,
    pincode CHAR(6) NOT NULL,
    house_number VARCHAR(25) NOT NULL,
    unit_type ENUM('apartment','house','villa','building') NOT NULL,
    notes VARCHAR(200),
    FOREIGN KEY (owner_id) REFERENCES Owner(owner_id)
);

CREATE TABLE IF NOT EXISTS Listing (
    listing_id INT PRIMARY KEY AUTO_INCREMENT,
    property_id INT NOT NULL,
    agent_id INT,
    listing_type ENUM('sale','rent') NOT NULL,
    size_sqft DECIMAL(10,2) NOT NULL,
    bhk TINYINT NOT NULL,
    bathroom_count TINYINT NOT NULL,
    construction_year SMALLINT NOT NULL,
    furnishing ENUM('semi furnished','fully furnished','not furnished') NOT NULL,
    price DECIMAL(12,2),
    monthly_rent DECIMAL(10,2),
    security_deposit DECIMAL(10,2),
    maintenance_cost DECIMAL(8,2),
    facing ENUM('north','south','east','west') NOT NULL,
    listed_date DATE NOT NULL,
    status ENUM('active','sold','rented','closed','expired') DEFAULT 'active',
    FOREIGN KEY (property_id) REFERENCES Property(property_id),
    FOREIGN KEY (agent_id) REFERENCES Agent(agent_id)
);

CREATE TABLE IF NOT EXISTS Agent_Employment (
    employment_id INT AUTO_INCREMENT PRIMARY KEY,
    agent_id INT NOT NULL,
    date_joined DATE NOT NULL,
    date_left DATE,
    licence_number VARCHAR(20) UNIQUE,
    issued_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    FOREIGN KEY (agent_id) REFERENCES Agent(agent_id)
);

CREATE TABLE IF NOT EXISTS Sale_Transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    listing_id INT UNIQUE,
    agent_id INT NOT NULL,
    buyer_id INT NOT NULL,
    deal_date DATE NOT NULL,
    final_price INT UNSIGNED NOT NULL,
    FOREIGN KEY (listing_id) REFERENCES Listing(listing_id),
    FOREIGN KEY (agent_id) REFERENCES Agent(agent_id),
    FOREIGN KEY (buyer_id) REFERENCES CLIENT(client_id)
);

CREATE TABLE IF NOT EXISTS Rental_Transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    listing_id INT UNIQUE,
    agent_id INT NOT NULL,
    tenant_id INT NOT NULL,
    rent_start DATE NOT NULL,
    rent_end DATE NOT NULL,
    deal_date DATE NOT NULL,
    monthly_rent INT UNSIGNED NOT NULL,
    security_deposit INT UNSIGNED NOT NULL,
    status ENUM('active','completed','terminated','cancelled') NOT NULL,
    FOREIGN KEY (listing_id) REFERENCES Listing(listing_id),
    FOREIGN KEY (agent_id) REFERENCES Agent(agent_id),
    FOREIGN KEY (tenant_id) REFERENCES CLIENT(client_id)
);

CREATE TABLE IF NOT EXISTS WISHLIST (
    listing_id INT,
    client_id INT,
    date_added DATE NOT NULL,
    notes VARCHAR(255),
    PRIMARY KEY (listing_id, client_id),
    FOREIGN KEY (client_id) REFERENCES CLIENT(client_id),
    FOREIGN KEY (listing_id) REFERENCES Listing(listing_id)
);

CREATE TABLE IF NOT EXISTS CLIENT_REQUEST (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    request_type ENUM('BUY','RENT','LIST') NOT NULL,
    listing_id INT NULL,
    offered_price BIGINT NULL,
    rent_start DATE NULL,
    rent_end DATE NULL,
    target_city VARCHAR(100) NULL,
    target_locality VARCHAR(100) NULL,
    property_details TEXT NULL,
    preferred_listing_type ENUM('sale','rent') NULL,
    bhk INT NULL,
    min_budget DECIMAL(12,2) NULL,
    max_budget DECIMAL(12,2) NULL,
    property_state VARCHAR(30) NULL,
    property_pincode CHAR(6) NULL,
    house_number VARCHAR(25) NULL,
    unit_type ENUM('apartment','house','villa','building') NULL,
    size_sqft DECIMAL(10,2) NULL,
    bathroom_count TINYINT NULL,
    construction_year SMALLINT NULL,
    furnishing ENUM('semi furnished','fully furnished','not furnished') NULL,
    price DECIMAL(12,2) NULL,
    monthly_rent DECIMAL(10,2) NULL,
    security_deposit DECIMAL(10,2) NULL,
    maintenance_cost DECIMAL(8,2) NULL,
    facing ENUM('north','south','east','west') NULL,
    assigned_agent_id INT NULL,
    status ENUM('pending','assigned','approved','rejected') NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES CLIENT(client_id),
    FOREIGN KEY (listing_id) REFERENCES Listing(listing_id),
    FOREIGN KEY (assigned_agent_id) REFERENCES Agent(agent_id)
);

INSERT IGNORE INTO Agent (agent_id, phone_number, email_address, first_name, last_name, password, status) VALUES
(1, 9876543210, 'agent1@realty.com', 'Aarav', 'Sharma', 'password123', 'active'),
(2, 9876500001, 'agent2@realty.com', 'Riya', 'Mehta', 'password123', 'active'),
(3, 9876500002, 'agent3@realty.com', 'Kabir', 'Nair', 'password123', 'active');

INSERT IGNORE INTO CLIENT (client_id, phone_number, email_address, password, first_name, last_name) VALUES
(1, '9990001111', 'client1@mail.com', 'password123', 'Neha', 'Kapoor'),
(2, '9990001112', 'client2@mail.com', 'password123', 'Rahul', 'Joshi');

INSERT IGNORE INTO Owner (owner_id, first_name, last_name, phone_number, email_address, password) VALUES
(1, 'Sanjay', 'Verma', '8881002001', 'owner1@mail.com', 'password123'),
(2, 'Pooja', 'Iyer', '8881002002', 'owner2@mail.com', 'password123');

INSERT IGNORE INTO Property (property_id, owner_id, state, city, locality, pincode, house_number, unit_type, notes) VALUES
(1, 1, 'Assam', 'Guwahati', 'Beltola', '781028', 'H-12', 'apartment', 'Near market'),
(2, 2, 'Assam', 'Guwahati', 'Khanapara', '781022', 'V-08', 'villa', 'Garden facing'),
(3, 1, 'Assam', 'Guwahati', 'Dispur', '781006', 'B-24', 'house', 'Corner property');

INSERT IGNORE INTO Listing (
    listing_id, property_id, agent_id, listing_type, size_sqft, bhk, bathroom_count,
    construction_year, furnishing, price, monthly_rent, security_deposit,
    maintenance_cost, facing, listed_date, status
) VALUES
(1, 1, 1, 'sale', 1450.00, 3, 2, 2018, 'semi furnished', 7200000.00, NULL, NULL, 2500.00, 'east', CURDATE(), 'active'),
(2, 2, 2, 'rent', 2100.00, 4, 3, 2016, 'fully furnished', NULL, 42000.00, 80000.00, 3500.00, 'north', CURDATE(), 'active'),
(3, 3, 3, 'rent', 980.00, 2, 2, 2020, 'not furnished', NULL, 18000.00, 35000.00, 1500.00, 'west', CURDATE(), 'active');
