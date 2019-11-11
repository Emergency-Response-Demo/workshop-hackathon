DROP TABLE IF EXISTS incident;
 
CREATE TABLE incident (
  id INT AUTO_INCREMENT  PRIMARY KEY,
  latitude VARCHAR(20) NOT NULL,
  longitude VARCHAR(20) NOT NULL,
  number_of_people INT NOT NULL,
  medical_needed BOOLEAN NOT NULL,
  victim_name VARCHAR(250) NOT NULL,
  victim_phone_number VARCHAR(10) NOT NULL,
  incident_id INT NOT NULL,
  reported_time INT NOT NULL,
  version INT NOT NULL,
  status VARCHAR(10) NOT NULL
);

