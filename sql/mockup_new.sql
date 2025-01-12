-- Aktivasi ekstensi pgcrypto jika belum diaktifkan
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO roles (role_name) 
VALUES
    ('Member'),
    ('Admin');
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Populate Users
INSERT INTO users (id_role, username, email, hashed_password) VALUES
    (1, 'user1', 'user1@example.com', crypt('password123', gen_salt('bf'))),
    (1, 'user2', 'user2@example.com', crypt('securepass', gen_salt('bf'))),
    (2, 'admin1', 'admin@example.com', crypt('adminpass', gen_salt('bf'))),
    (1, 'johndoe', 'john@example.com', crypt('test123', gen_salt('bf'))),
    (1, 'alice123', 'alice123@example.com', crypt('alicepass', gen_salt('bf'))),
    (1, 'bob_thebuilder', 'bob@example.com', crypt('bobpass', gen_salt('bf'))),
    (1, 'charliebrown', 'charlie@example.com', crypt('charliepass', gen_salt('bf'))),
    (2, 'superadmin', 'superadmin@example.com', crypt('superadminpass', gen_salt('bf'))),
    (1, 'david_007', 'david@example.com', crypt('davidpass', gen_salt('bf'))),
    (1, 'ellen_doe', 'ellen@example.com', crypt('ellenpass', gen_salt('bf'))),
    (1, 'frankie69', 'frankie@example.com', crypt('frankiepass', gen_salt('bf'))),
    (1, 'grace_hopper', 'grace@example.com', crypt('gracepass', gen_salt('bf'))),
    (1, 'hannah_abc', 'hannah@example.com', crypt('hannahpass', gen_salt('bf'))),
    (1, 'ian_smith', 'ian@example.com', crypt('ianpass', gen_salt('bf'))),
    (1, 'jackson_p', 'jackson@example.com', crypt('jacksonpass', gen_salt('bf'))),
    (1, 'karen_lee', 'karen@example.com', crypt('karenpass', gen_salt('bf'))),
    (1, 'lucy_harris', 'lucy@example.com', crypt('lucypass', gen_salt('bf'))),
    (1, 'matthewjones', 'matthew@example.com', crypt('matthewpass', gen_salt('bf'))),
    (2, 'admin1234', 'admin1234@example.com', crypt('admin1234pass', gen_salt('bf'))),
    (1, 'natalie_smith', 'natalie@example.com', crypt('nataliepass', gen_salt('bf'))),
    (1, 'oliver_king', 'oliver@example.com', crypt('oliverpass', gen_salt('bf'))),
    (1, 'paul_thomas', 'paul@example.com', crypt('paulpass', gen_salt('bf'))),
    (1, 'quincy_williams', 'quincy@example.com', crypt('quincypass', gen_salt('bf'))),
    (1, 'rachel_g', 'rachel@example.com', crypt('rachelpass', gen_salt('bf'))),
    (1, 'steve_davis', 'steve@example.com', crypt('stevepass', gen_salt('bf'))),
    (1, 'tony_brown', 'tony@example.com', crypt('tonypass', gen_salt('bf'))),
    (1, 'uriah_clark', 'uriah@example.com', crypt('uriahpass', gen_salt('bf'))),
    (1, 'victoria_hill', 'victoria@example.com', crypt('victoriapass', gen_salt('bf'))),
    (1, 'william_jones', 'william@example.com', crypt('williampass', gen_salt('bf'))),
    (1, 'xander_ford', 'xander@example.com', crypt('xanderpass', gen_salt('bf'))),
    (1, 'yolanda_hooper', 'yolanda@example.com', crypt('yolandapass', gen_salt('bf'))),
    (1, 'zachary_west', 'zachary@example.com', crypt('zacharypass', gen_salt('bf')));


-- Populate Venues
INSERT INTO venues (venue_name, city_name) VALUES
    ('Stadion Utama Gelora Bung Karno', 'Jakarta'),
    ('Madison Square Garden', 'New York'),
    ('Wembley Stadium', 'London'),
    ('The Forum', 'Los Angeles'),
    ('Sydney Opera House', 'Sydney'),
    ('O2 Arena', 'London');

