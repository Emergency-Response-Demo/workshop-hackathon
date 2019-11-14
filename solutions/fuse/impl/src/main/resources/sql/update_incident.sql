UPDATE incident 
SET latitude = :#${body['body']['incident']['latitude']},
    longitude = :#${body['body']['incident']['longitude']},
    number_of_people = :#${body['body']['incident']['numberOfPeople']},
    medical_needed = :#${body['body']['incident']['medicalNeeded']},
    victim_name = :#${body['body']['incident']['victimName']},
    victim_phone_number = :#${body['body']['incident']['victimPhoneNumber']},
    reported_time = :#${body['body']['incident']['reportedTime']},
    version = :#${body['body']['incident']['version']},
    status = :#${body['body']['incident']['status']}
WHERE incident_id = :#${body['body']['incident']['incidentId']}