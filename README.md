# PFKotlin

PFKotlin is an English-first Kotlin practice repository for daily training.

The goal is simple:

- keep every lesson small and readable
- practice Kotlin syntax by editing runnable code
- grow from foundations into more expressive Kotlin patterns
- use one small project to connect language features to real design choices

## Official Study Links

If you want one place to review Kotlin basics and the official references, start here:

- [docs/kotlin_official_study_map.md](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/docs/kotlin_official_study_map.md)
- [docs/kotlin_7_day_bootcamp.md](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/docs/kotlin_7_day_bootcamp.md)
- [docs/practice_workspace_guide.md](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/docs/practice_workspace_guide.md)
- [docs/kotlin_advanced_app_track.md](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/docs/kotlin_advanced_app_track.md)

This study map links the official Kotlin documentation to the matching files in this repository.
The 7-day bootcamp gives you a concrete sprint plan with daily reading, commands, and exercises.
The workspace guide explains where to read, where to change code, and where to practice freely.

## Learning Path

### 1. Foundations

Each file in `foundations/` is a focused lesson.

Suggested order:

1. `01_hello_collections.kt`
2. `02_null_safety_and_when.kt`
3. `03_data_classes_and_extensions.kt`
4. `04_variables_and_types.kt`
5. `05_loops_and_ranges.kt`
6. `06_functions_basics.kt`
7. `07_classes_and_objects.kt`
8. `08_exceptions_basics.kt`
9. `09_interfaces_and_inheritance.kt`

Topics covered:

- variables and string templates
- variables and basic types
- lists, maps, and collection operators
- `when` expressions
- loops and ranges
- null safety with `?.`, `?:`, and `let`
- functions, parameters, and return values
- data classes
- classes, objects, interfaces, and inheritance
- extension functions
- exceptions and validation

### 2. Advanced

The `advanced/` folder moves from syntax drills to more idiomatic Kotlin design.

Suggested order:

1. `01_task_pipeline.kt`
2. `02_result_and_sequences.kt`
3. `03_parser_error_modeling.kt`

Topics covered:

- sealed interfaces and exhaustive `when`
- generic transformation pipelines
- lazy sequences
- `Result`
- sealed success and failure modeling
- parser-style error handling
- small domain modeling decisions

### 3. Tests

The `tests/` folder introduces direct-run Kotlin tests without requiring a full build tool yet.

Suggested order:

1. `01_study_planner_tests.kt`
2. `02_parser_error_modeling_tests.kt`

Topics covered:

- lightweight assertion helpers
- verifying ranking logic
- validating parser behavior
- using temporary files in tests

### 4. Projects

The `projects/` folder turns the syntax into a usable feature.

Current project:

1. `01_study_planner_cli`
2. `02_architecture_app`
3. `03_course_tracker_mvvm`

This mini project practices:

- data classes and enums
- extension functions
- collection grouping and sorting
- simple scoring strategies
- command-line style program structure
- file reading and parsing
- exporting reports to files
- filtering and narrowing ranked results
- manual JSON import and export
- score explanation and ranking transparency
- splitting a larger CLI into clearer source files
- architecture layering with data, domain, and presentation boundaries
- MVVM-style state handling with search, filtering, and bookmarking flows

## Repository Layout

```text
PFKotlin/
  foundations/                # small Kotlin syntax drills
  advanced/                   # more idiomatic and expressive Kotlin practice
  tests/                      # direct-run Kotlin tests for practice code
  projects/                   # small runnable project work
  data/                       # sample input data for the CLI project
  docs/                       # official study links and focused learning maps
  playground/                 # safe hands-on area for your own experiments
  CHANGELOG.md                # short English progress log
```

## Commands You Can Use Later

If Kotlin is installed on your machine, these files can be compiled and run directly.

