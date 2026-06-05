# Contributing to Keycloak OIDC Spring Boot Starter

Thank you for your interest in contributing! Here's how you can help.

## Development Setup

### Prerequisites
- Java 17+
- Maven 3.8+

### Build
```bash
mvn clean verify
```

### Run Tests
```bash
mvn test
```

## Code Style

- Follow standard Java naming conventions
- Use Lombok annotations (`@Data`, `@Getter`, `@Slf4j`) consistently
- Add Javadoc to all public classes, methods, and fields
- Reference relevant RFC specifications in Javadoc where applicable

## Pull Request Process

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Write tests for your changes
4. Ensure all tests pass (`mvn clean verify`)
5. Commit with a descriptive message
6. Open a Pull Request against the `main` branch

## Commit Messages

Use clear, descriptive commit messages:
```
feat: add device code flow support
fix: handle null response body in error decoder
docs: update README with PKCE example
```

## Reporting Issues

- Use GitHub Issues
- Include Keycloak version, Spring Boot version, and Java version
- Provide a minimal reproducible example when possible

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
