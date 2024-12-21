-- show user's role
SELECT
	users.username,
	roles.role_name
FROM
	roles
	INNER JOIN users
	ON roles.id_role = users.id_role;

-- show setlist's edit history
SELECT
	setlists.id_setlist,
	setlist_name,
	edits.date_added,
	users.id_user,
	users.username
FROM
	setlists
	INNER JOIN edits
	ON setlists.id_setlist = edits.id_setlist
	INNER JOIN users
	ON edits.id_user = users.id_user
WHERE
	setlists.id_setlist = 4
	-- ubah id untuk memilih setlist specific, bisa ubah menggunakan setlists.setlist_name
ORDER BY
    edits.date_added ASC;

-- show user's edit history
SELECT
	users.username,
	setlist_name,
	edits.date_added
FROM
	setlists
	INNER JOIN edits
	ON setlists.id_setlist = edits.id_setlist
	INNER JOIN users
	ON edits.id_user = users.id_user
WHERE
	users.id_user = 1
	-- ubah id untuk memilih setlist specific, bisa ubah menggunakan setlists.setlist_name
ORDER BY
    edits.date_added ASC;


-- show artist in each event
SELECT DISTINCT
	events.event_date,
	events.event_name,
	artists.artist_name
FROM
	events
	INNER JOIN setlists
	ON events.id_event = setlists.id_event
	INNER JOIN artists
	ON setlists.id_artist = artists.id_artist
WHERE
	events.event_name = 'Symphony Night';
	-- ubah event_name tergantung yang dibutuhkan, bisa menggunakan id_event juga

-- show events in a venue
SELECT
	venue_name,
	events.event_name,
	events.event_date
FROM
	events
	INNER JOIN venues
	ON events.id_venue = venues.id_venue
WHERE
	venues.venue_name = 'Madison Square Garden';
	-- ubah venue_name, bisa menggunakan id_event juga

-- show setlist
SELECT
	id_setlist,
	setlist_name,
	artists.id_artist,
	artists.artist_name,
	events.id_event,
	events.event_name
FROM
	setlists
	INNER JOIN artists
	ON setlists.id_artist = artists.id_artist
	INNER JOIN events
	ON setlists.id_event = events.id_event;

-- show song playlist per setlist
SELECT 
	setlists.id_setlist,
	setlist_name,
	songs.id_song,
	song_name,
	artist_name
FROM
	setlists_songs
	INNER JOIN setlists
	ON setlists_songs.id_setlist = setlists.id_setlist
	INNER JOIN songs
	ON setlists_songs.id_song = songs.id_song
	INNER JOIN artists
	ON songs.id_artist = artists.id_artist
WHERE
	-- change id based on param
	setlists.id_setlist = 4;

-- func show all artist based on category
SELECT
	category_name,
	artist_name
FROM
	artists
	LEFT JOIN artists_categories
	ON artists.id_artist = artists_categories.id_artist
	LEFT JOIN categories
	ON artists_categories.id_category = categories.id_category
-- delete "WHERE" for showAll
-- change the id value for a specific category
WHERE
	categories.id_category = 1;
	