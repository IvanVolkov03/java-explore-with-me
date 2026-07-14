-- Очистка таблиц перед тестами (только для тестового профиля)
DELETE FROM compilation_events;
DELETE FROM compilations;
DELETE FROM requests;
DELETE FROM events;
DELETE FROM categories;
DELETE FROM users;

ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE categories_id_seq RESTART WITH 1;
ALTER SEQUENCE events_id_seq RESTART WITH 1;
ALTER SEQUENCE compilations_id_seq RESTART WITH 1;
ALTER SEQUENCE requests_id_seq RESTART WITH 1;