-- Demo data for the `dev` profile (spring.flyway.locations adds classpath:db/demo).
-- Prices of dishes / setmeals are in cents; cart / order amounts are in currency units.
-- Every account below uses the password 123456 (stored as a BCrypt hash).
-- Images referenced here are bundled under src/main/resources/seed-images and served by /common/download.

-- employees
INSERT INTO employee (id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) VALUES
(5001, 'Administrator', 'admin', '$2b$10$pxaARM5NUt/m/Non46Sx5OhLDmFHtil.AGPACXwBbCVrQR9W5q33K', '13800000001', '1', '110101199001011234', 1, NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 40 DAY, 5001, 5001),
(5002, 'Alex Morgan', 'manager', '$2b$10$pxaARM5NUt/m/Non46Sx5OhLDmFHtil.AGPACXwBbCVrQR9W5q33K', '13800000002', '1', '110101198805052345', 1, NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 40 DAY, 5001, 5001),
(5003, 'Sam Rivera', 'cashier', '$2b$10$pxaARM5NUt/m/Non46Sx5OhLDmFHtil.AGPACXwBbCVrQR9W5q33K', '13800000003', '0', '110101199208083456', 1, NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 40 DAY, 5001, 5001),
(5004, 'Jordan Blake', 'former.staff', '$2b$10$pxaARM5NUt/m/Non46Sx5OhLDmFHtil.AGPACXwBbCVrQR9W5q33K', '13800000004', '1', '110101198512124567', 0, NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 40 DAY, 5001, 5001);

-- categories (type 1 = dishes, type 2 = setmeals)
INSERT INTO category (id, type, name, sort, create_time, update_time, create_user, update_user) VALUES
(1001, 1, 'Signature Mains', 1, NOW() - INTERVAL 39 DAY, NOW() - INTERVAL 39 DAY, 5001, 5001),
(1002, 1, 'Noodles & Rice', 2, NOW() - INTERVAL 39 DAY, NOW() - INTERVAL 39 DAY, 5001, 5001),
(1003, 1, 'Small Plates', 3, NOW() - INTERVAL 39 DAY, NOW() - INTERVAL 39 DAY, 5001, 5001),
(1004, 1, 'Soups', 4, NOW() - INTERVAL 39 DAY, NOW() - INTERVAL 39 DAY, 5001, 5001),
(1005, 1, 'Desserts', 5, NOW() - INTERVAL 39 DAY, NOW() - INTERVAL 39 DAY, 5001, 5001),
(1006, 1, 'Drinks', 6, NOW() - INTERVAL 39 DAY, NOW() - INTERVAL 39 DAY, 5001, 5001),
(1101, 2, 'Lunch Combos', 11, NOW() - INTERVAL 39 DAY, NOW() - INTERVAL 39 DAY, 5001, 5001),
(1102, 2, 'Family Feasts', 12, NOW() - INTERVAL 39 DAY, NOW() - INTERVAL 39 DAY, 5001, 5001);

