-- total edit made (past month)
SELECT COUNT(*) AS "edit made"
FROM edits
WHERE date_added >= (CURRENT_DATE - INTERVAL '1 month');

-- get users with most edits
SELECT username, activities
FROM users
	LEFT JOIN (
	SELECT id_user, COUNT(*) AS activities
	FROM edits
	GROUP BY id_user
	) AS countEdit
	ON users.id_user = countEdit.id_user
ORDER BY activities DESC
LIMIT 10;

-- note: ganti limit buat mengubah jumlah user yang ditunjukkan

-- get users with most edits (last month)
SELECT username, activities
FROM user
	LEFT JOIN (
	SELECT id_user, COUNT(*) AS activities
	FROM edits
	WHERE date_added >= (CURRENT_DATE - INTERVAL '1 month')
	GROUP BY id_user
	) AS countEdit
	ON users.id_user = countEdit.id_user
ORDER BY activities DESC
LIMIT 10;

-- get artist that got added the most in setlist
SELECT artist_name, popular
FROM artists
	LEFT JOIN (
	SELECT id_artist, COUNT(*) AS popular
	FROM setlists 
	GROUP BY id_artist
	) AS countEdit
	ON artists.id_artist = countEdit.id_artist
ORDER BY popular DESC
LIMIT 10;

-- filter num of setlist edited per month
SELECT DATE_TRUNC('month', date_insert) AS month_added, COUNT(*) AS insert_num
FROM (
    -- get date setlist inserted
	SELECT id_setlist, MIN(date_added) AS date_insert
	FROM edits
	GROUP BY id_setlist) AS setlist_inserts
GROUP BY month_added