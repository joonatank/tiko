-- Luo Keskusdivarin schema
create schema keskus;
set search_path to keskus;
create table kayttaja (email varchar(255), nimi varchar(255),
    salasana varchar(255), osoite varchar(255), puh_nro varchar(12),
    kayttooikeus varchar(16),
    primary key (email) );
create table kirja (nro int, tekija varchar(255), nimi varchar(255),
    tyyppi varchar(255), luokka varchar(255), isbn varchar(13),
    primary key (nro) );
create table tilaus(nro int, tilaaja varchar(255), pvm date,
    tila varchar(16),
    foreign key (tilaaja) references kayttaja(email),
    primary key (nro) );
-- onko tilaus_kirjat turha? tilauksen kirjat saa liittämällä
-- kirja, teos, ja tilaus taulut, kun lisäsin teokselle tilaus_nro
create table tilaus_kirjat (tilaus_nro int, kirja_nro int,
    foreign key (tilaus_nro) references tilaus(nro),
    foreign key (kirja_nro) references kirja(nro),
    primary key (tilaus_nro, kirja_nro) );
create table divari (nro int, nimi varchar(255), osoite varchar(255),
    web_sivu varchar(255),
    primary key (nro) );
create table teos (nro int, paino float, kirja_nro int, hinta float,
    divari_nro int, tilaus_nro int,
    foreign key (kirja_nro) references kirja(nro),
    foreign key (divari_nro) references divari(nro),
    foreign key (tilaus_nro) references tilaus(nro),
    primary key (nro) );

-- @todo hinta avaimeksi päivitä ER,
-- hinta parempi avain,
-- sillon tilauksen kulut saa suoraan tilaus_postikulut taulusta
create table postikulut (paino float, hinta float,
    primary key (paino) );
create table tilaus_postikulut (tilaus_nro int, posti_paino float,
    foreign key (tilaus_nro) references tilaus(nro),
    foreign key (posti_paino) references postikulut(paino),
    primary key (tilaus_nro, posti_paino) );

-- Luo Divari 1 schema
create schema div1;
set search_path to div1;
-- Sama kuin keskustaulussa
create table kirja (nro int, tekija varchar(255), nimi varchar(255),
    tyyppi varchar(255), luokka varchar(255), isbn varchar(13),
    primary key (nro) );
-- Hieman muokattu keskustaulusta
create table teos (nro int, paino float, kirja_nro int,
    ostohinta float, myynti_pvm date,
    foreign key (kirja_nro) references kirja(nro),
    primary key (nro) );
