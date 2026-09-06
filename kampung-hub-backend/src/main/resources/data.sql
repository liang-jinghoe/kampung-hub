-- BE-Req-3: Initial SQL Seed Records for Multi-Tenant Database Simulation

-- Seed Neighborhoods
INSERT INTO neighborhoods (id, name, property_type, subscription_tier, subscription_status, max_allowed_units, created_at)
VALUES ('nh-usj4-001', 'Taman USJ 4', 'LANDED', 'PREMIUM_LANDED', 'ACTIVE', 350, CURRENT_TIMESTAMP());

INSERT INTO neighborhoods (id, name, property_type, subscription_tier, subscription_status, max_allowed_units, created_at)
VALUES ('nh-ss15-002', 'SS15 Condominium', 'HIGH_RISE', 'BASIC_LANDED', 'ACTIVE', 200, CURRENT_TIMESTAMP());

-- Seed Users (Platform Identity Profiles)
-- Passwords below are BCrypt hashes for 'password123'
INSERT INTO users (user_id, email, full_name, phone_number, password, status, created_at)
VALUES ('usr-001', 'ahmad@example.com', 'Ahmad Zulkifli', '012-3456789', '$2a$10$wcwWIdbKe0TjpwB2XvgzQeXRz7roRmY2azJ.WV8mSmVbfUCW/3mha', 'ACTIVE', CURRENT_TIMESTAMP());

INSERT INTO users (user_id, email, full_name, phone_number, password, status, created_at)
VALUES ('usr-002', 'siti@example.com', 'Siti Aminah', '012-9876543', '$2a$10$wcwWIdbKe0TjpwB2XvgzQeXRz7roRmY2azJ.WV8mSmVbfUCW/3mha', 'ACTIVE', CURRENT_TIMESTAMP());

INSERT INTO users (user_id, email, full_name, phone_number, password, status, created_at)
VALUES ('usr-003', 'chong@example.com', 'Chong Wei', '012-5555555', NULL, 'INVITED', CURRENT_TIMESTAMP());

INSERT INTO users (user_id, email, full_name, phone_number, password, status, created_at)
VALUES ('usr-guard', 'muthu@example.com', 'Guard Muthu', '012-1111111', '$2a$10$wcwWIdbKe0TjpwB2XvgzQeXRz7roRmY2azJ.WV8mSmVbfUCW/3mha', 'ACTIVE', CURRENT_TIMESTAMP());

-- Seed Memberships (Connecting Users to Neighborhoods)
INSERT INTO memberships (membership_id, neighborhood_id, user_id, unit_number, status, invitation_token, created_at)
VALUES ('mem-usj4-001', 'nh-usj4-001', 'usr-001', 'No. 12 Jalan USJ 4/1', 'ACTIVE', NULL, CURRENT_TIMESTAMP());

INSERT INTO memberships (membership_id, neighborhood_id, user_id, unit_number, status, invitation_token, created_at)
VALUES ('mem-ss15-999', 'nh-ss15-002', 'usr-001', 'A-12-05', 'ACTIVE', NULL, CURRENT_TIMESTAMP());

INSERT INTO memberships (membership_id, neighborhood_id, user_id, unit_number, status, invitation_token, created_at)
VALUES ('mem-usj4-002', 'nh-usj4-001', 'usr-002', 'No. 45 Jalan USJ 4/2', 'ACTIVE', NULL, CURRENT_TIMESTAMP());

INSERT INTO memberships (membership_id, neighborhood_id, user_id, unit_number, status, invitation_token, created_at)
VALUES ('mem-usj4-003', 'nh-usj4-001', 'usr-003', 'No. 88 Jalan USJ 4/3', 'INVITED', 'token-chong-12345', CURRENT_TIMESTAMP());

INSERT INTO memberships (membership_id, neighborhood_id, user_id, unit_number, status, invitation_token, created_at)
VALUES ('mem-usj4-guard', 'nh-usj4-001', 'usr-guard', 'GUARDHOUSE', 'ACTIVE', NULL, CURRENT_TIMESTAMP());

-- Seed Membership Roles (Coexistence modeling)
INSERT INTO membership_roles (membership_id, role) VALUES ('mem-usj4-001', 'OWNER');
INSERT INTO membership_roles (membership_id, role) VALUES ('mem-usj4-001', 'ADMIN');

INSERT INTO membership_roles (membership_id, role) VALUES ('mem-ss15-999', 'TENANT');

INSERT INTO membership_roles (membership_id, role) VALUES ('mem-usj4-002', 'RESIDENT');

INSERT INTO membership_roles (membership_id, role) VALUES ('mem-usj4-003', 'TENANT');

INSERT INTO membership_roles (membership_id, role) VALUES ('mem-usj4-guard', 'GUARD');

-- Seed Resident Vehicles
INSERT INTO resident_vehicles (vehicle_id, neighborhood_id, owner_membership_id, unit_number, plate_text, model, color, zone_mask, status, updated_at)
VALUES ('veh-001', 'nh-usj4-001', 'mem-usj4-001', 'No. 12 Jalan USJ 4/1', 'VHM8807', 'Perodua Myvi', 'SILVER', 'Jalan 4/1', 'ACTIVE', CURRENT_TIMESTAMP());

INSERT INTO resident_vehicles (vehicle_id, neighborhood_id, owner_membership_id, unit_number, plate_text, model, color, zone_mask, status, updated_at)
VALUES ('veh-002', 'nh-usj4-001', 'mem-usj4-002', 'No. 45 Jalan USJ 4/2', 'WYY1234', 'Proton Saga', 'WHITE', 'Jalan 4/2', 'ACTIVE', CURRENT_TIMESTAMP());

-- Seed Visitor Passes
INSERT INTO visitor_passes (pass_id, neighborhood_id, unit_number, requester_membership_id, visitor_name, visitor_plate_text, pass_token, valid_from, valid_until, status)
VALUES ('pass-001', 'nh-usj4-001', 'No. 12 Jalan USJ 4/1', 'mem-usj4-001', 'John Doe', 'WYY9900', 'vpass-token-abc12345', CURRENT_TIMESTAMP(), DATEADD('DAY', 1, CURRENT_TIMESTAMP()), 'ACTIVE');

-- Seed Access Logs
INSERT INTO access_logs (log_id, neighborhood_id, plate_text, access_type, visitor_pass_id, membership_id, verified_by_guard_id, timestamp)
VALUES ('log-001', 'nh-usj4-001', 'VHM8807', 'ENTRY', NULL, 'mem-usj4-001', 'usr-guard', CURRENT_TIMESTAMP());
