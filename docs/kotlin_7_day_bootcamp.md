# Kotlin 7-Day Bootcamp

This plan is designed for short, focused practice with the current `PFKotlin` repository.

## Before you begin

Read these first:

- [kotlin_official_study_map.md](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/docs/kotlin_official_study_map.md)
- [practice_workspace_guide.md](/Users/platojobs/Desktop/Github/flutterffi/PFKotlin/docs/practice_workspace_guide.md)

Core rule for the whole week:

- read a small amount
- run a small file
- change one thing
- rerun it
- write one short note

---

## Day 1: Syntax and collections

### Goal

- get comfortable reading Kotlin syntax
- understand values, lists, maps, and string templates

### Read

- [Basic syntax overview](https://kotlinlang.org/docs/basic-syntax.html)
- [Types overview](https://kotlinlang.org/docs/types-overview.html)
- [Collections overview](https://kotlinlang.org/docs/collections-overview.html)

### Run

```bash
kotlinc foundations/01_hello_collections.kt -include-runtime -d day01.jar
java -jar day01.jar
```

### Change

- add two more topics
- print the shortest topic
- create one more map field

### Deliverable

- one modified version saved in `playground/scratch/day01_collections.kt`

---

## Day 2: Null safety and control flow

### Goal

- get fluent with `when`, nullable types, Elvis operator, and `let`

### Read

- [Null safety](https://kotlinlang.org/docs/null-safety.html)
- [Control flow](https://kotlinlang.org/docs/control-flow.html)

### Run

```bash
kotlinc foundations/02_null_safety_and_when.kt -include-runtime -d day02.jar
java -jar day02.jar
```

### Change

- add one more score branch
- add another nullable input
- rewrite one part using a different nullable style

### Deliverable

- one short note: when should you use `?:`, and when should you use `let`?

---

## Day 3: Data classes and extensions

### Goal

- understand Kotlin object modeling basics
- practice extension functions

### Read

- [Classes](https://kotlinlang.org/docs/classes.html)
- [Functions](https://kotlinlang.org/docs/functions.html)
- [Extensions](https://kotlinlang.org/docs/extensions.html)

### Run

```bash
kotlinc foundations/03_data_classes_and_extensions.kt -include-runtime -d day03.jar
java -jar day03.jar
```

### Change

- add a new property to `PracticeTask`
- create one new extension function
- calculate one more summary value

### Deliverable

- a variant in `playground/kata/day03_extensions.kt`

---

## Day 4: Transformations and result handling

### Goal

- practice list transformation and `Result`
- learn how Kotlin code becomes more expressive

### Read

- [Lambdas](https://kotlinlang.org/docs/lambdas.html)
- [Scope functions](https://kotlinlang.org/docs/scope-functions.html)

### Run

```bash
kotlinc advanced/01_task_pipeline.kt -include-runtime -d day04a.jar
java -jar day04a.jar

kotlinc advanced/02_result_and_sequences.kt -include-runtime -d day04b.jar
java -jar day04b.jar
```

### Change

- add one more task lane rule
- add one more invalid duration case
- rewrite one transformation in a different style

### Deliverable

- explain in one paragraph what `Result` helped you avoid

---

## Day 5: Parser thinking

### Goal

- practice success and failure modeling
- understand why explicit parsing logic matters

### Read

- [Sealed classes and interfaces](https://kotlinlang.org/docs/sealed-classes.html)

### Run

```bash
kotlinc advanced/03_parser_error_modeling.kt -include-runtime -d day05.jar
java -jar day05.jar
```

### Change

- add one new parser failure type
- add one new broken input line
- change the output wording

### Deliverable

- one custom parser experiment in `playground/kata/day05_parser.kt`

---

## Day 6: Project practice

### Goal

- work with a realistic multi-file Kotlin project
- understand input, scoring, output, and JSON flow

### Run

```bash
kotlinc projects/01_study_planner_cli/*.kt -include-runtime -d study-planner.jar
java -jar study-planner.jar
java -jar study-planner.jar --topic COLLECTIONS --top 1 --explain
java -jar study-planner.jar --import-json data/study_tasks.json
```

### Explore layer demos

```bash
kotlinc projects/01_study_planner_cli/*.kt -include-runtime -d planner-demos.jar
java -cp planner-demos.jar ScoringDemoKt
java -cp planner-demos.jar OutputDemoKt
java -cp planner-demos.jar JsonDemoKt
```

### Change

- adjust one score factor
- change one sample task
- rerun one CLI command and one demo

### Deliverable

- one screenshot or note showing which task ranking changed and why

---

## Day 7: Confidence pass

### Goal

- verify changes safely
- connect syntax knowledge to real workflow

### Run tests

```bash
kotlinc projects/01_study_planner_cli/*.kt tests/TestSupport.kt tests/01_study_planner_tests.kt -include-runtime -d planner-tests.jar
java -cp planner-tests.jar _01_study_planner_testsKt

kotlinc advanced/03_parser_error_modeling.kt tests/TestSupport.kt tests/02_parser_error_modeling_tests.kt -include-runtime -d parser-tests.jar
java -cp parser-tests.jar _02_parser_error_modeling_testsKt
```

### Change

- make one intentional break
- run tests and observe the failure
- fix it
- rerun tests

### Deliverable

- write a short summary:
  - what Kotlin features feel clear now
  - what still feels weak
  - what you want to study next

---

## Recommended daily rhythm

- 20 minutes reading official docs
- 20 minutes running and changing code
- 20 minutes writing your own variation
- 10 minutes saving notes

## After the 7 days

Best next steps:

1. rebuild one file from memory in `playground/`
2. add one new CLI flag to the planner
3. add one new failing test before changing code
4. start a coroutine-focused mini project
