CREATE TABLE T_CARD (
    cardId smallint NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) primary key,
    colorId smallint NOT NULL,
    point smallint NOT NULL,
    number smallint NOT NULL default 1,
    label varchar(10) NOT NULL
);
insert into T_CARD (colorId, point, label) values (1, 1, '黑桃A');
insert into T_CARD (colorId, point, label) values (2, 1, '红心A');
insert into T_CARD (colorId, point, label) values (3, 1, '梅花A');
insert into T_CARD (colorId, point, label) values (4, 1, '方片A');

insert into T_CARD (colorId, point, label) values (1, 2, '黑桃2');
insert into T_CARD (colorId, point, label) values (2, 2, '红心2');
insert into T_CARD (colorId, point, label) values (3, 2, '梅花2');
insert into T_CARD (colorId, point, label) values (4, 2, '方片2');

insert into T_CARD (colorId, point, label) values (1, 3, '黑桃3');
insert into T_CARD (colorId, point, label) values (2, 3, '红心3');
insert into T_CARD (colorId, point, label) values (3, 3, '梅花3');
insert into T_CARD (colorId, point, label) values (4, 3, '方片3');

insert into T_CARD (colorId, point, label) values (1, 4, '黑桃4');
insert into T_CARD (colorId, point, label) values (2, 4, '红心4');
insert into T_CARD (colorId, point, label) values (3, 4, '梅花4');
insert into T_CARD (colorId, point, label) values (4, 4, '方片4');

insert into T_CARD (colorId, point, label) values (1, 5, '黑桃5');
insert into T_CARD (colorId, point, label) values (2, 5, '红心5');
insert into T_CARD (colorId, point, label) values (3, 5, '梅花5');
insert into T_CARD (colorId, point, label) values (4, 5, '方片5');

insert into T_CARD (colorId, point, label) values (1, 6, '黑桃6');
insert into T_CARD (colorId, point, label) values (2, 6, '红心6');
insert into T_CARD (colorId, point, label) values (3, 6, '梅花6');
insert into T_CARD (colorId, point, label) values (4, 6, '方片6');

insert into T_CARD (colorId, point, label) values (1, 7, '黑桃7');
insert into T_CARD (colorId, point, label) values (2, 7, '红心7');
insert into T_CARD (colorId, point, label) values (3, 7, '梅花7');
insert into T_CARD (colorId, point, label) values (4, 7, '方片7');

insert into T_CARD (colorId, point, label) values (1, 8, '黑桃8');
insert into T_CARD (colorId, point, label) values (2, 8, '红心8');
insert into T_CARD (colorId, point, label) values (3, 8, '梅花8');
insert into T_CARD (colorId, point, label) values (4, 8, '方片8');

insert into T_CARD (colorId, point, label) values (1, 9, '黑桃9');
insert into T_CARD (colorId, point, label) values (2, 9, '红心9');
insert into T_CARD (colorId, point, label) values (3, 9, '梅花9');
insert into T_CARD (colorId, point, label) values (4, 9, '方片9');

insert into T_CARD (colorId, point, label) values (1, 10, '黑桃10');
insert into T_CARD (colorId, point, label) values (2, 10, '红心10');
insert into T_CARD (colorId, point, label) values (3, 10, '梅花10');
insert into T_CARD (colorId, point, label) values (4, 10, '方片10');

insert into T_CARD (colorId, point, label) values (1, 11, '黑桃J');
insert into T_CARD (colorId, point, label) values (2, 11, '红心J');
insert into T_CARD (colorId, point, label) values (3, 11, '梅花J');
insert into T_CARD (colorId, point, label) values (4, 11, '方片J');

insert into T_CARD (colorId, point, label) values (1, 12, '黑桃Q');
insert into T_CARD (colorId, point, label) values (2, 12, '红心Q');
insert into T_CARD (colorId, point, label) values (3, 12, '梅花Q');
insert into T_CARD (colorId, point, label) values (4, 12, '方片Q');

insert into T_CARD (colorId, point, label) values (1, 13, '黑桃K');
insert into T_CARD (colorId, point, label) values (2, 13, '红心K');
insert into T_CARD (colorId, point, label) values (3, 13, '梅花K');
insert into T_CARD (colorId, point, label) values (4, 13, '方片K');

insert into T_CARD (colorId, point, label) values (5, 20, '小王');
insert into T_CARD (colorId, point, label) values (6, 20, '大王');