# PFKotlin

PFKotlin is an English-first Kotlin practice repository for daily training.

The goal is simple:

- keep every lesson small and readable
- practice Kotlin syntax by editing runnable code
- grow from foundations into more expressive Kotlin patterns
- use one small project to connect language features to real design choices

## Learning Path

### 1. Foundations

Each file in `foundations/` is a focused lesson.

Suggested order:

1. `01_hello_collections.kt`
2. `02_null_safety_and_when.kt`
3. `03_data_classes_and_extensions.kt`

Topics covered:

- variables and string templates
- lists, maps, and collection operators
- `when` expressions
- null safety with `?.`, `?:`, and `let`
- data classes
- extension functions

### 2. Advanced

The `advanced/` folder moves from syntax drills to more idiomatic Kotlin design.

Suggested order:

1. `01_task_pipeline.kt`
2. `02_result_and_sequences.kt`

Topics covered:

- sealed interfaces and exhaustive `when`
- generic transformation pipelines
- lazy sequences
- `Result`
- small domain modeling decisions

### 3. Projects

The `projects/` folder turns the syntax into a usable feature.

Current project:

1. `01_study_planner_cli`

This mini project practices:

- data classes and enums
- extension functions
- collection grouping and sorting
- simple scoring strategies
- command-line style program structure
- file reading and parsing

## Repository Layout

```text
PFKotlin/
  foundations/                # small Kotlin syntax drills
  advanced/                   # more idiomatic and expressive Kotlin practice
  projects/                   # small runnable project work
  data/                       # sample input data for the CLI project
  CHANGELOG.md                # short English progress log
```

## Commands You Can Use Later

If Kotlin is installed on your machine, these files can be compiled and run directly.

```bash
kotlinc foundations/01_hello_collections.kt -include-runtime -d foundations01.jar
java -jar foundations01.jar

kotlinc advanced/01_task_pipeline.kt -include-runtime -d advanced01.jar
java -jar advanced01.jar

kotlinc projects/01_study_planner_cli/Main.kt -include-runtime -d study-planner.jar
java -jar study-planner.jar
java -jar study-planner.jar --energy HIGH
java -jar study-planner.jar --file data/study_tasks.txt
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
