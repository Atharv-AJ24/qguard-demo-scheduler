CREATE TABLE bookings (
  id UUID PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(254) NOT NULL,
  company VARCHAR(160) NOT NULL,
  job_title VARCHAR(160) NOT NULL,
  phone VARCHAR(40),
  starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
  ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
  status VARCHAR(20) NOT NULL,
  slot_key VARCHAR(64) UNIQUE,
  management_token_hash VARCHAR(64) NOT NULL UNIQUE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  CONSTRAINT booking_time_order CHECK (ends_at > starts_at),
  CONSTRAINT booking_status_check CHECK (status IN ('CONFIRMED','CANCELLED'))
);
CREATE INDEX bookings_active_window_idx ON bookings (starts_at, ends_at) WHERE status = 'CONFIRMED';
