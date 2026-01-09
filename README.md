# Docker Remote Management

A simple client-server app to manage Docker remotely via CLI. Think of it as a Docker CLI that works over the network.

## What You Need

- Java 17
- Maven
- Docker (running)

The server connects to Docker using the Unix socket (`/var/run/docker.sock`), so no special Docker configuration needed.

## Quick Start

**Build both projects:**
```bash
cd server && mvn clean compile
cd ../client && mvn clean compile
```

**Run the server:**
```bash
cd server
mvn exec:java
```

**Run the client (in another terminal):**
```bash
cd client
mvn exec:java
```

You should see `docker>` prompt. That's it!

## Available Commands

- `images` - List all Docker images
- `ps` - List running containers
- `ps -a` - List all containers
- `pull <image>` - Download an image
- `run <image>` - Create and start a container
- `stop <id>` - Stop a container
- `rm <id>` - Remove a container
- `rmi <id>` - Remove an image
- `login` - Authenticate with Docker registry
- `exit` or `quit` - Disconnect

## How It Works

The client sends JSON commands over TCP (port 9090) to the server. The server talks to Docker and sends back formatted results. Long operations like `pull` and `run` happen in the background so you don't have to wait.

## Troubleshooting

**Can't connect?** Make sure the server is running first.

**Docker errors?** Check that Docker is running (`docker ps`) and your user has permissions.

**Build issues?** Make sure Java 17 is installed and Maven can download dependencies.

## Architecture

- Client connects to server on port 9090
- Server uses Unix socket to talk to Docker
- JSON protocol for client-server communication
- Each client gets its own thread on the server