-- dishes
INSERT INTO dish (id, name, category_id, price, code, image, description, status, sort, create_time, update_time, create_user, update_user, is_deleted) VALUES
(2001, 'Kung Pao Chicken', 1001, 1888, 'D2001', 'kung-pao-chicken.jpg', 'Wok-fried chicken thigh with roasted peanuts, dried chilli and Sichuan pepper.', 1, 1, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 7 DAY, 5001, 5002, 0),
(2002, 'Mapo Tofu', 1001, 1568, 'D2002', 'mapo-tofu.jpg', 'Silken tofu and minced pork in a fiery, numbing bean sauce.', 1, 2, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 1 DAY, 5001, 5002, 0),
(2003, 'Sweet and Sour Pork', 1001, 1988, 'D2003', 'sweet-sour-pork.jpg', 'Crispy pork shoulder tossed in a glossy tomato-vinegar glaze with pineapple.', 1, 3, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 2 DAY, 5001, 5002, 0),
(2004, 'Beef with Broccoli', 1001, 2188, 'D2004', 'beef-broccoli.jpg', 'Velvety flank steak and crisp broccoli in oyster sauce.', 1, 4, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 3 DAY, 5001, 5002, 0),
(2005, 'Peking Duck (half)', 1001, 3888, 'D2005', 'peking-duck.jpg', 'Roast duck with crackling skin, served with pancakes, scallion and hoisin.', 1, 5, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 4 DAY, 5001, 5002, 0),
(2006, 'Crispy Fried Fish', 1001, 2488, 'D2006', 'crispy-fried-fish.jpg', 'Seasonal special - temporarily off the menu.', 0, 6, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 5 DAY, 5001, 5002, 0),
(2011, 'Dan Dan Noodles', 1002, 1388, 'D2011', 'dan-dan-noodles.jpg', 'Chewy noodles, spiced pork, preserved vegetable and chilli oil.', 1, 1, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 3 DAY, 5001, 5002, 0),
(2012, 'Braised Beef Noodle Soup', 1002, 1688, 'D2012', 'beef-noodle-soup.jpg', 'Slow-braised beef shank in a star-anise broth with hand-pulled noodles.', 1, 2, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 4 DAY, 5001, 5002, 0),
(2013, 'Yangzhou Fried Rice', 1002, 1288, 'D2013', 'yangzhou-fried-rice.jpg', 'Wok-fried rice with shrimp, ham, egg and peas.', 1, 3, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 5 DAY, 5001, 5002, 0),
(2021, 'Pork & Chive Dumplings (8 pcs)', 1003, 988, 'D2021', 'pork-chive-dumplings.jpg', 'Hand-folded dumplings with juicy pork and garlic chives.', 1, 1, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 6 DAY, 5001, 5002, 0),
(2022, 'Scallion Pancake', 1003, 688, 'D2022', 'scallion-pancake.jpg', 'Flaky, layered pan-fried flatbread with scallion.', 1, 2, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 7 DAY, 5001, 5002, 0),
(2023, 'Smashed Cucumber Salad', 1003, 788, 'D2023', 'cucumber-salad.jpg', 'Cold cucumber with garlic, black vinegar and chilli oil.', 1, 3, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 1 DAY, 5001, 5002, 0),
(2024, 'Spring Rolls (4 pcs)', 1003, 788, 'D2024', 'spring-rolls.jpg', 'Crisp rolls filled with cabbage, carrot and shiitake.', 1, 4, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 2 DAY, 5001, 5002, 0),
(2031, 'Hot and Sour Soup', 1004, 788, 'D2031', 'hot-sour-soup.jpg', 'Tofu, wood-ear mushroom and egg ribbons in a peppery broth.', 1, 1, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 2 DAY, 5001, 5002, 0),
(2032, 'Wonton Soup', 1004, 1088, 'D2032', 'wonton-soup.jpg', 'Pork and shrimp wontons in a clear chicken broth.', 1, 2, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 3 DAY, 5001, 5002, 0),
(2041, 'Mango Pudding', 1005, 688, 'D2041', 'mango-pudding.jpg', 'Silky chilled pudding made with ripe mango.', 1, 1, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 5 DAY, 5001, 5002, 0),
(2042, 'Sesame Balls (4 pcs)', 1005, 788, 'D2042', 'sesame-balls.jpg', 'Deep-fried glutinous rice balls with sweet red bean paste.', 1, 2, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 6 DAY, 5001, 5002, 0),
(2051, 'Jasmine Tea', 1006, 388, 'D2051', 'jasmine-tea.jpg', 'Fragrant jasmine green tea.', 1, 1, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 1 DAY, 5001, 5002, 0),
(2052, 'Bubble Milk Tea', 1006, 588, 'D2052', 'bubble-milk-tea.jpg', 'Black milk tea with chewy tapioca pearls.', 1, 2, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 2 DAY, 5001, 5002, 0),
(2053, 'Fresh Lemonade', 1006, 488, 'D2053', 'lemonade.jpg', 'Squeezed to order, lightly sweetened.', 1, 3, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 3 DAY, 5001, 5002, 0);

