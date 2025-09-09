# Filmorate

Учебный проект с курса «Java-разработчик» от Яндекс Практикума.  
Backend-сервис для работы с пользователями и фильмами, лайками и дружбой.  
**Цель текущего этапа (Спринт 12)** — спроектировать базу данных под уже существующую функциональность и оформить ER-диаграмму с примерами SQL-запросов.

## Содержание
- [Этапы работы над проектом](#этапы-работы-над-проектом)
- [ER-диаграмма](#er-диаграмма)
- [Примеры запросов к базе данных](#примеры-запросов-к-базе-данных)

## Этапы работы над проектом
**Спринт 10**
- Каркас приложения на Spring Boot.
- Базовые операции для фильмов и пользователей.
- Централизованная обработка ошибок и валидации.

**Спринт 11**
- In-memory хранилища для фильмов и пользователей.
- Лайки фильмов и список друзей.
- Эндпоинты: популярные фильмы, друзья и общие друзья.

**Спринт 12 (текущий)**
- Доработка модели: у `Film` появились несколько жанров (M:N) и рейтинг MPA.
- В модель дружбы добавлен статус: **неподтверждённая / подтверждённая** (отражено в схеме БД).
- Создана ER-диаграмма. В README приведены примеры SQL для основных операций.

## ER-диаграмма
Диаграмма расположена в `docs/er-filmorate.png` и отображена ниже.

![ER Diagram](docs/er-filmorate.png)

**Краткое описание схемы**
- `users` — пользователи (`email`, `login` — уникальны).
- `friendships` — связь дружбы с признаком подтверждения (`is_confirmed`).
- `mpa_ratings` — справочник рейтингов MPA.
- `films` — фильмы (ограничения по дате релиза и длительности).
- `genres` — справочник жанров.
- `film_genres` — связь M:N «фильм—жанр».
- `likes` — лайки фильмов пользователями.

## Примеры запросов к базе данных

### Все фильмы с MPA и жанрами
```sql
SELECT f.id, f.name, f.description, f.release_date, f.duration,
       m.code AS mpa_code,
       COALESCE(string_agg(g.name, ', ' ORDER BY g.name), '') AS genres
FROM films f
JOIN mpa_ratings m ON m.id = f.mpa_id
LEFT JOIN film_genres fg ON fg.film_id = f.id
LEFT JOIN genres g ON g.id = fg.genre_id
GROUP BY f.id, m.code
ORDER BY f.id;
```

### Топ-N популярных фильмов по лайкам
```sql
SELECT f.*, COUNT(l.user_id) AS likes_count
FROM films f
LEFT JOIN likes l ON l.film_id = f.id
GROUP BY f.id
ORDER BY likes_count DESC, f.id
LIMIT :limit;
```

### Подтверждённые друзья пользователя
```sql
SELECT u.*
FROM friendships fr
JOIN users u ON u.id = fr.friend_id
WHERE fr.user_id = :userId AND fr.is_confirmed = TRUE
ORDER BY u.id;
```

### Общие друзья двух пользователей (только подтверждённые)
```sql
SELECT u.*
FROM friendships f1
JOIN friendships f2 ON f1.friend_id = f2.friend_id
JOIN users u ON u.id = f1.friend_id
WHERE f1.user_id = :id AND f2.user_id = :otherId
  AND f1.is_confirmed = TRUE AND f2.is_confirmed = TRUE
ORDER BY u.id;
```

### Пользователи, родившиеся не раньше 1995 года
```sql
SELECT *
FROM users
WHERE EXTRACT(YEAR FROM birthday) >= 1995
ORDER BY birthday;
```

### Фильмы, у которых не меньше двух лайков
```sql
SELECT f.id, f.name, f.description, f.release_date,
       COUNT(l.user_id) AS likes_count
FROM films AS f
LEFT JOIN likes l ON f.id = l.film_id
GROUP BY f.id, f.name, f.description, f.release_date
HAVING COUNT(l.user_id) >= 2
ORDER BY likes_count DESC;
```

### Пользователи с почтой на Яндексе
```sql
SELECT id, email, login, name
FROM users
WHERE email ILIKE '%yandex%';
```
