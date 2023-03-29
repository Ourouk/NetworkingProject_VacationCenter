FROM ubuntu:latest

# Copy the project files into the container
ADD target/auth_server /opt/auth_server/

# Set the working directory to the project directory
WORKDIR /opt/auth_server/

# Run any necessary commands to setup and run the project
CMD ["./auth_server"]