-- dish flavours
INSERT INTO dish_flavor (id, dish_id, name, value, create_time, update_time, create_user, update_user, is_deleted) VALUES
(2501, 2001, 'Spice level', '["Mild","Medium","Hot"]', NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 38 DAY, 5001, 5001, 0),
(2502, 2002, 'Spice level', '["Mild","Medium","Hot"]', NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 38 DAY, 5001, 5001, 0),
(2503, 2011, 'Spice level', '["Mild","Medium","Hot"]', NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 38 DAY, 5001, 5001, 0),
(2504, 2021, 'Cooking', '["Steamed","Pan-fried"]', NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 38 DAY, 5001, 5001, 0),
(2505, 2023, 'Spice level', '["Mild","Medium","Hot"]', NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 38 DAY, 5001, 5001, 0),
(2506, 2051, 'Temperature', '["Hot","Iced"]', NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 38 DAY, 5001, 5001, 0),
(2507, 2052, 'Sweetness', '["Full","Half","Less","None"]', NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 38 DAY, 5001, 5001, 0),
(2508, 2052, 'Ice', '["Regular","Less","None"]', NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 38 DAY, 5001, 5001, 0);

-- setmeals
INSERT INTO setmeal (id, category_id, name, price, status, code, description, image, create_time, update_time, create_user, update_user, is_deleted) VALUES
(3001, 1101, 'Solo Lunch Set', 2899, 1, 'S3001', 'Kung Pao Chicken, Yangzhou Fried Rice and a Jasmine Tea.', 'combo-solo-lunch.jpg', NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 3 DAY, 5001, 5002, 0),
(3002, 1101, 'Dumpling Duo', 1899, 1, 'S3002', 'Dumplings, Hot and Sour Soup and a Bubble Milk Tea.', 'combo-dumpling-duo.jpg', NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 3 DAY, 5001, 5002, 0),
(3003, 1102, 'Family Feast for Four', 9999, 1, 'S3003', 'Peking Duck, Mapo Tofu, Beef with Broccoli, two fried rice and spring rolls.', 'combo-family-feast.jpg', NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 3 DAY, 5001, 5002, 0);

-- dishes contained in each setmeal
INSERT INTO setmeal_dish (id, setmeal_id, dish_id, name, price, copies, sort, create_time, update_time, create_user, update_user, is_deleted) VALUES
(3501, 3001, 2001, 'Kung Pao Chicken', 1888, 1, 1, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 5001, 5001, 0),
(3502, 3001, 2013, 'Yangzhou Fried Rice', 1288, 1, 2, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 5001, 5001, 0),
(3503, 3001, 2051, 'Jasmine Tea', 388, 1, 3, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 5001, 5001, 0),
(3504, 3002, 2021, 'Pork & Chive Dumplings (8 pcs)', 988, 1, 1, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 5001, 5001, 0),
(3505, 3002, 2031, 'Hot and Sour Soup', 788, 1, 2, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 5001, 5001, 0),
(3506, 3002, 2052, 'Bubble Milk Tea', 588, 1, 3, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 5001, 5001, 0),
(3507, 3003, 2005, 'Peking Duck (half)', 3888, 1, 1, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 5001, 5001, 0),
(3508, 3003, 2002, 'Mapo Tofu', 1568, 1, 2, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 5001, 5001, 0),
(3509, 3003, 2004, 'Beef with Broccoli', 2188, 1, 3, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 5001, 5001, 0),
(3510, 3003, 2013, 'Yangzhou Fried Rice', 1288, 2, 4, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 5001, 5001, 0),
(3511, 3003, 2024, 'Spring Rolls (4 pcs)', 788, 2, 5, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 5001, 5001, 0);

