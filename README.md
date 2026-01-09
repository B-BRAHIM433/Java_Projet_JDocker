# Docker Remote Management - Testing Guide

This project provides a client-server application for remote Docker management.

## Prerequisites

1. **Java 17** - Make sure Java 17 is installed
   ```bash
   java -version
   ```

2. **Maven** - Required for building the project
   ```bash
   mvn -version
   ```

3. **Docker** - Docker must be installed and running
   ```bash
   docker --version
   docker ps
   ```

4. **Docker Daemon TCP Access** - The server connects to Docker via TCP on port 2375
   
   **Important**: By default, Docker doesn't expose TCP port 2375 for security reasons. You have two options:
   
   **Option A: Use Docker Socket (Recommended for local testing)**
   - Modify `DockerService.java` to use Unix socket instead of TCP
   - Change: `"tcp://localhost:2375"` to use Docker's default socket
   
   **Option B: Enable Docker TCP (For remote access)**
   - Edit Docker daemon configuration (usually `/etc/docker/daemon.json`)
   - Add: `"hosts": ["unix:///var/run/docker.sock", "tcp://0.0.0.0:2375"]`
   - **Warning**: This exposes Docker without authentication - use only in secure environments!

## Building the Project

Build both server and client:

```bash
# Build server
cd server
mvn clean compile

# Build client
cd ../client
mvn clean compile
```

Or build both from the root directory:
```bash
cd server && mvn clean compile && cd ../client && mvn clean compile
```

## Running the Application

### Step 1: Start the Server

Open a terminal and run:

```bash
cd server
mvn exec:java
```

Or compile and run manually:
```bash
cd server
mvn compile
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) com.jdocker.DockerServer
```

You should see:
```
Serveur Docker démarré sur le port 9090
```

### Step 2: Start the Client

Open a **new terminal** and run:

```bash
cd client
mvn exec:java
```

Or compile and run manually:
```bash
cd client
mvn compile
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) com.jdocker.client.DockerClientCLI
```

You should see:
```
Client connecté au serveur Docker
docker> 
```

## Testing Commands

Once both server and client are running, try these commands in the client terminal:

### 1. List Docker Images
```
docker> images
```
This should return a list of available Docker images.

### 2. List Running Containers
```
docker> ps
```
Lists only containers that are currently running.

### 3. List All Containers
```
docker> ps -a
```
Lists all containers (running and stopped).

### 4. Pull a Docker Image
```
docker> pull hello-world
```
This will download the hello-world image in the background.

### 5. Run a Container
```
docker> run hello-world
```
This creates and starts a container from the hello-world image.

### 6. Stop a Container
First, get the container ID from `ps` or `ps -a`, then:
```
docker> stop <container_id>
```

### 7. Remove a Container
```
docker> rm <container_id>
```
Removes a stopped container.

### 8. Remove an Image
```
docker> rmi <image_id>
```
Removes a Docker image. Get the image ID from the `images` command.

### 9. Docker Login
```
docker> login
```
Interactive command that prompts for:
- Username
- Password
- Registry URL (optional, defaults to Docker Hub)

This authenticates with a Docker registry for pulling/pushing private images.

### 10. Exit
```
docker> exit
```
or
```
docker> quit
```
Disconnects from the server and exits the client.

## Troubleshooting

### Connection Issues

**Problem**: Client can't connect to server
- Make sure the server is running first
- Check if port 9090 is available: `netstat -an | grep 9090`
- Verify firewall settings

**Problem**: Server can't connect to Docker
- Check if Docker is running: `docker ps`
- Verify Docker daemon TCP access (see Prerequisites)
- Check Docker daemon logs: `journalctl -u docker` (on systemd systems)

### Build Issues

**Problem**: Maven dependencies not downloading
- Check internet connection
- Try: `mvn clean install -U` (force update dependencies)

**Problem**: Java version mismatch
- Ensure Java 17 is installed and set as default
- Check: `java -version` and `JAVA_HOME`

### Runtime Issues

**Problem**: "ClassNotFoundException"
- Make sure you compiled the project: `mvn compile`
- Check that all dependencies are downloaded

**Problem**: Docker operations fail
- Verify Docker daemon is accessible
- Check Docker permissions (user should be in `docker` group)
- Test Docker manually: `docker ps`, `docker images`

## Quick Test Script

You can create a simple test script:

```bash
#!/bin/bash
# test.sh

echo "Building projects..."
cd server && mvn clean compile && cd ../client && mvn clean compile

echo "Starting server in background..."
cd ../server
mvn exec:java &
SERVER_PID=$!
sleep 3

echo "Starting client..."
cd ../client
mvn exec:java

# Cleanup
kill $SERVER_PID 2>/dev/null
```

## Architecture Notes

- **Server Port**: 9090 (configurable in `DockerServer.java`)
- **Docker Daemon**: TCP port 2375 (configurable in `DockerService.java`)
- **Protocol**: JSON over TCP sockets
- **Threading**: Server handles each client in a separate thread

## Next Steps

- Add more Docker commands (ps, logs, exec, etc.)
- Implement authentication/security
- Add error handling improvements
- Create a GUI client
- Add container listing command to client

