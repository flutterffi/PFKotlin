# Advanced

This folder moves from syntax drills to more idiomatic Kotlin practice.

## Goal

- model data more clearly
- work with transformations and result types
- practice parser-style thinking

## Study order

1. `01_task_pipeline.kt`
2. `02_result_and_sequences.kt`
3. `03_parser_error_modeling.kt`

## What to focus on

- `01_task_pipeline.kt`: sealed modeling and small transformation pipelines
- `02_result_and_sequences.kt`: `Result`, sequences, and failure handling
- `03_parser_error_modeling.kt`: explicit success and failure types

## Commands

```bash
kotlinc advanced/01_task_pipeline.kt -include-runtime -d advanced01.jar
java -jar advanced01.jar

kotlinc advanced/02_result_and_sequences.kt -include-runtime -d advanced02.jar
java -jar advanced02.jar

kotlinc advanced/03_parser_error_modeling.kt -include-runtime -d advanced03.jar
java -jar advanced03.jar
```
