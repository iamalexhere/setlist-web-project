-- Aktivasi ekstensi pgcrypto jika belum diaktifkan
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Populate Users
INSERT INTO users (id_role, username, hashed_password) VALUES
    (1, 'user1', crypt('password123', gen_salt('bf'))),
    (1, 'user2', crypt('securepass', gen_salt('bf'))),
    (2, 'admin1', crypt('adminpass', gen_salt('bf'))),
    (1, 'johndoe', crypt('test123', gen_salt('bf')));
   
-- Populate Venues
INSERT INTO venues (venue_name, city_name) VALUES
    ('Stadion Utama Gelora Bung Karno', 'Jakarta'),
    ('Madison Square Garden', 'New York'),
    ('Wembley Stadium', 'London'),
	('The Forum', 'Los Angeles');

-- Populate Events
INSERT INTO events (id_venue, event_name, event_date) VALUES
    (1, 'Konser Musik Indonesia', '2024-07-20'),
    (2, 'Rock Concert 2024', '2024-08-15'),
    (3, 'Summer Music Fest', '2024-09-01'),
	(4, 'Jazz Night', '2024-07-25');

-- Populate Categories
INSERT INTO categories (category_name) VALUES
    ('Rock'),
    ('Pop'),
    ('Jazz'),
    ('Metal'),
    ('Indie'),
    ('Electronic'),
	('Acoustic');

-- Populate Artists
INSERT INTO artists (artist_name) VALUES
    ('Sheila On 7'),
    ('Coldplay'),
    ('The Beatles'),
    ('Metallica'),
	('Nirvana'),
    ('Radiohead'),
    ('John Mayer');

-- Populate Artists_Categories
INSERT INTO artists_categories (id_artist, id_category) VALUES
    (1, 2),
    (2, 2),
    (2, 1),
    (3, 1),
    (3, 2),
    (4, 4),
	(5, 1),
    (5, 5),
	(6, 1),
    (6, 5),
    (7, 7),
    (7, 3);

-- Populate Songs
INSERT INTO songs (id_artist, song_name) VALUES
    (1, 'Dan'),
    (1, 'Melompat Lebih Tinggi'),
    (2, 'Fix You'),
    (2, 'Yellow'),
    (3, 'Hey Jude'),
    (3, 'Let It Be'),
    (4, 'Enter Sandman'),
	(4, 'Master of Puppets'),
	(5, 'Smells Like Teen Spirit'),
	(5, 'Come As You Are'),
    (6, 'Creep'),
    (6, 'Paranoid Android'),
    (7, 'Gravity'),
	(7, 'Your Body is a Wonderland');

-- Populate Setlists
INSERT INTO setlists (id_artist, id_event, setlist_name, proof_filename, proof_original_filename, proof_url) VALUES
    (1, 1, 'Konser Sheila On 7 Jakarta', 'so7_jakarta.jpg', 'so7_jakarta_original.jpg', 'https://picsum.photos/200/300'),
    (2, 2, 'Coldplay MSG Concert', 'coldplay_msg.jpg', 'coldplay_msg_original.jpg', 'https://picsum.photos/200/300'),
	(3, 3, 'The Beatles Wembley', 'beatles_wembley.jpg', 'beatles_wembley_original.jpg', 'https://picsum.photos/200/300'),
	(4, 2, 'Metallica Rock Concert 2024', 'metallica_rock.jpg', 'metallica_rock_original.jpg', 'https://picsum.photos/200/300');

-- Populate Comments
INSERT INTO comments (id_setlist, id_user, comment_text) VALUES
    (1, 1, 'Keren banget konser SO7!'),
    (2, 2, 'Coldplay selalu yang terbaik!'),
    (1, 3, 'Setlistnya mantap!'),
	(3, 4, 'The Beatles legend!');

-- Populate Setlists_Songs
INSERT INTO setlists_songs (id_setlist, id_song) VALUES
    (1, 1),
    (1, 2),
    (2, 3),
    (2, 4),
    (3, 5),
    (3, 6),
	(4, 7),
    (4, 8);

-- Populate Edits
INSERT INTO edits (id_setlist, date_added, id_user, edit_description, status) VALUES
    (1, '2024-07-21', 1, 'Menambahkan beberapa lagu baru', 'pending'),
    (2, '2024-08-16', 2, 'Perbaikan urutan lagu', 'approved'),
    (3, '2024-09-02', 3, 'Update informasi venue', 'rejected'),
    (1, '2024-07-22', 4, 'Menambahkan info detail konser', 'approved');