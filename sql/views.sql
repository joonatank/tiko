-- kaikki myynnissä olevat kirjat ja niitä myyvän divarin tiedot
SET SEARCH_PATH TO keskus;

CREATE VIEW myynnissa AS
SELECT *
FROM kirja, teos, divari
WHERE kirja.nro = teos.kirja_nro 
    AND teos.divari_nro = divari.nro
    AND teos.tilaus_nro = NULL;