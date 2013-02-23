CREATE TABLE product_categories(id INTEGER PRIMARY KEY AUTOINCREMENT, parent INT, name TEXT, url TEXT);
CREATE TABLE product_watches(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, category INT, maxPrice INT, maxParicePerRound INT, mustBeInStock INT, filters TEXT);

INSERT INTO product_categories VALUES (NULL, 0, 'AMMO', '');

INSERT INTO product_categories VALUES (NULL, 1, '.223/5.56 ', '223-556');
INSERT INTO product_categories VALUES (NULL, 1, ' 7.62x39', '762x39');
INSERT INTO product_categories VALUES (NULL, 1, '22lr', '22lr');
INSERT INTO product_categories VALUES (NULL, 1, '9mm', '9mm');
INSERT INTO product_categories VALUES (NULL, 1, '.308/7.62x51', '308-762nato');
INSERT INTO product_categories VALUES (NULL, 1, '.45 ACP', '45acp');
INSERT INTO product_categories VALUES (NULL, 1, '.300 AAC & Whisper', '300-aac-whisper');
INSERT INTO product_categories VALUES (NULL, 1, '.40 S&W', '40sw');
INSERT INTO product_categories VALUES (NULL, 1, '5.45x39', '545x39');
INSERT INTO product_categories VALUES (NULL, 1, '.380 Auto', '380auto');
INSERT INTO product_categories VALUES (NULL, 1, '5.7x28', '57x28');
INSERT INTO product_categories VALUES (NULL, 1, '10mm', '10mm');
INSERT INTO product_categories VALUES (NULL, 1, '30-06 ', '30-06');
INSERT INTO product_categories VALUES (NULL, 1, '6.8 SPC', '68-spc');
