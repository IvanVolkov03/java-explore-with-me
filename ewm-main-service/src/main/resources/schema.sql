CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annotation VARCHAR(2000) NOT NULL,
    category_id BIGINT NOT NULL,
    confirmed_requests INTEGER DEFAULT 0,
    created_on TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    description VARCHAR(7000) NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    initiator_id BIGINT NOT NULL,
    location_lat REAL NOT NULL,
    location_lon REAL NOT NULL,
    paid BOOLEAN NOT NULL,
    participant_limit INTEGER DEFAULT 0,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN NOT NULL DEFAULT TRUE,
    state VARCHAR(50) NOT NULL,
    title VARCHAR(120) NOT NULL,
    views INTEGER DEFAULT 0,
    CONSTRAINT fk_events_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_events_user FOREIGN KEY (initiator_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS compilations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    title VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS compilation_events (
    compilation_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    PRIMARY KEY (compilation_id, event_id),
    CONSTRAINT fk_compilation_events_compilation FOREIGN KEY (compilation_id) REFERENCES compilations(id),
    CONSTRAINT fk_compilation_events_event FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    event_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_requests_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_requests_user FOREIGN KEY (requester_id) REFERENCES users(id),
    UNIQUE (event_id, requester_id)
);