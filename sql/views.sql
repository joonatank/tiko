-- kaikki myynnissä olevat kirjat ja niitä myyvän divarin tiedot
SET SEARCH_PATH TO keskus;

CREATE VIEW myynnissa AS
SELECT *
FROM kirja, teos, divari
WHERE kirja.nro = teos.kirja_nro 
    AND teos.divari_nro = divari.nro
    AND teos.tilaus_nro = NULL;

-- tilauksien kirjat
CREATE VIEW tilaus_kirjat AS
SELECT * 
FROM kirja, teos, tilaus
WHERE kirja.nro = teos.kirja_nro
    AND teos.tilaus_nro = tilaus.nro;

-- tilauksen painot
CREATE VIEW tilaus_paino AS
SELECT tilaus.nro, SUM(teos.paino) AS paino
FROM teos, tilaus
WHERE teos.tilaus_nro = tilaus.nro
GROUP BY tilaus.nro;