-- Destroy tables and schemas
--set search_path to keskus;
drop table keskus.divari cascade;
drop table keskus.tilaus_kirjat cascade;
drop table keskus.tilaus cascade;
drop table keskus.teos cascade;
drop table keskus.kirja cascade;
drop table keskus.kayttaja cascade;
drop table keskus.myy cascade;

--set search_path to div1;
drop table div1.teos cascade;
drop table div1.kirja cascade;

drop schema div1;
drop schema keskus;
