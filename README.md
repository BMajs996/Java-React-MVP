# Rekreativni mecevi

Web aplikacija za evidenciju rekreativnih meceva, terena, rezervacija, rang lista i turnira.

## Pokretanje

Backend:

```sh
./gradlew bootRun
```

Frontend:

```sh
cd frontend
npm install
npm run dev
```

Frontend radi na `http://localhost:5173`, backend na `http://localhost:8080`.

## Demo nalozi

Svi demo nalozi koriste lozinku `password`.

- `admin@demo.rs`
- `ana@demo.rs`
- `milos@demo.rs`
- `club@demo.rs`

## Export

- CSV mecevi: `http://localhost:8080/api/export/matches.csv`
- CSV rang lista: `http://localhost:8080/api/export/rankings.csv`
- PDF mecevi: `http://localhost:8080/api/export/matches.pdf`
- PDF rang lista: `http://localhost:8080/api/export/rankings.pdf`

## H2 konzola

H2 konzola je dostupna na `http://localhost:8080/h2-console`.

JDBC URL: `jdbc:h2:mem:vezba`
