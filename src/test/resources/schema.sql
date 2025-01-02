DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS edits CASCADE;
DROP TABLE IF EXISTS setlists_songs CASCADE;
DROP TABLE IF EXISTS setlists CASCADE;
DROP TABLE IF EXISTS songs CASCADE;
DROP TABLE IF EXISTS artists_categories CASCADE;
DROP TABLE IF EXISTS artists CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS venues CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

CREATE TABLE roles(
	id_role SERIAL PRIMARY KEY,
	role_name VARCHAR(6) NOT NULL
);

CREATE TABLE users(
	id_user SERIAL PRIMARY KEY,
	id_role INT NOT NULL,
	username VARCHAR(60) UNIQUE NOT NULL,
	hashed_password VARCHAR(255) NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	is_deleted BOOLEAN DEFAULT FALSE,
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
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	is_deleted BOOLEAN DEFAULT FALSE,
	FOREIGN KEY (id_venue) REFERENCES venues(id_venue)
);

CREATE TABLE categories(
	id_category SERIAL PRIMARY KEY,
	category_name VARCHAR(15) NOT NULL
);

CREATE TABLE artists(
	id_artist SERIAL PRIMARY KEY,
	artist_name VARCHAR(60) NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE artists_categories(
	id_artist INT,
	id_category INT,
	PRIMARY KEY (id_artist, id_category),
	FOREIGN KEY (id_artist) REFERENCES artists(id_artist),
	FOREIGN KEY (id_category) REFERENCES categories(id_category)
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
	proof_filename VARCHAR(255),
	proof_original_filename VARCHAR(255),
	proof_url VARCHAR(255),
	proof_upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	is_deleted BOOLEAN DEFAULT FALSE,
	FOREIGN KEY (id_artist) REFERENCES artists(id_artist),
	FOREIGN KEY (id_event) REFERENCES events(id_event)
);

CREATE TABLE comments(
    id_comment SERIAL PRIMARY KEY,
    id_setlist INT NOT NULL,
    id_user INT NOT NULL,
    comment_text TEXT NOT NULL,
    comment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
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

CREATE TABLE edits(
	id_setlist INT NOT NULL,
    date_added DATE NOT NULL,
	id_user INT NOT NULL,
    edit_description TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected')),
    PRIMARY KEY (id_setlist, date_added),
    FOREIGN KEY (id_setlist) REFERENCES setlists(id_setlist),
	FOREIGN KEY (id_user) REFERENCES users(id_user)
);

-- Indexes for search optimization
CREATE INDEX idx_artist_name ON artists(artist_name);
CREATE INDEX idx_event_name ON events(event_name);
CREATE INDEX idx_setlist_name ON setlists(setlist_name);
CREATE INDEX idx_song_name ON songs(song_name);

