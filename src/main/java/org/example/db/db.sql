CREATE TABLE `member` (
    id INT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    loginId VARCHAR(100) NOT NULL UNIQUE,
    loginPw VARCHAR(100) NOT NULL,
    NAME VARCHAR(100) NOT NULL,
    myMovie VARCHAR(255),
    myReview VARCHAR(255)
);

CREATE TABLE movie_info (
    id INT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    rating_1 INT DEFAULT 0,
    rating_2 INT DEFAULT 0,
    rating_3 INT DEFAULT 0,
    rating_4 INT DEFAULT 0,
    rating_5 INT DEFAULT 0,
    total_ratings FLOAT DEFAULT 0.0,
    seat_1 VARCHAR(1) DEFAULT '1',
    seat_2 VARCHAR(1) DEFAULT '2',
    seat_3 VARCHAR(1) DEFAULT '3',
    seat_4 VARCHAR(1) DEFAULT '4',
    seat_5 VARCHAR(1) DEFAULT '5',
    seat_6 VARCHAR(1) DEFAULT '6',
    seat_7 VARCHAR(1) DEFAULT '7',
    seat_8 VARCHAR(1) DEFAULT '8',
    seat_9 VARCHAR(1) DEFAULT '9',
    seat_10 VARCHAR(2) DEFAULT '10'
);


CREATE TABLE movie_seats (
    id INT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    movie_id INT UNSIGNED NOT NULL,
    seat_index INT NOT NULL,
    FOREIGN KEY (movie_id) REFERENCES movie_info(id)
);

SELECT * FROM `member`;
SELECT * FROM movie_info;

DELETE FROM `member`;
DELETE FROM movie_info;
ALTER TABLE movie_info AUTO_INCREMENT = 1;
ALTER TABLE `member` AUTO_INCREMENT = 1;