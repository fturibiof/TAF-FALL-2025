###########################
# Stage 1: Builder
###########################
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy parent pom.xml
COPY pom.xml .

# Copy modules
COPY backend ./backend
COPY gatling ./gatling  
# Optional: include if needed

# Download dependencies offline
RUN mvn dependency:go-offline -B

# Build all modules, skip tests for speed
RUN mvn clean package -DskipTests -B

###########################
# Stage 2: Runner
###########################
FROM eclipse-temurin:21-jre AS runner

# Set working directory
WORKDIR /app

# Copy the backend JAR from builder
ARG JAR_FILE=backend/target/backend-1.0.0-SNAPSHOT.jar
COPY --from=builder /app/${JAR_FILE} /app/app.jar

# Install Chrome for Selenium headless
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    ca-certificates \
    && wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub \
       | gpg --dearmor -o /usr/share/keyrings/google-chrome.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-chrome.gpg] \
       http://dl.google.com/linux/chrome/deb/ stable main" \
       > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

# Expose your app port
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "/app/app.jar"]
