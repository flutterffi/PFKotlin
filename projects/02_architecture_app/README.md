# 02 Architecture App

This project is an advanced Kotlin architecture practice app.

## Goal

Practice a small but structured app using clear boundaries:

- `DataLayer.kt`: repository and fake remote source
- `DomainLayer.kt`: use cases
- `PresentationLayer.kt`: view model, actions, and console rendering
- `AppModels.kt`: shared models and state
- `Main.kt`: composition root and app flow

## Why this project matters

This project helps you practice:

- state modeling
- repository pattern
- use case separation
- presentation state updates
- a simple action-driven UI flow

## Commands

```bash
kotlinc projects/02_architecture_app/*.kt -include-runtime -d architecture-app.jar
java -jar architecture-app.jar
```

## Suggested exercises

1. Add a new action for resetting completed topics.
2. Add a local cache timestamp to the repository.
3. Add a new dashboard field for completed minutes.
4. Write one failure flow where the remote source returns no topics.
