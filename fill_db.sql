INSERT INTO user (email,username,name,about) VALUES('qwe@qwe', 'qwe', 'qwe', 'qweqwe');
INSERT INTO user (email,username,name,about) VALUES("qaz@qaz", "ываыв", "ыыыв", "мовыгк");
INSERT INTO forum (short_name, user, name) VALUES("mf", "qwe@qwe", "myforum");
INSERT INTO thread (forum, title, date, user, slug, message) VALUES("mf", "mythread", "1990-12-03", "qwe@qwe", "hey", "heyhey");
INSERT INTO post (date, forum, message, thread, user, username) VALUES ("1990-11-13", "mf", "hella", 1, "qwe@qwe", "qwe");
INSERT INTO follows (follower, followed) VALUES ("qwe@qwe", "qaz@qaz");
INSERT INTO subscribed (user, thread) VALUES ("qwe@qwe", 1);
DELETE FROM follows WHERE follower='qwe@qwe' AND followed='qaz@qaz';
