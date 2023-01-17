CREATE TABLE activities (
        id SERIAL PRIMARY KEY,
        name VARCHAR(255),
        schedule VARCHAR(255),
        max_players INTEGER,
        min_age INTEGER,
        max_age INTEGER
    );

BEGIN;
INSERT INTO activities (name, schedule, max_players, min_age, max_age)
VALUES
  ('Football', 'Lundi, Mercredi, Vendredi', 15, 5, 12),
  ('Basket-ball', 'Mardi, Jeudi', 10, 6, 15),
  ('Baseball', 'Lundi, Mercredi', 12, 7, 13),
  ('Natation', 'Mardi, Jeudi, Samedi', 8, 4, 10),
  ('Tennis', 'Lundi, Mercredi, Vendredi', 4, 5, 14),
  ('Gymnastique', 'Mardi, Jeudi', 8, 3, 8),
  ('Football Américain', 'Lundi, Mercredi, Vendredi', 22, 7, 13),
  ('Volley-ball', 'Mardi, Jeudi', 6, 8, 15),
  ('Danse', 'Lundi, Mercredi, Vendredi', 15, 5, 10),
  ('Yoga', 'Mardi, Jeudi, Samedi', 10, 7, 12);
COMMIT;

