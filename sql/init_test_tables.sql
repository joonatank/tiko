set search_path to keskus;
insert into postikulut values(.050, 1.4);
insert into postikulut values(.100, 2.1);
insert into postikulut values(.250, 2.8);
insert into postikulut values(.500, 5.6);
insert into postikulut values(1, 8.4);
insert into postikulut values(2, 14);

insert into kayttaja values('jotu@foo.com', 'Jotu Marjamaa', 'fooBar', 'FooBar 25', '252525252', 'user');
insert into kayttaja values('j@foo.bar', 'Foo Bar', 'fooBar', 'FooBar 25', '252525252', 'user');
insert into kayttaja values('admin@bar.com', 'admin', 'adminmf', 'Somewhere 25', '252525252', 'admin');

-- Divarit
set search_path to keskus;
insert into divari values( 0, 'Lassen lehti', 'Lehtitie 123, 123321 SIPOO', NULL);
insert into divari values( 1, 'Galleinn Galle', 'Pinnintie 11, 33100 TAMPERE', 'www.gallendivari.fi');

-- kirjoja
insert into kirja values(0, 'Madeleine Brent', 'Elektran tytär', 'romantiikka', 'romaani', '9155430674', 1986);
insert into kirja values(1, 'Madeleine Brent', 'Tuulentavoittelijan morsian', 'romantiikka', 'romaani', '9156381451', 1978);
insert into kirja values(2, 'Mika Waltari', 'Turms kuolematon', 'historia', 'romaani', '', 1995);
insert into kirja values(3, 'Mika Waltari', 'Komisario Palmun erehdys', 'dekkari', 'romaani', '', 1940);
insert into kirja values(4, 'Shelton Gilbert', 'Friikkilän pojat Mexicossa', 'huumori', 'sarjakuva', '', 1989);
insert into kirja values(5, 'Dale Carnegien', 'Miten saan ystäviä, menestystä, vaikutusvaltaa', 'opas', 'tietokirja', '9789510396230', 1939);

-- Teoksia
-- keskus
insert into teos values(0, 0.9, 0, 10, 0, null);
insert into teos values(1, 0.9, 0, 10, 1, null);
insert into teos values(2, 0.9, 1, 10, 0, null);
insert into teos values(3, 0.9, 2, 10, 0, null);
insert into teos values(4, 0.9, 3, 10, 1, null);
insert into teos values(5, 0.9, 4, 10, 1, null);
insert into teos values(6, 0.9, 4, 10, 0, null);
insert into teos values(7, 0.9, 5, 10, 0, null);

-- @todo
set search_path to div1
---insert into teos (0, 0.9, 0, 10,

