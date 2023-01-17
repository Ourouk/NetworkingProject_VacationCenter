FROM ibm-semeru-runtimes:open-17.0.5_8-jre-jammy

#Install latest Java to run the program
RUN apt-get update -y


# Copy the project files into the container
ADD /target/Project1_VacationCenter2-1.0-SNAPSHOT.jar /opt/custom_http_server/

# Set the working directory to the project directory
WORKDIR /opt/custom_http_server/

# Run any necessary commands to setup and run the project

CMD ["java", "-jar", "/opt/custom_http_server/Project1_VacationCenter2-1.0-SNAPSHOT.jar"]