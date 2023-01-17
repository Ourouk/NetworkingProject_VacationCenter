# Start from the official PostgreSQL image
FROM postgres:latest

# Install the necessary packages ¬ Should not be useful
 RUN apt-get update

# Copy the init.sql file into the container
COPY src/docker/sql-storage/init.sql /docker-entrypoint-initdb.d/