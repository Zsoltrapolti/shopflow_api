# Contributing to ShopFlow API

Thank you for considering contributing! This document outlines the process and standards.

## Development Setup

```bash
git clone https://github.com/yourusername/shopflow-api.git
cd shopflow-api
./mvnw clean install
./mvnw spring-boot:run
```

## Branch Strategy

```
main          ← stable, production-ready
└── develop   ← integration branch
    └── feature/your-feature-name
    └── fix/issue-description
```

## Commit Convention (Conventional Commits)

```
feat(orders): add bulk order cancellation endpoint
fix(products): resolve concurrent stock deduction race condition
test(service): add edge cases for loyalty discount threshold
docs(readme): update API reference table
refactor(observer): extract base listener class
```

## Pull Request Checklist

- [ ] Tests written and passing (`./mvnw test`)
- [ ] No new Checkstyle violations
- [ ] JavaDoc on all public methods
- [ ] README updated if API surface changed
- [ ] Follows existing package structure

## Code Standards

- **Formatting**: Google Java Style Guide
- **Max line length**: 120 characters
- **Test coverage**: new features require ≥ 80% branch coverage
- **No magic numbers**: use named constants or enums

## Reporting Issues

Please include:
1. Java & Spring Boot version
2. Steps to reproduce
3. Expected vs actual behavior
4. Relevant logs
