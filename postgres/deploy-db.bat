ECHO OFF
docker build -t tracket-postgres .
docker run --env-file .env -d -p 8081:5432 --name tracket-postgres tracket-postgres
