# Start from the official PostgreSQL image
FROM postgres:latest

# Install the necessary packages ¬ Should not be useful
 RUN apt-get update && apt-get install -y \
     postgresql-contrib

# Create a new database named "activities"
RUN whoami
RUN /etc/init.d/postgresql start 
RUN psql --command "CREATE DATABASE activitiesManager;" &&\
    psql --command "GRANT ALL PRIVILEGES ON DATABASE activities to postgres;"
# Copy the init.sql file into the container
COPY init.sql /
# Add a table to the "activities" database to store the activities information
RUN psql -U postgres -d activitiesManager -f init.sql
#Remove useless file
RUN rm /init.sql