-- Populate Events
INSERT INTO events (id_venue, event_name, event_date) VALUES
    (1, 'Sheila On 7 Live in Jakarta', '2024-01-15'),
    (2, 'Coldplay Magic Night', '2024-02-20'),
    (3, 'Beatles Tribute Night', '2024-03-10'),
    (4, 'Metallica Rock Night', '2024-04-05'),
    (5, 'Nirvana Tribute Concert', '2020-05-25'),
    (6, 'Radiohead Dreamscape', '2014-06-15'),
    (1, 'John Mayer Unplugged', '2015-07-05'),
    (2, 'Adele Evening Serenade ft Kity Perry', '2016-08-10'),
    (3, 'Ed Sheeran Live Experience', '2012-09-20'),
    (4, 'Taylor Swift Storytelling Night', '2011-10-15'),
    (5, 'Beyoncé Power Night', '2004-11-05'),
    (6, 'Drake Vibe Session', '2023-12-01'),
    (1, 'Ariana Grande Anthem Night', '2025-01-10'),
    (2, 'Bruno Mars Beat Night with Rihana', '2022-02-20'),
    (3, 'Kanye West Chronicles', '2021-03-15'),
    (4, 'Lady Gaga Glam Night', '2018-04-10'),
    (5, 'Justin Bieber Journey', '2015-05-05');


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
INSERT INTO artists (artist_name, image_url) VALUES
    ('Sheila On 7', 'https://www.google.com/imgres?q=sheila%20on%207&imgurl=https%3A%2F%2Fomong-omong.com%2Fwp-content%2Fuploads%2F2022%2F01%2FTak-berjudul8-1.jpg&imgrefurl=https%3A%2F%2Fomong-omong.com%2Fdulu-saya-benci-sheila-on-7%2F&docid=hbcCm0qWwXANyM&tbnid=q_Wru4kHyXRBPM&vet=12ahUKEwj1o7Xf_O2KAxVa4TgGHXIRE68QM3oECGsQAA..i&w=600&h=400&hcb=2&ved=2ahUKEwj1o7Xf_O2KAxVa4TgGHXIRE68QM3oECGsQAA'),
    ('Coldplay', 'https://artist99.cdn107.com/f7d/f7df8504d3756b4052c381941d684a51_xl.jpg'),
    ('The Beatles', 'https://static01.nyt.com/images/2022/02/10/opinion/10brooksPromo/10brooksPromo-mediumSquareAt3X.jpg'),
    ('Metallica', 'https://assets.promediateknologi.id/crop/0x0:0x0/750x500/webp/photo/2023/01/31/1250666745.jpg'),
    ('Nirvana', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTUMKJpoCyQjxSDXs-tickpBzEyr2MK_VWeUA&s'),
    ('Radiohead', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSLPikfCSCX5w8j4xdGXYkZAJv2YM0pPRousA&s'),
    ('John Mayer', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTVDAtMG74kxO076V5pVhNLoYJL96Szq811Ig&s'),
    ('Adele', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSdObnOFVYcRDoebt4nBXpUe-b0BlCRBatM5g&s'),
    ('Ed Sheeran', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT3pC_BKcllSBttFjRIwVzHdWs1cWeTG2AjOw&s'),
    ('Taylor Swift', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT7AaFG37vJha2O-Y_uY8abp1jqmqIuFhf3Uw&s'),
    ('Beyoncé', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQK2dZ3uU7oFEg73-Ixjwhzg1WmK6sqJP0rPg&s'),
    ('Drake', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTHWIh9zyiZJphhcF1OicwEFBJn5en3Gdm4_g&s'),
    ('Ariana Grande', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSV16b6tCgZ7AUd_bRMOpTDrrTUl4DHHxRiqQ&s'),
    ('Bruno Mars', 'https://hips.hearstapps.com/hmg-prod/images/gettyimages-134315104.jpg?resize=1200:*'),
    ('Kanye West', 'https://static.wikia.nocookie.net/vgost/images/9/9f/KanyeWest.webp/revision/latest?cb=20240220163852'),
    ('Lady Gaga', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT0Oqe5cVhhNnuVdQHMHFITzop7Ce5LLZsVeQ&s'),
    ('Justin Bieber', 'https://cdn0-production-images-kly.akamaized.net/6ZHLYoarQh-CIBBukr1HfVwAq-s=/1200x900/smart/filters:quality(75):strip_icc():format(webp)/kly-media-production/medias/2268428/original/073913300_1530691064-Justin_Bieber.jpg'),
    ('Rihanna', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQsHUovRVaCXlFL6xdbJzmPCJHget5T2GucUA&s'),
    ('Katy Perry', 'https://pyxis.nymag.com/v1/imgs/e38/ceb/3c1d8c23b513684216fcd124ddfa48d9bd-katy-perry-stonewall.rhorizontal.w700.jpg'),
    

    -- ('Sheila On 7', '/images/artists/sheila_on_7.jpg'),
    -- ('Coldplay', '/images/artists/coldplay.jpg'),
    -- ('The Beatles', '/images/artists/the_beatles'),
    -- ('Metallica', '/images/artists/metalica.jpg'),
    -- ('Nirvana', '/images/artists/nirvana.jpg'),
    -- ('Radiohead', '/images/artists/radiohead.jpg'),
    -- ('John Mayer', '/images/artists/johnmayer.jpg'),
    -- ('Adele', '/images/artists/adele.jpg'),
    -- ('Ed Sheeran', '/images/artists/ed_sheeran.jpg'),
    -- ('Taylor Swift', '/images/artists/taylor_swift.jpg'),
    -- ('Beyoncé', '/images/artists/beyonce.jpg'),
    -- ('Drake', '/images/artists/drake.jpg'),
    -- ('Ariana Grande', '/images/artists/ariana_grande.jpg'),
    -- ('Bruno Mars', '/images/artists/bruno_mars.jpg'),
    -- ('Kanye West', '/images/artists/kanye_west.jpg'),
    -- ('Lady Gaga', '/images/artists/lady_gaga.jpg'),
    -- ('Justin Bieber', 'justin_bieber.jpg'),
    -- ('Rihanna', '/images/artists/rihana.jpg'),
    -- ('Katy Perry', '/images/artists/katy_perry.jpg'),
    -- ('Bernadya', '/images/artists/bernadya.jpeg');

-- Populate Artists_Categories
INSERT INTO artists_categories (id_artist, id_category) VALUES
    (1, 2),  -- Sheila On 7 in Pop
    (2, 2),  -- Coldplay in Pop
    (2, 1),  -- Coldplay in Rock
    (3, 1),  -- The Beatles in Rock
    (3, 2),  -- The Beatles in Pop
    (4, 4),  -- Metallica in Metal
    (5, 1),  -- Nirvana in Rock
    (5, 5),  -- Nirvana in Indie
    (6, 1),  -- Radiohead in Rock
    (6, 5),  -- Radiohead in Indie
    (7, 7),  -- John Mayer in Acoustic
    (7, 3),  -- John Mayer in Jazz
    (8, 2),  -- Adele in Pop
    (9, 5),  -- Ed Sheeran in Indie
    (9, 2),  -- Ed Sheeran in Pop
    (10, 2), -- Taylor Swift in Pop
    (10, 5), -- Taylor Swift in Indie
    (11, 2), -- Beyoncé in Pop
    (11, 3), -- Beyoncé in Jazz
    (12, 2), -- Drake in Pop
    (12, 6), -- Drake in Electronic
    (13, 2), -- Ariana Grande in Pop
    (14, 2), -- Bruno Mars in Pop
    (14, 3), -- Bruno Mars in Jazz
    (15, 2), -- Kanye West in Pop
    (15, 6), -- Kanye West in Electronic
    (16, 2), -- Lady Gaga in Pop
    (16, 6), -- Lady Gaga in Electronic
    (17, 2), -- Justin Bieber in Pop
    (18, 2), -- Rihanna in Pop
    (18, 6), -- Rihanna in Electronic
    (19, 2); -- Katy Perry in Pop

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
    (7, 'Your Body is a Wonderland'),
    (8, 'Hello'),
    (8, 'Someone Like You'),
    (9, 'Shape of You'),
    (9, 'Thinking Out Loud'),
    (10, 'Love Story'),
    (10, 'Blank Space'),
    (11, 'Halo'),
    (11, 'Single Ladies'),
    (12, 'God''s Plan'),
    (12, 'Hotline Bling'),
    (13, 'Thank U, Next'),
    (13, '7 Rings'),
    (14, 'Uptown Funk'),
    (14, 'Just the Way You Are'),
    (15, 'Stronger'),
    (15, 'Gold Digger'),
    (16, 'Poker Face'),
    (16, 'Bad Romance'),
    (17, 'Sorry'),
    (17, 'Love Yourself'),
    (18, 'Diamonds'),
    (18, 'Umbrella'),
    (19, 'Firework'),
    (19, 'Roar');

-- Populate Setlists
INSERT INTO setlists (id_artist, id_event, setlist_name, proof_filename, proof_original_filename, proof_url) VALUES
    (1, 1, 'Sheila On 7 Hits', 'so7_hits.jpg', 'so7_hits_original.jpg', 'data:image/png;base64,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'),
    (2, 2, 'Coldplay Classics', 'coldplay_classics.jpg', 'coldplay_classics_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTAQZ4sR-qt2o5XzfD38B7EOLn-G14vZJ6LLQ&s'),
    (3, 3, 'Beatles Mania', 'beatles_mania.jpg', 'beatles_mania_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQOZS4FXf_FMkbFMizB2wtQpSkf1jxh7wp5UA&s'),
    (4, 4, 'Metallica Mayhem', 'metallica_mayhem.jpg', 'metallica_mayhem_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTGj08w_nJkt_F5nIMS1rn3IJANrs_klZUDzg&s'),
    (5, 5, 'Nirvana Nostalgia', 'nirvana_nostalgia.jpg', 'nirvana_nostalgia_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTf22-tghzWDmZAaurf3O7C7y4Uklg5ckXpog&s'),
    (6, 6, 'Radiohead Reverie', 'radiohead_reverie.jpg', 'radiohead_reverie_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQZQzlfTobiL_q2hl1taz8OJhcURfF7e3bMzg&s'),
    (7, 7, 'John Mayer Acoustic', 'john_mayer_acoustic.jpg', 'john_mayer_acoustic_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSr1OG29TJDZXPpKOyI9lEeUDozRJdQHRRNzg&s'),
    (8, 8, 'Adele Ballads', 'adele_ballads.jpg', 'adele_ballads_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRJ_aDg850lOdqnDwt-c8tpKsJJoVEWJZCq4w&s'),
    (9, 9, 'Ed Sheeran Hits', 'ed_sheeran_hits.jpg', 'ed_sheeran_hits_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSmLCCmeai7Eq5wXJ1EZexyzqzcQU-cEZFEbw&s'),
    (10, 10, 'Taylor Swift Story', 'taylor_swift_story.jpg', 'taylor_swift_story_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQkiPTqxx8ll36Qbxc9qLrU5iDwRfSXbn2WeA&s'),
    (11, 11, 'Beyoncé Best', 'beyonce_best.jpg', 'beyonce_best_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR1t2RpM2sIF16oCOCummfL6W-pFzFc77sxug&s'),
    (12, 12, 'Drake Vibes', 'drake_vibes.jpg', 'drake_vibes_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTrmQVlLOOeLCEVvXD3_St2OBHGll83JnyPFQ&s'),
    (13, 13, 'Ariana Anthems', 'ariana_anthems.jpg', 'ariana_anthems_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRO0P7Q4GpBDandqQBTN-lQ9L7-S5WGKfpo6Q&s0'),
    (14, 14, 'Bruno Beat', 'bruno_beat.jpg', 'bruno_beat_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRjVNi1zqdEd0THirhGLodbYpJ9Xu2PmEj1DA&s'),
    (15, 15, 'Kanye Chronicles', 'kanye_chronicles.jpg', 'kanye_chronicles_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQNpTl4CFHvNckDsZFoRWIEdEto3u4hbh4mjA&s'),
    (16, 16, 'Gaga Glam', 'gaga_glam.jpg', 'gaga_glam_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQcOyBSXXlGtd-bNJ7nzVijO4UtIZ8mSXt3aQ&s'),
    (17, 17, 'Justin Journey', 'justin_journey.jpg', 'justin_journey_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTB-zdDh87buJhAsftkxmyXmX3J5uTSZApjPA&s'),
    (18, 14, 'Rihanna Rhythms', 'rihanna_rhythms.jpg', 'rihanna_rhythms_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQc1o_wiT6R_vTeCypAEwLe5oAI_B-tp34Esw&s'),
    (19, 8, 'Katy Perry Show', 'katy_perry_show.jpg', 'katy_perry_show_original.jpg', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRah_5nFcp6B0wuTKkiekspt0Invi7im7eJBQ&s');

-- Populate Comments
INSERT INTO comments (id_setlist, id_user, comment_text) VALUES
    (1, 1, 'Keren banget konser SO7!'),
    (2, 2, 'Coldplay selalu yang terbaik!'),
    (1, 3, 'Setlistnya mantap!'),
    (3, 4, 'The Beatles legend!'),
    (8, 5, 'Adele is the best alto woman singer in the world'),
    (9, 6, 'Ed Sheeran’s performance was amazing!'),
    (10, 7, 'Taylor Swift rocked the stage!'),
    (11, 8, 'Beyoncé was phenomenal!'),
    (12, 9, 'Drake’s energy was unmatched!'),
    (13, 10, 'Ariana Grande’s vocals were stunning!'),
    (14, 11, 'Bruno Mars brought the house down!'),
    (15, 12, 'Kanye West’s performance was iconic!'),
    (16, 13, 'Lady Gaga was electrifying!'),
    (17, 14, 'Justin Bieber had the crowd singing along!');

-- Populate Setlists_Songs
INSERT INTO setlists_songs (id_setlist, id_song) VALUES
    (1, 1),
    (1, 2),
    (2, 3),
    (2, 4),
    (3, 5),
    (3, 6),
    (4, 7),
    (4, 8),
    (5, 9),
    (5, 10),
    (6, 11),
    (6, 12),
    (7, 13),
    (7, 14),
    (8, 15),
    (8, 16),
    (9, 17),
    (9, 18),
    (10, 19),
    (10, 20),
    (11, 21),
    (11, 22),
    (12, 23),
    (12, 24),
    (13, 25),
    (13, 26),
    (14, 27),
    (14, 28),
    (15, 29),
    (15, 30),
    (16, 31),
    (16, 32),
    (17, 33),
    (17, 34),
    (18, 35),
    (18, 36),
    (19, 37),
    (19, 38);
-- Populate Edits
INSERT INTO edits (id_setlist, date_added, id_user, edit_description, status) VALUES
    (1, '2024-07-21', 1, 'Menambahkan beberapa lagu baru', 'pending'),
    (2, '2024-08-16', 2, 'Perbaikan urutan lagu', 'approved'),
    (3, '2024-09-02', 3, 'Update informasi venue', 'rejected'),
    (1, '2024-07-22', 4, 'Menambahkan info detail konser', 'approved'),
    (5, '2024-10-06', 5, 'Added encore song', 'approved'),
    (6, '2024-11-11', 6, 'Added special guest performance', 'approved');