-- customers (log in with the phone number; the verification code is shown by the UI in dev mode)
INSERT INTO `user` (id, name, phone, sex, status) VALUES
(6001, 'Demo Customer', '13800138000', '1', 1),
(6002, 'Linda Chen', '13900139000', '0', 1),
(6003, 'Marcus Webb', '15500155000', '1', 1),
(6004, 'Priya Nair', '13700137000', '0', 1);

-- delivery addresses
INSERT INTO address_book (id, user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default, create_time, update_time, create_user, update_user, is_deleted) VALUES
(6101, 6001, 'Demo Customer', '1', '13800138000', NULL, 'Zhejiang', NULL, 'Hangzhou', NULL, 'Xihu District', '88 Wensan Road, Building 3, Room 1201', 'Home', 1, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 6001, 6001, 0),
(6102, 6001, 'Demo Customer', '1', '13800138000', NULL, 'Zhejiang', NULL, 'Hangzhou', NULL, 'Binjiang District', '1 Jiangnan Avenue, Tech Park Tower B, 5F', 'Office', 0, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 6001, 6001, 0),
(6103, 6002, 'Linda Chen', '0', '13900139000', NULL, 'Shanghai', NULL, 'Shanghai', NULL, 'Pudong District', '500 Century Avenue, Apt 18C', 'Home', 1, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 6002, 6002, 0),
(6104, 6003, 'Marcus Webb', '1', '15500155000', NULL, 'Beijing', NULL, 'Beijing', NULL, 'Haidian District', '12 Zhongguancun Street, Unit 4-702', 'Home', 1, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 6003, 6003, 0),
(6105, 6004, 'Priya Nair', '0', '13700137000', NULL, 'Guangdong', NULL, 'Shenzhen', NULL, 'Nanshan District', '9 Keji South Road, Dorm 2, Room 305', 'School', 1, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 6004, 6004, 0);

