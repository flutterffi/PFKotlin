# Tests

This folder keeps direct-run Kotlin tests for practice code.

## Goal

- build the habit of checking behavior after changes
- practice reading assertions
- confirm that refactors do not break output

## Files

- `TestSupport.kt`: tiny helpers for direct-run assertions
- `01_study_planner_tests.kt`: planner behavior checks
- `02_parser_error_modeling_tests.kt`: parser result checks

## Commands

```bash
kotlinc projects/01_study_planner_cli/*.kt tests/TestSupport.kt tests/01_study_planner_tests.kt -include-runtime -d planner-tests.jar
java -cp planner-tests.jar _01_study_planner_testsKt

kotlinc advanced/03_parser_error_modeling.kt tests/TestSupport.kt tests/02_parser_error_modeling_tests.kt -include-runtime -d parser-tests.jar
java -cp parser-tests.jar _02_parser_error_modeling_testsKt
```

## Suggested habit

- run tests before editing a project file
- make a small change
- run tests again
- add one new assertion if you changed behavior on purpose
