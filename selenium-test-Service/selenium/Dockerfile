# ----------------------------
# Build stage
# ----------------------------
FROM maven:3.9-eclipse-temurin-17 as builder

WORKDIR /code

COPY pom.xml .
COPY gatling ./gatling
COPY backend ./backend
COPY selenium ./selenium

WORKDIR /code/selenium
RUN mvn clean install -DskipTests

# ----------------------------
# Runner stage
# ----------------------------
FROM eclipse-temurin:17-jdk

WORKDIR /code

# Install Google Chrome and ChromeDriver
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    && wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/google-chrome.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-chrome.gpg] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

# Verify Chrome is installed
RUN google-chrome --version

# Copy Selenium jar from builder
ARG JAR_FILE=target/selenium-1.0.0-SNAPSHOT.jar
COPY --from=builder /code/selenium/${JAR_FILE} /code/app.jar

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/code/app.jar"]
