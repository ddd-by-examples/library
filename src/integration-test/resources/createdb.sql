CREATE TABLE IF NOT EXISTS patron_resources_database_entity (id INTEGER IDENTITY PRIMARY KEY, patron_type VARCHAR(100), patron_id UUID UNIQUE);

CREATE TABLE IF NOT EXISTS resource_on_hold_database_entity (id INTEGER IDENTITY PRIMARY KEY, resource_id UUID, patron_id UUID, library_branch_id UUID, patron_resources_database_entity INTEGER);


