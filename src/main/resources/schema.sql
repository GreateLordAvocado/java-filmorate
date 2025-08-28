-- USERS
CREATE TABLE IF NOT EXISTS users (
  id           BIGSERIAL PRIMARY KEY,
  email        VARCHAR(255) NOT NULL UNIQUE,
  login        VARCHAR(64)  NOT NULL UNIQUE,
  name         VARCHAR(255) NOT NULL,
  birthday     DATE         NOT NULL,
  created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- FRIENDSHIPS
CREATE TABLE IF NOT EXISTS friendships (
  user_id      BIGINT  NOT NULL,
  friend_id    BIGINT  NOT NULL,
  is_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
  requested_at TIMESTAMP NOT NULL DEFAULT NOW(),
  confirmed_at TIMESTAMP NULL,
  CONSTRAINT pk_friendships PRIMARY KEY (user_id, friend_id),
  CONSTRAINT fk_friendships_user   FOREIGN KEY (user_id)   REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_friendships_friend FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT chk_friend_self CHECK (user_id <> friend_id)
);
CREATE INDEX IF NOT EXISTS ix_friendships_friend ON friendships(friend_id);
CREATE INDEX IF NOT EXISTS ix_friendships_user_confirmed ON friendships(user_id, is_confirmed);

-- MPA
CREATE TABLE IF NOT EXISTS mpa_ratings (
  id    INTEGER     PRIMARY KEY,
  code  VARCHAR(10) NOT NULL UNIQUE,  -- G, PG, PG-13, R, NC-17
  name  VARCHAR(50) NOT NULL
);

-- FILMS
CREATE TABLE IF NOT EXISTS films (
  id            BIGSERIAL PRIMARY KEY,
  name          VARCHAR(255) NOT NULL,
  description   VARCHAR(200),
  release_date  DATE         NOT NULL CHECK (release_date >= DATE '1895-12-28'),
  duration      INTEGER      NOT NULL CHECK (duration > 0),
  mpa_id        INTEGER      NOT NULL,
  CONSTRAINT fk_films_mpa FOREIGN KEY (mpa_id) REFERENCES mpa_ratings(id),
  CONSTRAINT uq_film_name_release UNIQUE (name, release_date)
);

-- GENRES
CREATE TABLE IF NOT EXISTS genres (
  id   INTEGER     PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE
);

-- FILM_GENRES
CREATE TABLE IF NOT EXISTS film_genres (
  film_id  BIGINT  NOT NULL,
  genre_id INTEGER NOT NULL,
  CONSTRAINT pk_film_genres PRIMARY KEY (film_id, genre_id),
  CONSTRAINT fk_fg_film  FOREIGN KEY (film_id)  REFERENCES films(id)  ON DELETE CASCADE,
  CONSTRAINT fk_fg_genre FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS ix_film_genres_genre ON film_genres(genre_id);

-- LIKES
CREATE TABLE IF NOT EXISTS likes (
  film_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  CONSTRAINT pk_likes PRIMARY KEY (film_id, user_id),
  CONSTRAINT fk_likes_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
  CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS ix_likes_film ON likes(film_id);