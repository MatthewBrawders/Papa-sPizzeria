/* Replace the location to where you saved the data files*/
COPY Users
FROM '/data/home/csmajs/mbraw003/PapasPizzeria/cs166_project_phase3/data/users.csv'
WITH DELIMITER ',' CSV HEADER;

COPY Items
FROM '/data/home/csmajs/mbraw003/PapasPizzeria/cs166_project_phase3/data/items.csv'
WITH DELIMITER ',' CSV HEADER;

COPY Store
FROM '/data/home/csmajs/mbraw003/PapasPizzeria/cs166_project_phase3/data/store.csv'
WITH DELIMITER ',' CSV HEADER;

COPY FoodOrder
FROM '/data/home/csmajs/mbraw003/PapasPizzeria/cs166_project_phase3/data/foodorder.csv'
WITH DELIMITER ',' CSV HEADER;

COPY ItemsInOrder
FROM '/data/home/csmajs/mbraw003/PapasPizzeria/cs166_project_phase3/data/itemsinorder.csv'
WITH DELIMITER ',' CSV HEADER;
