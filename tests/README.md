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
- `03_architecture_app_tests.kt`: architecture app flow checks
- `04_course_tracker_mvvm_tests.kt`: MVVM app state flow checks

## Commands

```bash
kotlinc projects/01_study_planner_cli/*.kt tests/TestSupport.kt tests/01_study_planner_tests.kt -include-runtime -d planner-tests.jar
java -cp planner-tests.jar _01_study_planner_testsKt

kotlinc advanced/03_parser_error_modeling.kt tests/TestSupport.kt tests/02_parser_error_modeling_tests.kt -include-runtime -d parser-tests.jar
java -cp parser-tests.jar _02_parser_error_modeling_testsKt

kotlinc projects/02_architecture_app/*.kt tests/TestSupport.kt tests/03_architecture_app_tests.kt -include-runtime -d architecture-tests.jar
java -cp architecture-tests.jar _03_architecture_app_testsKt

kotlinc projects/03_course_tracker_mvvm/AppModels.kt \
  projects/03_course_tracker_mvvm/DataLayer.kt \
  projects/03_course_tracker_mvvm/DomainLayer.kt \
  projects/03_course_tracker_mvvm/PresentationLayer.kt \
  projects/03_course_tracker_mvvm/Main.kt \
  tests/TestSupport.kt \
  tests/04_course_tracker_mvvm_tests.kt \
  -include-runtime -d course-tracker-tests.jar
java -cp course-tracker-tests.jar _04_course_tracker_mvvm_testsKt
```

## Suggested habit

- run tests before editing a project file
- make a small change
- run tests again
- add one new assertion if you changed behavior on purpose
