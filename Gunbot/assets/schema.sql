CREATE TABLE product_categories(id INTEGER PRIMARY KEY AUTOINCREMENT, parent INT, name TEXT, url TEXT);

INSERT INTO product_categories VALUES (NULL, 0, 'AMMO', '');

INSERT INTO product_categories VALUES (NULL, 1, '.223/5.56 ', '/ammo/223-556/');
INSERT INTO product_categories VALUES (NULL, 1, ' 7.62x39', '/ammo/762x39/');
INSERT INTO product_categories VALUES (NULL, 1, '22lr', '/ammo/22lr');
INSERT INTO product_categories VALUES (NULL, 1, '9mm', '/ammo/9mm');
INSERT INTO product_categories VALUES (NULL, 1, '.308/7.62x51', '/ammo/308-762nato/');
INSERT INTO product_categories VALUES (NULL, 1, '.45 ACP', '/ammo/45acp/');
INSERT INTO product_categories VALUES (NULL, 1, '.300 AAC & Whisper', '/ammo/300-aac-whisper/');
INSERT INTO product_categories VALUES (NULL, 1, '.40 S&W', '/ammo/40sw/');
INSERT INTO product_categories VALUES (NULL, 1, '5.45x39', '/ammo/545x39/');
INSERT INTO product_categories VALUES (NULL, 1, '.380 Auto', '/380auto/');
INSERT INTO product_categories VALUES (NULL, 1, '5.7x28', '/ammo/57x28/');
INSERT INTO product_categories VALUES (NULL, 1, '10mm', '/ammo/10mm/');
INSERT INTO product_categories VALUES (NULL, 1, '30-06 ', '/ammo/30-06/');
INSERT INTO product_categories VALUES (NULL, 1, '6.8 SPC', '/ammo/68-spc/');
