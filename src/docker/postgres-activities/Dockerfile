# Start from the official PostgreSQL image
FROM postgres:latest

# Copy the init.sql file into the container
COPY ./src/docker/postgres-activities/init.sql /docker-entrypoint-initdb.d/