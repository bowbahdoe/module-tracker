CREATE TABLE IF NOT EXISTS architecture
(
    name    text NOT NULL PRIMARY KEY,
    comment TEXT NOT NULL DEFAULT ''
);

INSERT INTO architecture (name, comment)
VALUES ('Any', ''),
       ('aarch64', ''),
       ('arm', ''),
       ('ppc64', ''),
       ('ppc64le', ''),
       ('s390x', ''),
       ('sparcv9', ''),
       ('x64', ''),
       ('x86', '')
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS operating_system
(
    name    text NOT NULL PRIMARY KEY,
    comment TEXT NOT NULL DEFAULT ''
);

INSERT INTO operating_system (name, comment)
VALUES ('Any', ''),
       ('AIX', ''),
       ('Alpine Linux', ''),
       ('Linux', ''),
       ('macOS', ''),
       ('Solaris', ''),
       ('Windows', '')
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS maven_coordinate
(
    id          integer NOT NULL PRIMARY KEY,
    `group`     text    NOT NULL,
    artifact    text    NOT NULL,
    version     text    NOT NULL,
    jar         blob,
    sources_jar blob,
    created_at  text    NOT NULL default current_timestamp,
    updated_at  text    NOT NULL default current_timestamp,
    UNIQUE (`group`, `artifact`, `version`)
);

CREATE TABLE IF NOT EXISTS module_version
(
    id                  integer NOT NULL PRIMARY KEY,
    module_name         text    NOT NULL,
    version             text    NOT NULL,
    maven_coordinate_id integer NOT NULL,
    created_at          text    NOT NULL default current_timestamp,
    updated_at          text    NOT NULL default current_timestamp,
    FOREIGN KEY (maven_coordinate_id) REFERENCES maven_coordinate (id)
);

CREATE TABLE IF NOT EXISTS `release`
(
    id           integer NOT NULL PRIMARY KEY,
    published_at text,
    created_at   text    NOT NULL default current_timestamp,
    updated_at   text    NOT NULL default current_timestamp
);

CREATE TRIGGER release_updated_at
    AFTER UPDATE
    ON `release`
    FOR EACH ROW
    WHEN NEW.updated_at = OLD.updated_at --- this avoid infinite loop
BEGIN
    UPDATE `release`
    SET updated_at=current_timestamp
    WHERE id = OLD.id;
END;

CREATE TABLE IF NOT EXISTS release_module_version
(
    id                text NOT NULL PRIMARY KEY,
    release_id        text NOT NULL,
    module_version_id text NOT NULL,
    created_at        text NOT NULL default current_timestamp,
    updated_at        text NOT NULL default current_timestamp,
    FOREIGN KEY (release_id) REFERENCES "release" (id),
    FOREIGN KEY (module_version_id) REFERENCES module_version (id)
);

CREATE TRIGGER release_module_version_updated_at
    AFTER UPDATE
    ON release_module_version
    FOR EACH ROW
    WHEN NEW.updated_at = OLD.updated_at --- this avoid infinite loop
BEGIN
    UPDATE release_module_version
    SET updated_at=current_timestamp
    WHERE id = NEW.id;
END;

