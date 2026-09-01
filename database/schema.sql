CREATE TABLE IF NOT EXISTS system_users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    mobile_number VARCHAR(40),
    organization_or_company VARCHAR(160),
    primary_hub VARCHAR(160),
    department VARCHAR(120),
    role VARCHAR(60) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS vendors (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    phone VARCHAR(40),
    country VARCHAR(100),
    region VARCHAR(100),
    category VARCHAR(120),
    rating DOUBLE PRECISION,
    performance_score DOUBLE PRECISION,
    on_time_rate DOUBLE PRECISION,
    active_orders INTEGER,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS inventory (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(60) NOT NULL UNIQUE,
    item_name VARCHAR(180) NOT NULL,
    category VARCHAR(100),
    quantity INTEGER NOT NULL,
    reorder_level INTEGER NOT NULL,
    warehouse VARCHAR(160) NOT NULL,
    unit_value NUMERIC(14,2),
    capacity INTEGER,
    vendor_id BIGINT REFERENCES vendors(id),
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS shipments (
    id BIGSERIAL PRIMARY KEY,
    tracking_number VARCHAR(60) NOT NULL UNIQUE,
    origin VARCHAR(140) NOT NULL,
    destination VARCHAR(140) NOT NULL,
    status VARCHAR(30) NOT NULL,
    estimated_delivery_date TIMESTAMP,
    carrier VARCHAR(100),
    vessel VARCHAR(120),
    progress INTEGER,
    declared_value NUMERIC(16,2),
    weight VARCHAR(40),
    route_priority INTEGER,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS customs_documents (
    id BIGSERIAL PRIMARY KEY,
    document_number VARCHAR(70) NOT NULL UNIQUE,
    document_type VARCHAR(80) NOT NULL,
    status VARCHAR(30) NOT NULL,
    deadline TIMESTAMP,
    notes VARCHAR(500),
    shipment_id BIGINT NOT NULL REFERENCES shipments(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS trade_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(60) NOT NULL UNIQUE,
    customer VARCHAR(160) NOT NULL,
    item_count INTEGER NOT NULL,
    total_amount NUMERIC(16,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    order_date DATE NOT NULL,
    region VARCHAR(100),
    contact VARCHAR(160),
    vendor_id BIGINT REFERENCES vendors(id),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(30) NOT NULL,
    method_name VARCHAR(160) NOT NULL,
    component_name VARCHAR(120),
    timestamp TIMESTAMP NOT NULL,
    performed_by VARCHAR(160),
    duration_ms BIGINT,
    success BOOLEAN NOT NULL,
    detail VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS supply_alerts (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(30) NOT NULL,
    category VARCHAR(50) NOT NULL,
    reference_key VARCHAR(80),
    title VARCHAR(180) NOT NULL,
    message VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES system_users(id),
    preferences_json TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_shipments_status_eta ON shipments(status, estimated_delivery_date);
CREATE INDEX IF NOT EXISTS idx_shipments_vendor ON shipments(vendor_id);
CREATE INDEX IF NOT EXISTS idx_inventory_stock ON inventory(quantity, reorder_level);
CREATE INDEX IF NOT EXISTS idx_inventory_vendor ON inventory(vendor_id);
CREATE INDEX IF NOT EXISTS idx_customs_deadline ON customs_documents(status, deadline);
CREATE INDEX IF NOT EXISTS idx_orders_status_date ON trade_orders(status, order_date);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_actor ON audit_logs(performed_by);
CREATE INDEX IF NOT EXISTS idx_alert_status ON supply_alerts(status, created_at DESC);
