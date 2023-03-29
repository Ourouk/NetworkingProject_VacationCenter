# Start from the official PostgreSQL image
FROM postgres:latest

Up# Copy the init.sql file into the container
COPY src/docker/sql-storage/init.sql /docker-entrypoint-initdb.d/