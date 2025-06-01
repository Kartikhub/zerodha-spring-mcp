# Kite Spring MCP Server

A Java implementation of Zerodha's Kite API as a Model Context Protocol (MCP) server built with Spring Boot. Inspired by the official [Kite MCP server](https://github.com/zerodha/kite-mcp-server) project.

## Overview

This project implements a Spring-based Model Context Protocol (MCP) server that provides tools for interacting with Zerodha's Kite trading platform. MCP is a protocol that enables AI agents to interact with external tools and services in a standardized way, extending their capabilities beyond language generation.

The server provides a communication bridge between AI agents and the Kite trading platform, allowing agents to authenticate users and access their trading information through defined tools.

## Features

- OAuth-based authentication with Zerodha Kite
- Session management for maintaining user context
- MCP tools for interacting with Kite API

## Tools Implemented

This proof-of-concept implementation provides the following tools:

1. `login` - Authenticates with Zerodha Kite via OAuth, providing a login URL for users.
2. `get_holdings` - Retrieves the user's portfolio holdings after authentication.

## Technology Stack

- **Spring Boot** (v3.2.0): Main application framework
- **Spring AI MCP WebMVC**: Implements the Model Context Protocol server capabilities (https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- **Zerodha Kite Connect SDK** (v3.5.0): Java client for accessing Zerodha's trading APIs (https://github.com/zerodha/javakiteconnect)
- **Java 21**: Target JDK version

## Running the Server

### Prerequisites

- Java 21 or higher
- Maven
- Zerodha Kite API credentials

### Environment Variables

The following environment variables need to be set:

```
KITE_API_KEY=your_kite_api_key
KITE_API_SECRET=your_kite_api_secret
```

### Build and Run

```bash
mvn clean package
java -jar target/kite-spring-mcp-1.0-SNAPSHOT.jar
```

The server will start on port 8080 by default.

## Configuring with AI Agents

This project supports Server-Sent Events (SSE) for communicating with AI agents that implement the MCP protocol.

### Using with VS Code

To use this MCP server with VS Code, add the following configuration:

#### Global settings.json

```json
"mcp": {
    "servers": {
        "kite_java": {
            "type": "sse",
            "url": "http://localhost:8080/mcp/sse"
        }
    }
},
```

#### Workspace mcp.json

Alternatively, you can add the configuration at the workspace level:

```json
"servers": {
    "kite_java": {
        "type": "sse",
        "url": "http://localhost:8080/mcp/sse"
    }
}
```

## Extending the Server

This implementation is designed to be easily extended with additional Kite API tools by:

1. Creating new tool classes in the `io.github.kartikhub.tool` package
2. Using the `@Tool` annotation to expose methods as MCP tools
3. Registering them in the `ToolConfig` class

## License

This project is released under the MIT License. See the [LICENSE](LICENSE) file for details.

This implementation is inspired by the official [Kite MCP server](https://github.com/zerodha/kite-mcp-server) by Zerodha Tech, also available under the MIT License. Attribution information is included in the [THIRD_PARTY_LICENSES](THIRD_PARTY_LICENSES) file.