-- orders in every life-cycle state (2 awaiting dispatch, 1 dispatched, 10 completed, 1 cancelled)
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, amount, remark, phone, address, user_name, consignee) VALUES
(1850000000000001001, '1850000000000001001', 2, 6001, 6101, NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 5 HOUR, 1, 35.64, 'Please add extra napkins', '13800138000', 'ZhejiangHangzhouXihu District88 Wensan Road, Building 3, Room 1201', 'Demo Customer', 'Demo Customer'),
(1850000000000001002, '1850000000000001002', 2, 6002, 6103, NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR, 1, 25.87, NULL, '13900139000', 'ShanghaiShanghaiPudong District500 Century Avenue, Apt 18C', 'Linda Chen', 'Linda Chen'),
(1850000000000001003, '1850000000000001003', 3, 6003, 6104, NOW() - INTERVAL 20 HOUR, NOW() - INTERVAL 20 HOUR, 1, 40.64, 'Ring the bell twice', '15500155000', 'BeijingBeijingHaidian District12 Zhongguancun Street, Unit 4-702', 'Marcus Webb', 'Marcus Webb'),
(1850000000000001004, '1850000000000001004', 4, 6001, 6101, NOW() - INTERVAL 30 HOUR, NOW() - INTERVAL 30 HOUR, 1, 28.99, NULL, '13800138000', 'ZhejiangHangzhouXihu District88 Wensan Road, Building 3, Room 1201', 'Demo Customer', 'Demo Customer'),
(1850000000000001005, '1850000000000001005', 4, 6004, 6105, NOW() - INTERVAL 28 HOUR, NOW() - INTERVAL 28 HOUR, 1, 19.64, NULL, '13700137000', 'GuangdongShenzhenNanshan District9 Keji South Road, Dorm 2, Room 305', 'Priya Nair', 'Priya Nair'),
(1850000000000001006, '1850000000000001006', 4, 6001, 6102, NOW() - INTERVAL 52 HOUR, NOW() - INTERVAL 52 HOUR, 1, 26.64, 'Office lunch', '13800138000', 'ZhejiangHangzhouBinjiang District1 Jiangnan Avenue, Tech Park Tower B, 5F', 'Demo Customer', 'Demo Customer'),
(1850000000000001007, '1850000000000001007', 4, 6002, 6103, NOW() - INTERVAL 75 HOUR, NOW() - INTERVAL 75 HOUR, 1, 115.75, 'Birthday dinner, thank you!', '13900139000', 'ShanghaiShanghaiPudong District500 Century Avenue, Apt 18C', 'Linda Chen', 'Linda Chen'),
(1850000000000001008, '1850000000000001008', 4, 6003, 6104, NOW() - INTERVAL 98 HOUR, NOW() - INTERVAL 98 HOUR, 1, 51.76, NULL, '15500155000', 'BeijingBeijingHaidian District12 Zhongguancun Street, Unit 4-702', 'Marcus Webb', 'Marcus Webb'),
(1850000000000001009, '1850000000000001009', 4, 6001, 6101, NOW() - INTERVAL 120 HOUR, NOW() - INTERVAL 120 HOUR, 1, 27.64, NULL, '13800138000', 'ZhejiangHangzhouXihu District88 Wensan Road, Building 3, Room 1201', 'Demo Customer', 'Demo Customer'),
(1850000000000001010, '1850000000000001010', 5, 6004, 6105, NOW() - INTERVAL 146 HOUR, NOW() - INTERVAL 146 HOUR, 1, 15.68, 'Changed my mind', '13700137000', 'GuangdongShenzhenNanshan District9 Keji South Road, Dorm 2, Room 305', 'Priya Nair', 'Priya Nair'),
(1850000000000001011, '1850000000000001011', 4, 6002, 6103, NOW() - INTERVAL 170 HOUR, NOW() - INTERVAL 170 HOUR, 1, 55.40, NULL, '13900139000', 'ShanghaiShanghaiPudong District500 Century Avenue, Apt 18C', 'Linda Chen', 'Linda Chen'),
(1850000000000001012, '1850000000000001012', 4, 6003, 6104, NOW() - INTERVAL 200 HOUR, NOW() - INTERVAL 200 HOUR, 1, 32.76, NULL, '15500155000', 'BeijingBeijingHaidian District12 Zhongguancun Street, Unit 4-702', 'Marcus Webb', 'Marcus Webb'),
(1850000000000001013, '1850000000000001013', 4, 6001, 6101, NOW() - INTERVAL 240 HOUR, NOW() - INTERVAL 240 HOUR, 1, 37.98, NULL, '13800138000', 'ZhejiangHangzhouXihu District88 Wensan Road, Building 3, Room 1201', 'Demo Customer', 'Demo Customer'),
(1850000000000001014, '1850000000000001014', 4, 6004, 6105, NOW() - INTERVAL 290 HOUR, NOW() - INTERVAL 290 HOUR, 1, 20.76, NULL, '13700137000', 'GuangdongShenzhenNanshan District9 Keji South Road, Dorm 2, Room 305', 'Priya Nair', 'Priya Nair');

