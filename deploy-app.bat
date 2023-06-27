ECHO OFF
CALL mvnw.cmd compile jib:dockerBuild
docker run -d -p 8080:8080 --name tracket-be tracket-be
