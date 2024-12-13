DROP TABLE IF EXISTS edits;
DROP TABLE IF EXISTS setlists_songs;
DROP TABLE IF EXISTS setlists;
DROP TABLE IF EXISTS songs;
DROP TABLE IF EXISTS artists;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS venues;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;


CREATE TABLE roles(
	id_role SERIAL PRIMARY KEY,
	role_name VARCHAR(6) NOT NULL
);

CREATE TABLE users(
	id_user SERIAL PRIMARY KEY,
	id_role INT NOT NULL,
	username VARCHAR(60) UNIQUE NOT NULL,
	hashed_password VARCHAR(255) NOT NULL,
	FOREIGN KEY (id_role) REFERENCES roles(id_role)
);

CREATE TABLE venues(
	id_venue SERIAL PRIMARY KEY,
	venue_name VARCHAR(60) NOT NULL,
	city_name VARCHAR(60) NOT NULL
);

CREATE TABLE events(
	id_event SERIAL PRIMARY KEY,
	id_venue INT NOT NULL,
	event_name VARCHAR(60) NOT NULL,
	event_date DATE NOT NULL,
	FOREIGN KEY (id_venue) REFERENCES venues(id_venue)
);

CREATE TABLE artists(
	id_artist SERIAL PRIMARY KEY,
	artist_name VARCHAR(60) NOT NULL
);

CREATE TABLE songs(
	id_song SERIAL PRIMARY KEY,
	id_artist INT NOT NULL,
	song_name VARCHAR(60) NOT NULL,
	FOREIGN KEY (id_artist) REFERENCES artists(id_artist)
);

CREATE TABLE setlists(
	id_setlist SERIAL PRIMARY KEY,
	id_artist INT NOT NULL,
	id_event INT NOT NULL,
	setlist_name VARCHAR(60) NOT NULL,
	FOREIGN KEY (id_artist) REFERENCES artists(id_artist),
	FOREIGN KEY (id_event) REFERENCES events(id_event)
);

CREATE TABLE edits(
	id_setlist INT NOT NULL,
    date_added DATE NOT NULL,
	id_user INT NOT NULL,
    PRIMARY KEY (id_setlist, date_added),
    FOREIGN KEY (id_setlist) REFERENCES setlists(id_setlist),
	FOREIGN KEY (id_user) REFERENCES users(id_user)
);

CREATE TABLE setlists_songs(
	id_setlist INT NOT NULL,
	id_song INT NOT NULL,
	PRIMARY KEY (id_setlist, id_song),
	FOREIGN KEY (id_setlist) REFERENCES setlists(id_setlist),
	FOREIGN KEY (id_song) REFERENCES songs(id_song)
);

INSERT INTO roles (role_name) VALUES
('Member'),
('Admin')

