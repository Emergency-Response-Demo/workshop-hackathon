DROP TABLE IF EXISTS incident;

CREATE TABLE incident (
  id BIGINT AUTO_INCREMENT  PRIMARY KEY,
  incident_id VARCHAR(36),
  latitude VARCHAR(20),
  longitude VARCHAR(20),
  number_of_people INT,
  medical_needed BOOLEAN,
  victim_name VARCHAR(250),
  victim_phone_number VARCHAR(20),
  reported_time BIGINT,
  version INT,
  status VARCHAR(10)
);

