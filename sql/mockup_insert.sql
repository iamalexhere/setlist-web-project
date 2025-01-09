DROP TABLE IF EXISTS edits;
DROP TABLE IF EXISTS setlists_songs;
DROP TABLE IF EXISTS setlists;
DROP TABLE IF EXISTS songs;
DROP TABLE IF EXISTS artists_categories;
DROP TABLE IF EXISTS artists;
DROP TABLE IF EXISTS categories;
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

CREATE TABLE categories(
	id_category SERIAL PRIMARY KEY,
	category_name VARCHAR(15) NOT NULL
);

CREATE TABLE artists(
	id_artist SERIAL PRIMARY KEY,
	artist_name VARCHAR(60) NOT NULL
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

INSERT INTO roles (role_name) 
VALUES
	('Member'),
	('Admin');

-- Insert users (adding more users)
INSERT INTO users (id_role, username, hashed_password)
VALUES
    (1, 'john_doe', 'hashed_password_1'),   -- Member user
    (2, 'admin_user', 'hashed_password_2'),  -- Admin user
    (1, 'jane_smith', 'hashed_password_3'),  -- Another Member user
    (1, 'mark_jones', 'hashed_password_4'),  -- Another Member user
    (2, 'admin_lee', 'hashed_password_5');   -- Another Admin user

-- Insert venues (adding more venues)
INSERT INTO venues (venue_name, city_name)
VALUES
    ('Madison Square Garden', 'New York'),
    ('The O2 Arena', 'London'),
    ('Hollywood Bowl', 'Los Angeles'),
    ('Red Rocks Amphitheatre', 'Colorado'),
    ('Sydney Opera House', 'Sydney'),
    ('Royal Albert Hall', 'London'),
    ('Staples Center', 'Los Angeles');

-- Insert more events (adding multi-artist concerts)
INSERT INTO events (id_venue, event_name, event_date)
VALUES
    (1, 'Summer Live Festival', '2024-05-01'),       -- Multiple artists at Madison Square Garden
    (2, 'Pop Legends Concert', '2024-06-15'),         -- Multiple pop artists at The O2 Arena
    (3, 'Classic Rock Night', '2024-07-20'),          -- Multiple classic rock artists at Hollywood Bowl
    (4, 'Indie Vibes Festival', '2024-08-10'),        -- Multiple indie artists at Red Rocks Amphitheatre
    (5, 'Jazz in the Park', '2024-09-05'),             -- Multiple jazz artists at Sydney Opera House
    (6, 'Symphony Night', '2024-10-01'),               -- Multiple classical artists at Royal Albert Hall
    (7, 'Electronic Music Party', '2024-11-20'),      -- Multiple electronic music artists at Staples Center
    (1, 'Rock & Roll Revival', '2024-05-10'),          -- Multiple rock artists at Madison Square Garden
    (2, 'Pop Hits Extravaganza', '2024-06-25'),        -- Multiple pop artists at The O2 Arena
    (3, 'Rock Legends Concert', '2024-07-10'),        -- Multiple rock artists at Hollywood Bowl
    (4, 'Indie Showcase', '2024-08-25'),              -- Multiple indie artists at Red Rocks Amphitheatre
    (5, 'Jazz Fusion Night', '2024-09-15'),            -- Multiple jazz artists at Sydney Opera House
    (6, 'Classical Showcase', '2024-10-10'),           -- Multiple classical artists at Royal Albert Hall
    (7, 'Techno Revolution', '2024-11-05');            -- Multiple electronic artists at Staples Center

INSERT INTO categories (category_name)
VALUES
    ('Rock'),        -- Rock artists
    ('Pop'),         -- Pop artists
    ('Classical'),   -- Classical music artists
    ('Indie'),       -- Indie artists
    ('Jazz'),        -- Jazz artists
    ('Electronic');  -- Electronic artists

-- Insert artists (adding more artists)
INSERT INTO artists (artist_name)
VALUES
    ('The Beatles'),
    ('Adele'),
    ('Elton John'),
    ('Coldplay'),
    ('Taylor Swift'),
    ('Beyoncé'),
    ('Ed Sheeran');

INSERT INTO artists_categories(id_artist, id_category)
VALUES
    (1, 1),
    (3, 1),
    (4, 1),
    (2, 2),
    (5, 2),
    (6, 2),
    (7, 2),
    (3, 3),
    (4, 4),
    (3, 5),
    (7, 6);

-- Insert songs (adding more songs)
INSERT INTO songs (id_artist, song_name)
VALUES
    (1, 'Hey Jude'),
    (1, 'Let It Be'),
    (1, 'Yesterday'),
    (1, 'Come Together'),
    (1, 'Here Comes the Sun'),
    (1, 'Eleanor Rigby'),
    (2, 'Rolling in the Deep'),
    (2, 'Someone Like You'),
    (3, 'Rocket Man'),
    (3, 'Your Song'),
    (4, 'Yellow'),
    (4, 'Viva La Vida'),
    (5, 'Shake It Off'),
    (5, 'Love Story'),
    (6, 'Crazy in Love'),
    (6, 'Formation'),
    (7, 'Shape of You'),
    (7, 'Perfect');

-- Insert setlists (adding setlist names for the new artists and events)
INSERT INTO setlists (id_artist, id_event, setlist_name)
VALUES
    (1, 1, 'The Beatles Greatest Hits'),  -- The Beatles at Rock Concert
    (2, 2, 'Adele’s Pop Classics'),       -- Adele at Pop Concert
    (3, 3, 'Elton’s Timeless Ballads'),    -- Elton John at Classical Music Night
    (4, 4, 'Coldplay Indie Vibes'),       -- Coldplay at Indie Music Festival
    (5, 5, 'Taylor Swift in the Park'),   -- Taylor Swift at Jazz in the Park
    (6, 6, 'Beyoncé’s Symphony Hits'),    -- Beyoncé at Symphony Night
    (7, 7, 'Ed Sheeran Electronic Beats'), -- Ed Sheeran at Electronic Music Party
    (4, 6, 'Coldplay Symphony Experience'), -- Coldplay at Symphony Night
    (2, 7, 'Adele’s Electronic Party');    -- Adele at Electronic Music Party

-- Insert edits (adding more edits for different setlists)
INSERT INTO edits (id_setlist, date_added, id_user)
VALUES
    (1, '2024-04-01', 1),   -- John Doe editing The Beatles' setlist
    (2, '2024-06-10', 2),   -- Admin user editing Adele's setlist
    (3, '2024-06-15', 2),   -- Admin user editing Elton John's setlist
    (4, '2024-07-05', 1),   -- John Doe editing Coldplay's setlist
    (5, '2024-08-01', 2),   -- Admin user editing Taylor Swift's setlist
    (6, '2024-09-01', 1),   -- John Doe editing Beyoncé's setlist
    (7, '2024-11-15', 1),   -- John Doe editing Ed Sheeran's setlist
    (4, '2024-07-10', 3),   -- Jane Smith editing Coldplay's setlist
    (5, '2024-08-05', 4),   -- Mark Jones editing Taylor Swift's setlist
    (6, '2024-09-03', 3),   -- Jane Smith editing Beyoncé's setlist
    (7, '2024-11-20', 4),   -- Mark Jones editing Ed Sheeran's setlist
    (1, '2024-04-15', 5),   -- Admin Lee editing The Beatles' setlist
    (2, '2024-06-20', 1),   -- John Doe editing Adele's setlist
    (3, '2024-06-18', 4),   -- Mark Jones editing Elton John's setlist
    (4, '2024-07-15', 2),   -- Admin user editing Coldplay's setlist
    (5, '2024-08-10', 3),   -- Jane Smith editing Taylor Swift's setlist
    (6, '2024-09-07', 2),   -- Admin user editing Beyoncé's setlist
    (7, '2024-11-18', 3);   -- Jane Smith editing Ed Sheeran's setlist

-- Insert setlists_songs (adding songs to the setlists)
INSERT INTO setlists_songs (id_setlist, id_song)
VALUES
    (1, 1),  -- The Beatles' setlist includes 'Hey Jude'
    (1, 2),  -- The Beatles' setlist includes 'Let It Be'
    (2, 3),  -- Adele's setlist includes 'Rolling in the Deep'
    (2, 4),  -- Adele's setlist includes 'Someone Like You'
    (3, 5),  -- Elton John's setlist includes 'Rocket Man'
    (3, 6),  -- Elton John's setlist includes 'Your Song'
    (4, 7),  -- Coldplay's setlist includes 'Yellow'
    (4, 8),  -- Coldplay's setlist includes 'Viva La Vida'
    (5, 9),  -- Taylor Swift's setlist includes 'Shake It Off'
    (5, 10), -- Taylor Swift's setlist includes 'Love Story'
    (6, 11), -- Beyoncé's setlist includes 'Crazy in Love'
    (6, 12), -- Beyoncé's setlist includes 'Formation'
    (7, 13), -- Ed Sheeran's setlist includes 'Shape of You'
    (7, 14); -- Ed Sheeran's setlist includes 'Perfect'