```bash
kotlinc foundations/01_hello_collections.kt -include-runtime -d foundations01.jar
java -jar foundations01.jar

kotlinc advanced/01_task_pipeline.kt -include-runtime -d advanced01.jar
java -jar advanced01.jar

kotlinc advanced/03_parser_error_modeling.kt -include-runtime -d advanced03.jar
java -jar advanced03.jar

kotlinc projects/01_study_planner_cli/*.kt tests/TestSupport.kt tests/01_study_planner_tests.kt -include-runtime -d planner-tests.jar
java -cp planner-tests.jar _01_study_planner_testsKt

kotlinc advanced/03_parser_error_modeling.kt tests/TestSupport.kt tests/02_parser_error_modeling_tests.kt -include-runtime -d parser-tests.jar
java -cp parser-tests.jar _02_parser_error_modeling_testsKt

kotlinc projects/01_study_planner_cli/*.kt -include-runtime -d study-planner.jar
java -jar study-planner.jar
java -jar study-planner.jar --energy HIGH
java -jar study-planner.jar --file data/study_tasks.txt
java -jar study-planner.jar --import-json data/study_tasks.json
java -jar study-planner.jar --file data/study_tasks.txt --save reports/today.txt
java -jar study-planner.jar --file data/study_tasks.txt --export-json reports/today.json
java -jar study-planner.jar --topic COLLECTIONS --top 1 --explain

kotlinc projects/01_study_planner_cli/*.kt -include-runtime -d planner-demos.jar
java -cp planner-demos.jar ScoringDemoKt
java -cp planner-demos.jar OutputDemoKt
java -cp planner-demos.jar JsonDemoKt

kotlinc projects/02_architecture_app/*.kt -include-runtime -d architecture-app.jar
java -jar architecture-app.jar

kotlinc projects/02_architecture_app/*.kt tests/TestSupport.kt tests/03_architecture_app_tests.kt -include-runtime -d architecture-tests.jar
java -cp architecture-tests.jar _03_architecture_app_testsKt

kotlinc projects/03_course_tracker_mvvm/AppModels.kt \
  projects/03_course_tracker_mvvm/DataLayer.kt \
  projects/03_course_tracker_mvvm/DomainLayer.kt \
  projects/03_course_tracker_mvvm/PresentationLayer.kt \
  projects/03_course_tracker_mvvm/Main.kt \
  -include-runtime -d course-tracker-app.jar
java -jar course-tracker-app.jar

kotlinc projects/03_course_tracker_mvvm/AppModels.kt \
  projects/03_course_tracker_mvvm/DataLayer.kt \
  projects/03_course_tracker_mvvm/DomainLayer.kt \
  projects/03_course_tracker_mvvm/PresentationLayer.kt \
  projects/03_course_tracker_mvvm/ViewPreview.kt \
  -include-runtime -d course-tracker-preview.jar
java -jar course-tracker-preview.jar

kotlinc projects/03_course_tracker_mvvm/AppModels.kt \
  projects/03_course_tracker_mvvm/DataLayer.kt \
  projects/03_course_tracker_mvvm/DomainLayer.kt \
  projects/03_course_tracker_mvvm/PresentationLayer.kt \
  projects/03_course_tracker_mvvm/Main.kt \
  tests/TestSupport.kt tests/04_course_tracker_mvvm_tests.kt \
  -include-runtime -d course-tracker-tests.jar
java -cp course-tracker-tests.jar _04_course_tracker_mvvm_testsKt
```

## How To Practice

Use the repository in loops:

1. Read one lesson.
2. Predict the output.
3. Change one rule.
4. Run it again.
5. Add a variation beside it.

Good modifications to try:

- add your own enum value
- replace a loop with `map` or `groupBy`
- create a new extension function
- add one more rule to the planner score
- convert one print-heavy section into returned values
- add a new CLI flag and wire it into the parsing flow
- write your own parser with a sealed success and failure model
- add one more test file and cover a failure case you broke on purpose
- add a new planner filter and verify it with a direct-run test
- compare text input and JSON input for the same task set
- add one more score factor and verify its effect with `--explain`
- move one more function into a better file and keep the tests green
