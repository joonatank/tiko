-- kaikki myynnissä olevat kirjat ja niitä myyvän divarin tiedot
SET SEARCH_PATH TO keskus;

CREATE VIEW myynnissa AS
SELECT teos.nro AS nro, kirja.nro AS kirja_nro, kirja.tekija, kirja.nimi,
    kirja.tyyppi, kirja.luokka, kirja.isbn, paino, hinta,
    divari.nimi AS divari_nimi, divari.osoite
FROM kirja, teos, divari
WHERE kirja.nro = teos.kirja_nro
    AND teos.divari_nro = divari.nro
    AND teos.tilaus_nro IS NULL
ORDER BY nimi, tekija, tyyppi, luokka;

-- tilauksen painot
CREATE VIEW tilaus_paino AS
SELECT tilaus.nro, SUM(teos.paino) AS paino
FROM teos, tilaus
WHERE teos.tilaus_nro = tilaus.nro
GROUP BY tilaus.nro;
