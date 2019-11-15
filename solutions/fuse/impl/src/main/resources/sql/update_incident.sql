UPDATE incident 
SET latitude = COALESCE(:#${body['body']['incident']['lat']}, latitude),
    longitude = COALESCE(:#${body['body']['incident']['lon']}, longitude),
    number_of_people = COALESCE(:#${body['body']['incident']['numberOfPeople']}, number_of_people),
    medical_needed = COALESCE(:#${body['body']['incident']['medicalNeeded']}, medical_needed),
    victim_name = COALESCE(:#${body['body']['incident']['victimName']}, victim_name),
    victim_phone_number = COALESCE(:#${body['body']['incident']['victimPhoneNumber']}, victim_phone_number),
    reported_time = COALESCE(:#${body['body']['incident']['reportedTime']}, reported_time),
    version = COALESCE(:#${body['body']['incident']['version']}, version),
    status = COALESCE(:#${body['body']['incident']['status']}, status)
WHERE incident_id = :#${body['body']['incident']['id']}