INSERT INTO order_detail (id, name, image, order_id, dish_id, setmeal_id, dish_flavor, number, amount) VALUES
(1860000000000001001, 'Kung Pao Chicken', 'kung-pao-chicken.jpg', 1850000000000001001, 2001, NULL, 'Medium', 1, 18.88),
(1860000000000001002, 'Yangzhou Fried Rice', 'yangzhou-fried-rice.jpg', 1850000000000001001, 2013, NULL, NULL, 1, 12.88),
(1860000000000001003, 'Jasmine Tea', 'jasmine-tea.jpg', 1850000000000001001, 2051, NULL, 'Iced', 1, 3.88),
(1860000000000001004, 'Dumpling Duo', 'combo-dumpling-duo.jpg', 1850000000000001002, NULL, 3002, NULL, 1, 18.99),
(1860000000000001005, 'Mango Pudding', 'mango-pudding.jpg', 1850000000000001002, 2041, NULL, NULL, 1, 6.88),
(1860000000000001006, 'Braised Beef Noodle Soup', 'beef-noodle-soup.jpg', 1850000000000001003, 2012, NULL, NULL, 2, 16.88),
(1860000000000001007, 'Scallion Pancake', 'scallion-pancake.jpg', 1850000000000001003, 2022, NULL, NULL, 1, 6.88),
(1860000000000001008, 'Solo Lunch Set', 'combo-solo-lunch.jpg', 1850000000000001004, NULL, 3001, NULL, 1, 28.99),
(1860000000000001009, 'Bubble Milk Tea', 'bubble-milk-tea.jpg', 1850000000000001005, 2052, NULL, 'Half', 2, 5.88),
(1860000000000001010, 'Spring Rolls (4 pcs)', 'spring-rolls.jpg', 1850000000000001005, 2024, NULL, NULL, 1, 7.88),
(1860000000000001011, 'Dan Dan Noodles', 'dan-dan-noodles.jpg', 1850000000000001006, 2011, NULL, 'Hot', 1, 13.88),
(1860000000000001012, 'Smashed Cucumber Salad', 'cucumber-salad.jpg', 1850000000000001006, 2023, NULL, 'Mild', 1, 7.88),
(1860000000000001013, 'Fresh Lemonade', 'lemonade.jpg', 1850000000000001006, 2053, NULL, NULL, 1, 4.88),
(1860000000000001014, 'Family Feast for Four', 'combo-family-feast.jpg', 1850000000000001007, NULL, 3003, NULL, 1, 99.99),
(1860000000000001015, 'Sesame Balls (4 pcs)', 'sesame-balls.jpg', 1850000000000001007, 2042, NULL, NULL, 2, 7.88),
(1860000000000001016, 'Peking Duck (half)', 'peking-duck.jpg', 1850000000000001008, 2005, NULL, NULL, 1, 38.88),
(1860000000000001017, 'Yangzhou Fried Rice', 'yangzhou-fried-rice.jpg', 1850000000000001008, 2013, NULL, NULL, 1, 12.88),
(1860000000000001018, 'Pork & Chive Dumplings (8 pcs)', 'pork-chive-dumplings.jpg', 1850000000000001009, 2021, NULL, 'Pan-fried', 2, 9.88),
(1860000000000001019, 'Hot and Sour Soup', 'hot-sour-soup.jpg', 1850000000000001009, 2031, NULL, NULL, 1, 7.88),
(1860000000000001020, 'Mapo Tofu', 'mapo-tofu.jpg', 1850000000000001010, 2002, NULL, 'Hot', 1, 15.68),
(1860000000000001021, 'Sweet and Sour Pork', 'sweet-sour-pork.jpg', 1850000000000001011, 2003, NULL, NULL, 1, 19.88),
(1860000000000001022, 'Yangzhou Fried Rice', 'yangzhou-fried-rice.jpg', 1850000000000001011, 2013, NULL, NULL, 2, 12.88),
(1860000000000001023, 'Fresh Lemonade', 'lemonade.jpg', 1850000000000001011, 2053, NULL, NULL, 2, 4.88),
(1860000000000001024, 'Wonton Soup', 'wonton-soup.jpg', 1850000000000001012, 2032, NULL, NULL, 1, 10.88),
(1860000000000001025, 'Beef with Broccoli', 'beef-broccoli.jpg', 1850000000000001012, 2004, NULL, NULL, 1, 21.88),
(1860000000000001026, 'Dumpling Duo', 'combo-dumpling-duo.jpg', 1850000000000001013, NULL, 3002, NULL, 2, 18.99),
(1860000000000001027, 'Braised Beef Noodle Soup', 'beef-noodle-soup.jpg', 1850000000000001014, 2012, NULL, NULL, 1, 16.88),
(1860000000000001028, 'Jasmine Tea', 'jasmine-tea.jpg', 1850000000000001014, 2051, NULL, 'Hot', 1, 3.88);
