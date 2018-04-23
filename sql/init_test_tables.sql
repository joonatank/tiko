set search_path to keskus;
insert into kayttaja values('jotu@foo.com', 'Jotu Marjamaa', 'fooBar', 'FooBar 25', '252525252', 'user');
insert into kayttaja values('admin@bar.com', 'admin', 'adminmf', 'Somewhere 25', '252525252', 'admin');

-- Divarit
set search_path to keskus;
insert into divari values( 0, 'Lassen lehti', 'Lehtitie 123, 123321 SIPOO', NULL);
insert into divari values( 1, 'Galleinn Galle', 'Pinnintie 11, 33100 TAMPERE', 'www.gallendivari.fi');